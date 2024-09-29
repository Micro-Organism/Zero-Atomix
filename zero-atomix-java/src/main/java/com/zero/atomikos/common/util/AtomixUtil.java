package com.zero.atomikos.common.util;

import com.zero.atomikos.common.constant.AtomixConstant;
import io.atomix.cluster.AtomixCluster;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class AtomixUtil {

    public static Atomix buildAtomix() {
        //集群成员
        List<String> raftMembers = Collections.singletonList("node1");
        //创建atomix
        return Atomix.builder(AtomixCluster.class.getClassLoader())
                .withClusterId("my-cluster")
                .withMemberId("node1")
                .withHost("127.0.0.1")
                .withPort(6789)
                .withMembershipProvider(BootstrapDiscoveryProvider.builder().build())
                .withManagementGroup(RaftPartitionGroup.builder("system")
                        .withNumPartitions(1)
                        .withDataDirectory(new File(AtomixConstant.ATOMIX_DATA_DIR, "system"))
                        .withMembers(raftMembers)
                        .build())
                .addPartitionGroup(RaftPartitionGroup.builder(AtomixConstant.ATOMIX_GROUP_NAME)
                        .withNumPartitions(raftMembers.size())
                        .withDataDirectory(new File(AtomixConstant.ATOMIX_DATA_DIR, "data"))
                        .withMembers(raftMembers)
                        .build())
                .build();
    }

}
