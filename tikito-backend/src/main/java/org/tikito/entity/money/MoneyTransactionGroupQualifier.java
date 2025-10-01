package org.tikito.entity.money;

import org.tikito.dto.export.MoneyTransactionGroupQualifierExportDto;
import org.tikito.dto.money.MoneyTransactionField;
import org.tikito.dto.money.MoneyTransactionGroupQualifierDto;
import org.tikito.dto.money.MoneyTransactionGroupQualifierType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoneyTransactionGroupQualifier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    private MoneyTransactionGroup group;

    @Enumerated(EnumType.STRING)
    private MoneyTransactionGroupQualifierType qualifierType;
    private String qualifier;

    @Enumerated(EnumType.STRING)
    private MoneyTransactionField transactionField;

    public MoneyTransactionGroupQualifier(final MoneyTransactionGroup group, final MoneyTransactionGroupQualifierDto dto) {
        this.group = group;
        this.qualifierType = dto.getQualifierType();
        this.qualifier = dto.getQualifier();
        this.transactionField = dto.getTransactionField();
    }

    public MoneyTransactionGroupQualifier(final MoneyTransactionGroup group, final MoneyTransactionGroupQualifierExportDto dto) {
        this.group = group;
        this.qualifierType = dto.getQualifierType();
        this.qualifier = dto.getQualifier();
        this.transactionField = dto.getTransactionField();
    }

    public MoneyTransactionGroupQualifierDto toDto() {
        return new MoneyTransactionGroupQualifierDto(
                id,
                group.getId(),
                qualifierType,
                qualifier,
                transactionField);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        final MoneyTransactionGroupQualifier that = (MoneyTransactionGroupQualifier) o;
        return Objects.equals(group.getId(), that.group.getId()) &&
                qualifierType == that.qualifierType &&
                Objects.equals(qualifier, that.qualifier) &&
                Objects.equals(transactionField, that.transactionField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group.getId(), qualifierType, qualifier, transactionField);
    }

    public MoneyTransactionGroupQualifierExportDto toExportDto() {
        return new MoneyTransactionGroupQualifierExportDto(
                group.getName(),
                qualifierType,
                qualifier,
                transactionField);
    }
}
