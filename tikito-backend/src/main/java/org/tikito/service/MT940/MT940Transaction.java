package org.tikito.service.MT940;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MT940Transaction {
    private String toAccountNumber;
    private String toAccountName;
    private String description;
    private LocalDate date;
    private double amount;
}
