package org.tikito.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tikito.auth.*;
import org.tikito.controller.request.ActivateRequest;
import org.tikito.controller.request.LoginRequest;
import org.tikito.dto.DateRange;
import org.tikito.dto.UserAccountDto;
import org.tikito.dto.UserPreferenceKey;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.UserAccount;
import org.tikito.entity.UserPreference;
import org.tikito.entity.security.Isin;
import org.tikito.entity.security.Security;
import org.tikito.exception.EmailAlreadyExistsException;
import org.tikito.exception.InvalidCredentialsException;
import org.tikito.exception.PasswordNotLongEnoughException;
import org.tikito.exception.RequestNotAllowedException;
import org.tikito.repository.IsinRepository;
import org.tikito.repository.SecurityRepository;
import org.tikito.repository.UserAccountRepository;
import org.tikito.repository.UserPreferenceRepository;
import org.tikito.service.importer.FileReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class UserAccountService {
    private final UserAccountRepository userAccountRepository;
    private final CacheService cacheService;
    private final SecurityRepository securityRepository;
    private final IsinRepository isinRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public UserAccountService(final UserAccountRepository userAccountRepository,
                              final CacheService cacheService,
                              final SecurityRepository securityRepository,
                              final IsinRepository isinRepository,
                              final UserPreferenceRepository userPreferenceRepository) {
        this.userAccountRepository = userAccountRepository;
        this.cacheService = cacheService;
        this.securityRepository = securityRepository;
        this.isinRepository = isinRepository;
        this.userPreferenceRepository = userPreferenceRepository;
    }


    public LoggedInUserDto login(final LoginRequest request) throws InvalidCredentialsException {
        log.info("Logging in {}", request.getEmail());
        final UserAccount userAccount = userAccountRepository.findByEmail(request.getEmail()).orElseThrow(() -> new InvalidCredentialsException("invalid"));
        if (!PasswordUtil.checkBcryptHash(request.getPassword().toCharArray(), userAccount.getPassword())) {
            throw new InvalidCredentialsException("Invalid");
        } else if (!userAccount.isActivated()) {
            throw new InvalidCredentialsException("invalid");
        }
        final String jwt = JwtGeneratorService.generateJwt(userAccount.toAuthUser(), Arrays.asList(Scope.values()));

        return new LoggedInUserDto(
                userAccount,
                jwt);
    }

    public LoggedInUserDto getLoggedInUser(final AuthUser authUser) {
        final UserAccount userAccount = userAccountRepository.findById(authUser.getId()).orElseThrow(() -> new InvalidCredentialsException("invalid"));

        return new LoggedInUserDto(
                userAccount,
                authUser.getJwt());
    }

    public void updatePassword(final long userId, final String currentPassword, final String newPassword) throws RequestNotAllowedException, PasswordNotLongEnoughException {
        log.info("Updating password of {}", userId);
        final UserAccount userAccount = userAccountRepository.findById(userId).orElseThrow(RequestNotAllowedException::new);
        if (!PasswordUtil.checkBcryptHash(currentPassword.toCharArray(), userAccount.getPassword())) {
            throw new RequestNotAllowedException();
        } else if (!StringUtils.hasText(newPassword) || newPassword.length() < 10) {
            throw new PasswordNotLongEnoughException("Password must be more than 10 characters long");
        } else {
            userAccount.setPassword(PasswordUtil.createBcryptHash(newPassword.toCharArray()));
            userAccountRepository.save(userAccount);
        }
    }

    public void activate(final ActivateRequest request) {
        log.info("Activating {}", request.getActivationCode());
        final UserAccount userAccount = userAccountRepository.findByActivationCode(request.getActivationCode()).orElseThrow();
        userAccount.setActivated(true);
        userAccountRepository.save(userAccount);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void register(final String email, final String password) throws EmailAlreadyExistsException, PasswordNotLongEnoughException, IOException {
        assertEmailNotExists(email);
        assertPasswordStrongEnough(password);

        final UserAccount userAccount = new UserAccount(email, PasswordUtil.createBcryptHash(password.toCharArray()));
        final boolean firstEverUser = cacheService.isFirstEverUser();

        if (firstEverUser) {
            userAccount.setActivated(true);
            generateCurrencies();
        } else {
            throw new IllegalStateException();
        }
        final UserAccountDto dto = userAccountRepository.saveAndFlush(userAccount).toDto();

        if (firstEverUser) {
            cacheService.firstEverUserRegistered();
            cacheService.refreshCurrencies();
            generateDefaultPreferences(dto.getId());
        }
    }

    private void generateDefaultPreferences(final long userAccountId) {
        userPreferenceRepository.saveAllAndFlush(List.of(
                new UserPreference(userAccountId, UserPreferenceKey.AGGREGATE_DATE_RANGE, "true"),
                new UserPreference(userAccountId, UserPreferenceKey.AMOUNT_OF_OTHER_GROUPS, "5"),
                new UserPreference(userAccountId, UserPreferenceKey.MONEY_SHOW_OTHER, "true"),
                new UserPreference(userAccountId, UserPreferenceKey.DATE_RANGE, DateRange.MONTH.name()),
                new UserPreference(userAccountId, UserPreferenceKey.SECURITY_DATE_RANGE, DateRange.MONTH.name()),
                new UserPreference(userAccountId, UserPreferenceKey.SHOW_CLOSED_POSITIONS, "true"),
                new UserPreference(userAccountId, UserPreferenceKey.START_AT_ZERO_FROM_BEGINNING, "true"),
                new UserPreference(userAccountId, UserPreferenceKey.SECURITY_START_AT_ZERO_FROM_BEGINNING, "true"),
                new UserPreference(userAccountId, UserPreferenceKey.START_AT_ZERO_AFTER_DATE_RANGE, "true"),
                new UserPreference(userAccountId, UserPreferenceKey.SECURITY_START_AT_ZERO_AFTER_DATE_RANGE, "true")));
    }

    private void generateCurrencies() throws IOException {
        final List<List<String>> lists = FileReader.readCsv(new ClassPathResource("initial_currencies.csv").getInputStream(), ';', '"');

        lists.stream().skip(1).forEach(currency -> {
            final Security security = new Security();
            final Isin isin = new Isin();
            security.setName(currency.get(1));
            security.setSecurityType(SecurityType.CURRENCY);
            security.setCurrentIsin(currency.get(0));
            final Security persistedSecurity = securityRepository.saveAndFlush(security);

            isin.setIsin(currency.get(0));
            isin.setSymbol(currency.get(2));
            isin.setSecurityId(persistedSecurity.getId());
            isinRepository.saveAndFlush(isin);
        });
    }

    private void assertPasswordStrongEnough(final String password) throws PasswordNotLongEnoughException {
        if (password.length() < 10) {
            throw new PasswordNotLongEnoughException("Password must be more than 10 characters long");
        }
    }

    private void assertEmailNotExists(final String email) throws EmailAlreadyExistsException {
        if (userAccountRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    public List<UserAccountDto> getUsers() {
        return userAccountRepository
                .findAll()
                .stream()
                .map(UserAccount::toDto)
                .toList();
    }

    public UserAccountDto editUser(final long userAccountId, final String email, final String password) throws PasswordNotLongEnoughException {
        final UserAccount userAccount = userAccountRepository.findById(userAccountId).orElseThrow();

        if (StringUtils.hasText(password)) {
            if (!StringUtils.hasText(password) || password.length() < 10) {
                throw new PasswordNotLongEnoughException("Password must be more than 10 characters long");
            } else {
                userAccount.setPassword(PasswordUtil.createBcryptHash(password.toCharArray()));
            }
        }
        userAccount.setEmail(email);
        return userAccountRepository
                .saveAndFlush(userAccount)
                .toDto();
    }
}
