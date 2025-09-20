package org.tikito.util;

import org.testcontainers.containers.MariaDBContainer;

import java.util.List;

public class MariadbTestContainer extends MariaDBContainer<MariadbTestContainer> {

    private static MariadbTestContainer _instance;

    private MariadbTestContainer() {
        super("mariadb:10.3.39");
    }

    public static MariadbTestContainer instance() {
        if (_instance == null) {
            _instance = new MariadbTestContainer()
                    .withDatabaseName("tikito");
            _instance.setPortBindings(List.of("13306:3306"));
        }

        return _instance;
    }



    @Override
    public void start() {
        super.start();
    }
}
