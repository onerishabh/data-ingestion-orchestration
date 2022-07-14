package com.myorg;

import java.util.Arrays;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecs.Cluster;
/*
 * @author Rishabh Srivastava (rishabh080598@gmail.com)
 */

 import software.amazon.awscdk.services.ecs.FargateTaskDefinition;
import software.amazon.awscdk.services.ecs.ContainerDefinitionOptions;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.FargateService;

public class LoadTesterInfraStack extends Stack{
    public LoadTesterInfraStack(final Construct scope, final String id) {
        this(scope, id, null, null, 2);
    }

    public LoadTesterInfraStack(final Construct scope, final String id, final StackProps props, 
                                        final String func_url, int number_of_tasks) {
        super(scope, id, props);

        final Cluster ecs_cluster = Cluster.Builder.create(this, "ClusterService")
                    .build();
        
        final FargateTaskDefinition task_def = FargateTaskDefinition.Builder.create(this, "LoadTesterTask")
                    .build();
        task_def.addContainer("LoadTester", ContainerDefinitionOptions.builder()
                    .image(ContainerImage.fromAsset("../load_testing"))
                    .build()).addEnvironment("URL", func_url+"?user_name=Rishabh&email=avc@gmail.com&pincode=3055");;

        final FargateService ecs_fargate = FargateService.Builder.create(this, "LoadTesterService")
                    .cluster(ecs_cluster)
                    .taskDefinition(task_def)
                    .desiredCount(number_of_tasks)
                    .build();

    }
    
}
