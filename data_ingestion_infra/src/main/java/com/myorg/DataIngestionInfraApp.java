/*
 * @author Rishabh Srivastava (rishabh080598@gmail.com)
 */

package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;


public class DataIngestionInfraApp {
    public static void main(final String[] args) {
        App app = new App();

        DataIngestionInfraStack appStack = new DataIngestionInfraStack(app, "DataIngestionInfraStack", StackProps.builder()
                .build());
        
        new LoadTesterInfraStack(app, "LoadTesterInfraStack", StackProps.builder()
                .build(), appStack.func_url.getUrl());
        
        new MonitorLoadStack(app, "MonitorLoadStack", StackProps.builder()
                .build(), appStack.entry_function.getFunctionName());

        app.synth();
    }
}

