package org.tikito.service;

import org.tikito.TestUtil;
import org.tikito.controller.request.CreateOrUpdateMoneyTransactionGroupRequest;
import org.tikito.dto.money.MoneyTransactionGroupDto;
import org.tikito.dto.money.MoneyTransactionGroupQualifierDto;
import org.tikito.dto.money.MoneyTransactionGroupQualifierType;
import org.tikito.entity.money.MoneyTransaction;
import org.tikito.entity.money.MoneyTransactionGroup;
import org.tikito.entity.money.MoneyTransactionGroupQualifier;
import org.tikito.service.money.MoneyTransactionGroupService;
import org.tikito.util.MariadbTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.tikito.dto.money.MoneyTransactionField.DESCRIPTION;
import static org.tikito.dto.money.MoneyTransactionGroupQualifierType.REGEX;
import static org.tikito.dto.money.MoneyTransactionGroupQualifierType.SIMILAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class MoneyTransactionGroupServiceTest extends BaseIntegrationTest {

    MariadbTestContainer testContainer = MariadbTestContainer.instance();

    @Autowired
    private MoneyTransactionGroupService groupService;

    @BeforeEach
    public void setup() {
        withDefaultCurrencies();
        withDefaultUserAccount();
        withDefaultAccounts();
        loginWithDefaultUser();
    }

    @Test
    void testCreateOrUpdateGroup() {
        final CreateOrUpdateMoneyTransactionGroupRequest request = new CreateOrUpdateMoneyTransactionGroupRequest();
        request.setName("My group");
        request.setQualifiers(List.of(
                qualifier(REGEX, "Some value"),
                qualifier(SIMILAR, "Other value")
        ));
        final MoneyTransactionGroupDto dto = groupService.createOrUpdateGroup(DEFAULT_USER_ACCOUNT.getId(), request);
        assertEquals("My group", dto.getName());
        assertEquals(2, dto.getQualifiers().size());
        assertEquals(REGEX, dto.getQualifiers().getFirst().getQualifierType());
        assertEquals("Some value", dto.getQualifiers().get(0).getQualifier());
        assertEquals(DESCRIPTION, dto.getQualifiers().get(0).getTransactionField());
        assertEquals(SIMILAR, dto.getQualifiers().get(1).getQualifierType());
        assertEquals("Other value", dto.getQualifiers().get(1).getQualifier());
        assertEquals(DESCRIPTION, dto.getQualifiers().get(1).getTransactionField());
    }

    @Test
    void testCreateOrUpdateGroupWithDuplicates() {
        withDefaultMoneyTransactionGroups();

        final CreateOrUpdateMoneyTransactionGroupRequest request = new CreateOrUpdateMoneyTransactionGroupRequest();
        request.setId(TRANSACTION_GROUP_REGEX.getId());
        request.setName("My group");
        request.setQualifiers(List.of());
        final MoneyTransactionGroupDto dto = groupService.createOrUpdateGroup(DEFAULT_USER_ACCOUNT.getId(), request);

        assertEquals(0, dto.getQualifiers().size());
        assertEquals("My group", dto.getName());
    }

    @Test
    void testGroupIncludes() {
        final MoneyTransactionGroup group = createGroup(List.of(
                createQualifier(MoneyTransactionGroupQualifierType.INCLUDES, "Company a")
        ));
        final MoneyTransaction transaction1 = createTransaction("this is really company\n A that I bought something from");
        final MoneyTransaction transaction2 = createTransaction("this is really company B that I bought something from");

        groupService.groupTransactions(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_ACCOUNT.getId());

        final MoneyTransaction updatedTransaction1 = moneyTransactionRepository.findById(transaction1.getId()).get();
        final MoneyTransaction updatedTransaction2 = moneyTransactionRepository.findById(transaction2.getId()).get();
        assertEquals(group.getId(), updatedTransaction1.getGroupId());
        assertNull(updatedTransaction2.getGroupId());
    }

    @Test
    void testGroupRegex() {
        final MoneyTransactionGroup group = createGroup(List.of(
                createQualifier(REGEX, "(.*)(AH)(.*)")
        ));
        final MoneyTransaction transaction1 = createTransaction("this is really company\n AH that I bought something from");
        final MoneyTransaction transaction2 = createTransaction("this is really company AB that I bought something from");

        groupService.groupTransactions(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_ACCOUNT.getId());

        final MoneyTransaction updatedTransaction1 = moneyTransactionRepository.findById(transaction1.getId()).get();
        final MoneyTransaction updatedTransaction2 = moneyTransactionRepository.findById(transaction2.getId()).get();
        assertEquals(group.getId(), updatedTransaction1.getGroupId());
        assertNull(updatedTransaction2.getGroupId());
    }

    @Test
    void testGroupInvalidRegex() {
        final MoneyTransactionGroup group = createGroup(List.of(
                createQualifier(REGEX, "(((()")
        ));
        final MoneyTransaction transaction1 = createTransaction("this is really company AH that I bought something from");
        final MoneyTransaction transaction2 = createTransaction("this is really company AB that I bought something from");

        groupService.groupTransactions(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_ACCOUNT.getId());

        final MoneyTransaction updatedTransaction1 = moneyTransactionRepository.findById(transaction1.getId()).get();
        final MoneyTransaction updatedTransaction2 = moneyTransactionRepository.findById(transaction2.getId()).get();
        assertNull(updatedTransaction1.getGroupId());
        assertNull(updatedTransaction2.getGroupId());
    }

    @Test
    void testGroupSimilar() {
        final MoneyTransactionGroup group = createGroup(List.of(
                createQualifier(SIMILAR, "this is really company BB that I bought something from")
        ));
        final MoneyTransaction transaction1 = createTransaction("this is really company AH that I bought something from");
        final MoneyTransaction transaction2 = createTransaction("This is another thing that I don't want to match");

        groupService.groupTransactions(DEFAULT_USER_ACCOUNT.getId(), DEFAULT_ACCOUNT.getId());

        final MoneyTransaction updatedTransaction1 = moneyTransactionRepository.findById(transaction1.getId()).get();
        final MoneyTransaction updatedTransaction2 = moneyTransactionRepository.findById(transaction2.getId()).get();
        assertEquals(group.getId(), updatedTransaction1.getGroupId());
        assertNull(updatedTransaction2.getGroupId());
    }

    private MoneyTransaction createTransaction(final String description) {
        final MoneyTransaction transaction = new MoneyTransaction();
        transaction.setAccountId(DEFAULT_ACCOUNT.getId());
        transaction.setUserId(DEFAULT_USER_ACCOUNT.getId());
        transaction.setAmount(TestUtil.randomFloat(1, 10));
        transaction.setCurrencyId(CURRENCY_EURO_ID);
        transaction.setDescription(description);
        transaction.setTimestamp(Instant.now());
        transaction.setCounterpartAccountName(TestUtil.randomString(5, 10));
        transaction.setCounterpartAccountNumber(TestUtil.randomIBAN());
        transaction.setFinalBalance(TestUtil.randomFloat(100, 1000));
        return moneyTransactionRepository.save(transaction);
    }

    private MoneyTransactionGroup createGroup(final List<MoneyTransactionGroupQualifier> qualifiers) {
        final MoneyTransactionGroup group = new MoneyTransactionGroup();
        group.setQualifiers(qualifiers);
        qualifiers.forEach(qualifier -> qualifier.setGroup(group));
        group.setName(TestUtil.randomString(5, 10));
        group.setUserId(DEFAULT_USER_ACCOUNT.getId());
        return transactionGroupRepository.save(group);
    }

    private MoneyTransactionGroupQualifier createQualifier(final MoneyTransactionGroupQualifierType qualifierType, final String qualifier) {
        final MoneyTransactionGroupQualifier groupQualifier = new MoneyTransactionGroupQualifier();
        groupQualifier.setQualifierType(qualifierType);
        groupQualifier.setTransactionField(DESCRIPTION);
        groupQualifier.setQualifier(qualifier);
        return groupQualifier;
    }

    private MoneyTransactionGroupQualifierDto qualifier(final MoneyTransactionGroupQualifierType qualifierType, final String qualifier) {
        return new MoneyTransactionGroupQualifierDto(
                0,
                0,
                qualifierType,
                qualifier,
                DESCRIPTION);
    }
}