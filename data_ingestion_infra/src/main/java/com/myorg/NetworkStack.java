package com.myorg;

import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.CfnOutput;

public class NetworkStack extends Stack {
        public Vpc app_vpc;
        public SecurityGroup msk_sg;
        public SecurityGroup producer_lambda_sg;

        public NetworkStack(final Construct scope, final String id) {
                this(scope, id, null);
        }

        public NetworkStack(final Construct scope, final String id, final StackProps props) {
                super(scope, id, props);

                this.app_vpc = Vpc.Builder.create(this, "AppVPC")
                                .build();

                this.msk_sg = SecurityGroup.Builder.create(this, "MSKSecurityGroup")
                                .allowAllOutbound(true)
                                .vpc(app_vpc)
                                .build();

                this.producer_lambda_sg = SecurityGroup.Builder.create(this, "ProducerLambdaSecurityGroup")
                                .allowAllOutbound(true)
                                .vpc(app_vpc)
                                .build();

                this.msk_sg.getConnections().allowFrom(producer_lambda_sg, Port.allTraffic(), "LambdaToMSK");

                CfnOutput.Builder.create(this, "AppVPCId")
                                .value(this.app_vpc.getVpcId())
                                .build();
                CfnOutput.Builder.create(this, "MSKSGId")
                                .value(this.msk_sg.getSecurityGroupId())
                                .build();
                CfnOutput.Builder.create(this, "ProdcLambdaId")
                                .value(this.producer_lambda_sg.getSecurityGroupId())
                                .build();
        }
}
