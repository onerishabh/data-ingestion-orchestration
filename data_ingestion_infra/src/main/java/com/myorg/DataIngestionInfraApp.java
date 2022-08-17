/*
 * @author Rishabh Srivastava (rishabh080598@gmail.com)
 */

package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;


public class DataIngestionInfraApp {
    public static void main(final String[] args) {
        App app = new App();

        new NetworkStack(app, "NetworkStack");

        DataIngestionInfraStack appStack = new DataIngestionInfraStack(app, "DataIngestionInfraStack", StackProps.builder()
                .build());
        
        int number_of_tasks = 2;
        new LoadTesterInfraStack(app, "LoadTesterInfraStack", StackProps.builder()
                .build(), appStack.func_url.getUrl(), number_of_tasks);
        
        new MonitorLoadStack(app, "MonitorLoadStack", StackProps.builder()
                .build(), appStack.entry_function.getFunctionName());
        
        new MSKClusterStack(app, "MSKClusterStack");

        app.synth();
    }
}

