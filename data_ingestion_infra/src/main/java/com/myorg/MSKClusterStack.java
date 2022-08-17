package com.myorg;

import software.constructs.Construct;

import java.util.Arrays;
import java.util.stream.Collectors;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.msk.CfnClusterProps;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;

public class MSKClusterStack extends Stack {
    public CfnCluster msk_cluster;

    public MSKClusterStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public MSKClusterStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        NetworkStack net = new NetworkStack(this, "NetworkStack");

        this.msk_cluster = getMSKClusterDefinition(net.app_vpc, net.msk_sg);
    }

    private CfnCluster getMSKClusterDefinition(Vpc app_vpc, SecurityGroup msk_sg) {
        return new CfnCluster(this, "MSKSourcing",
                CfnClusterProps.builder()
                        .clusterName("DataIngestionMSKCluster")
                        .kafkaVersion("2.7.0")
                        .numberOfBrokerNodes(2)
                        .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                                .instanceType("kafka.t3.small")
                                .securityGroups(Arrays.asList(msk_sg.getSecurityGroupId()))
                                .clientSubnets(app_vpc.getPrivateSubnets().stream().map(z -> z.getSubnetId())
                                        .collect(Collectors.toList()))
                                .storageInfo(CfnCluster.StorageInfoProperty.builder()
                                        .ebsStorageInfo(CfnCluster.EBSStorageInfoProperty.builder()
                                                .volumeSize(2)
                                                .build())
                                        .build())

                                .build())
                        .build());
    }
}
