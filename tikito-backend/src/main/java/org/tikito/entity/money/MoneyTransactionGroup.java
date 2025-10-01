package org.tikito.entity.money;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.tikito.dto.export.MoneyTransactionGroupExportDto;
import org.tikito.dto.money.MoneyTransactionGroupDto;
import org.tikito.dto.money.MoneyTransactionGroupType;
import org.tikito.entity.Account;
import org.tikito.entity.budget.Budget;
import org.tikito.entity.loan.Loan;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MoneyTransactionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private String name;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "money_transaction_group_group_type",
            joinColumns = @JoinColumn(name = "money_transaction_group_id"))
    @Column(name = "group_type")
    private Set<MoneyTransactionGroupType> groupTypes = new HashSet<>();

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MoneyTransactionGroupQualifier> qualifiers = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "money_transaction_group_account_id",
            joinColumns = @JoinColumn(name = "money_transaction_account_id"))
    @Column(name = "account_id")
    private Set<Long> accountIds = new HashSet<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id")
    private Budget budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    public MoneyTransactionGroup(final MoneyTransactionGroupDto dto) {
        this.userId = dto.getUserId();
        this.name = dto.getName();
        this.groupTypes = new HashSet<>(dto.getGroupTypes());
        this.qualifiers = new ArrayList<>(
                dto
                        .getQualifiers()
                        .stream()
                        .map(qualifier -> new MoneyTransactionGroupQualifier(this, qualifier))
                        .toList());
    }

    public MoneyTransactionGroup(final long userId, final Set<Long> accountIds, final MoneyTransactionGroupExportDto group) {
        this.userId = userId;
        this.name = group.getName();
        this.groupTypes = group.getGroupTypes();
        this.qualifiers = new ArrayList<>(group
                .getQualifiers()
                .stream()
                .map(qualifier -> new MoneyTransactionGroupQualifier(this, qualifier))
                .toList());
        this.accountIds = new HashSet<>(accountIds);
    }

    public MoneyTransactionGroupDto toDto() {
        return new MoneyTransactionGroupDto(
                id,
                userId,
                name,
                groupTypes,
                qualifiers
                        .stream()
                        .map(MoneyTransactionGroupQualifier::toDto)
                        .toList(),
                accountIds);
    }

    public MoneyTransactionGroupExportDto toExportDto(final Map<Long, Account> accountsById) {
        return new MoneyTransactionGroupExportDto(
                name,
                groupTypes,
                qualifiers.stream().map(MoneyTransactionGroupQualifier::toExportDto).toList(),
                accountIds.stream().map(id -> accountsById.get(id).getName()).collect(Collectors.toSet()));
    }
}
