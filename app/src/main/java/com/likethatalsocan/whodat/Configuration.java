package com.likethatalsocan.whodat;

/**
 * Created by sathy on 30/12/17.
 */

public class Configuration {

    public static final String environment = Environment.PRODUCTION.getValue();

    private enum Environment {
        DEVELOPMENT("development"), PRODUCTION("production");

        private final String value;

        Environment(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
