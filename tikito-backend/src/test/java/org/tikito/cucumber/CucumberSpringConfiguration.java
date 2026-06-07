package org.tikito.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.tikito.config.TestcontainersConfiguration;
import org.tikito.service.TimeService;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = TestcontainersConfiguration.class)
public class CucumberSpringConfiguration {

    @MockitoBean
    protected TimeService timeService;
}
