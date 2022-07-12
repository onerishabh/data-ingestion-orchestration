package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;


public class DataIngestionInfraApp {
    public static void main(final String[] args) {
        App app = new App();

        new DataIngestionInfraStack(app, "DataIngestionInfraStack", StackProps.builder()
                .build());

        app.synth();
    }
}
