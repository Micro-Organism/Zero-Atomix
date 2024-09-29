package com.zero.atomikos.common.config;

import com.zero.atomikos.common.constant.AtomixConstant;
import io.atomix.cluster.AtomixCluster;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Configuration
public class AtomixConfig {

    @Value("${zero.atomix.node}")
    private String node;
    @Value("${zero.atomix.cluster-id}")
    private String clusterId;
    @Value("${zero.atomix.member-id}")
    private String memberId;
    @Value("${zero.atomix.host}")
    private String host;
    @Value("${zero.atomix.manage-group.name}")
    private String manageGroupName;
    @Value("${zero.atomix.manage-group.port}")
    private Integer manageGroupPort;
    @Value("${zero.atomix.manage-group.data-dir}")
    private String manageGroupDataDir;
    @Value("${zero.atomix.manage-group.number-partition}")
    private Integer manageGroupNumPartitions;
    @Value("${zero.atomix.partition-group.name}")
    private String partitionGroupName;
    @Value("${zero.atomix.partition-group.data-dir}")
    private String partitionGroupDataDir;


    @Bean
    public Atomix buildAtomix() {

        //创建集群成员列表
        List<String> raftMembers = Collections.singletonList(node);

        //创建atomix
        return Atomix.builder(AtomixCluster.class.getClassLoader())
                .withClusterId(clusterId)
                .withMemberId(memberId)
                .withHost(host)
                .withPort(manageGroupPort)
                .withMembershipProvider(BootstrapDiscoveryProvider.builder().build())
                .withManagementGroup(RaftPartitionGroup.builder(manageGroupName)
                        .withNumPartitions(manageGroupNumPartitions)
                        .withDataDirectory(new File(AtomixConstant.ATOMIX_DATA_DIR, manageGroupDataDir))
                        .withMembers(raftMembers)
                        .build())
                .addPartitionGroup(RaftPartitionGroup.builder(AtomixConstant.ATOMIX_GROUP_NAME)
                        .withNumPartitions(raftMembers.size())
                        .withDataDirectory(new File(AtomixConstant.ATOMIX_DATA_DIR, partitionGroupDataDir))
                        .withMembers(raftMembers)
                        .build())
                .build();
    }

}
