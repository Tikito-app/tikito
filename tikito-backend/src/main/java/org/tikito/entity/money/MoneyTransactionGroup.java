package org.tikito.entity.money;

import org.tikito.dto.money.MoneyTransactionGroupDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class MoneyTransactionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long userId;
    private String name;

    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MoneyTransactionGroupQualifier> qualifiers = new ArrayList<>();

    public MoneyTransactionGroupDto toDto() {
        return new MoneyTransactionGroupDto(
                id,
                name,
                qualifiers
                        .stream()
                        .map(MoneyTransactionGroupQualifier::toDto)
                        .toList());
    }
}
