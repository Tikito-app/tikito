package org.tikito.service;

import org.tikito.auth.LoggedInUserDto;
import org.tikito.controller.request.ActivateRequest;
import org.tikito.controller.request.LoginRequest;
import org.tikito.dto.security.SecurityType;
import org.tikito.entity.UserAccount;
import org.tikito.exception.EmailAlreadyExistsException;
import org.tikito.exception.InvalidCredentialsException;
import org.tikito.exception.PasswordNotLongEnoughException;
import org.tikito.exception.RequestNotAllowedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserAccountServiceTest extends BaseIntegrationTest {

    @Autowired
    private UserAccountService service;

    @Test
    void login_shouldSucceed_givenValidCredentials() {
        withDefaultUserAccount();
        final LoggedInUserDto loggedInUser = service.login(new LoginRequest(DEFAULT_USER_ACCOUNT.getEmail(), DEFAULT_USER_ACCOUNT_PASSWORD));
        assertEquals(DEFAULT_USER_ACCOUNT.getEmail(), loggedInUser.getEmail());
    }

    @Test
    void login_shouldThrowException_givenInvalidCredentials() {
        withDefaultUserAccount();
        assertThrows(InvalidCredentialsException.class, () -> service.login(new LoginRequest(DEFAULT_USER_ACCOUNT.getEmail(), "wrong-password")));
    }

    @Test
    void login_shouldThrowException_givenNotActivatedAccount() {
        final String email = randomString(10);
        withExistingUserAccount(email, DEFAULT_USER_ACCOUNT_PASSWORD, randomString(5));
        assertThrows(InvalidCredentialsException.class, () -> service.login(new LoginRequest(email, DEFAULT_USER_ACCOUNT_PASSWORD)));
    }

    @Test
    void changePassword_shouldSucceed_givenValidPassword() throws PasswordNotLongEnoughException, RequestNotAllowedException {
        withDefaultUserAccount();
        final String newPassword = randomString(15);
        service.updatePassword(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_USER_ACCOUNT_PASSWORD, newPassword);
        service.login(new LoginRequest(DEFAULT_USER_ACCOUNT.getEmail(), newPassword));
    }

    @Test
    void changePassword_shouldThrowRequestNotAllowedException_givenInvalidCurrentPassword() {
        withDefaultUserAccount();
        final String newPassword = randomString(15);
        assertThrows(RequestNotAllowedException.class, () -> service.updatePassword(DEFAULT_USER_ACCOUNT.getId(), "wrong-password", newPassword));
    }

    @Test
    void changePassword_shouldThrowPasswordNotLongEnoughException_givenInvalidCurrentPassword() {
        withDefaultUserAccount();
        final String newPassword = randomString(2);
        assertThrows(PasswordNotLongEnoughException.class, () -> service.updatePassword(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_USER_ACCOUNT_PASSWORD, newPassword));
    }

    @Test
    void activate_shouldActivate_givenValidActivationCode() {
        final String activationCode = randomString(10);
        final String email = randomString(10);
        withExistingUserAccount(email, DEFAULT_USER_ACCOUNT_PASSWORD, activationCode);
        service.activate(new ActivateRequest(activationCode));
        service.login(new LoginRequest(email, DEFAULT_USER_ACCOUNT_PASSWORD));
    }

    @Test
    void activate_shouldThrowException_givenInValidActivationCode() {
        withExistingUserAccount(randomString(10), randomString(10), randomString(10));
        assertThrows(NoSuchElementException.class, () -> service.activate(new ActivateRequest("invalid-code")));
    }

    @Test
    void register_shouldRegister_givenValidData() throws PasswordNotLongEnoughException, EmailAlreadyExistsException, IOException {
        withDefaultUserAccount();
        final String email = randomEmail();
        service.register(email, DEFAULT_USER_ACCOUNT_PASSWORD);
        final UserAccount userAccount = userAccountRepository.findByEmail(email).get();
        assertEquals(email, userAccount.getEmail());
        assertFalse(userAccount.isActivated());
        service.activate(new ActivateRequest(userAccount.getActivationCode()));
        service.login(new LoginRequest(email, DEFAULT_USER_ACCOUNT_PASSWORD));
    }

    @Test
    void register_shouldThrowEmailAlreadyExistsException_givenEmailAlreadyExists() {
        withDefaultUserAccount();
        assertThrows(EmailAlreadyExistsException.class, () -> service.register(DEFAULT_USER_ACCOUNT.getEmail(), randomString(20)));
    }

    @Test
    void register_shouldThrowPasswordNotLongEnoughException_givenPasswordNotStrongEnough() {
        withDefaultUserAccount();
        assertThrows(PasswordNotLongEnoughException.class, () -> service.register(randomEmail(), randomString(2)));
    }

    @Test
    void register_shouldInitialiseData_givenFirstEverUser() throws PasswordNotLongEnoughException, EmailAlreadyExistsException, IOException {
        final String email = randomEmail();
        assertTrue(securityRepository.findBySecurityType(SecurityType.CURRENCY).isEmpty());
        service.register(email, randomString(20));
        final UserAccount userAccount = userAccountRepository.findByEmail(email).get();
        assertTrue(userAccount.isActivated());
        assertEquals(168, securityRepository.findBySecurityType(SecurityType.CURRENCY).size());
        assertFalse(cacheService.isFirstEverUser());
    }
}