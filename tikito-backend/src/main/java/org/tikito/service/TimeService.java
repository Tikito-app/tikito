package org.tikito.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TimeService {

    public LocalDate now() {
        return LocalDate.now();
    }
}
