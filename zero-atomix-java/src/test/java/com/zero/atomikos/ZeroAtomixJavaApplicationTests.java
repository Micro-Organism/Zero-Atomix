package com.zero.atomikos;

import com.zero.atomikos.common.constant.AtomixConstant;
import com.zero.atomikos.common.util.AtomixUtil;
import io.atomix.cluster.Member;
import io.atomix.core.Atomix;
import io.atomix.core.election.AsyncLeaderElector;
import io.atomix.core.election.Leadership;
import io.atomix.core.map.AsyncAtomicMap;
import io.atomix.primitive.Recovery;
import io.atomix.protocols.raft.MultiRaftProtocol;
import io.atomix.utils.time.Versioned;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@SpringBootTest
class ZeroAtomixJavaApplicationTests {

    @Value("${zero.atomix.async-map.name}")
    private String asyncMapName;
    @Value("${zero.atomix.async-map.key}")
    private String asyncMapKey;
    @Value("${zero.atomix.async-map.value}")
    private String asyncMapValue;
    @Value("${zero.atomix.async-leader-elector.name}")
    private String asyncLeaderElectorName;
    @Value("${zero.atomix.async-leader-elector.protocol.max-time-out}")
    private Long asyncLeaderElectorProtocolMaxTimeOut;

    @Autowired
    private Atomix atomix;

    @Test
    void testAtomix() throws ExecutionException, InterruptedException {

//        Atomix atomix = AtomixUtil.buildAtomix();
        //atomix启动并加入集群
        atomix.start().join();

        //创建atomixMap
        AsyncAtomicMap<Object, Object> asyncAtomicMap = atomix.atomicMapBuilder(asyncMapName)
                .withProtocol(MultiRaftProtocol.builder(AtomixConstant.ATOMIX_GROUP_NAME)
                        .withRecoveryStrategy(Recovery.RECOVER)
                        .withMaxRetries(AtomixConstant.ATOMIX_MAX_RETRIES)
                        .build())
                .withReadOnly(false)
                .build()
                .async();
        //进行数据存储
        asyncAtomicMap.put(asyncMapKey, asyncMapValue);
        //进行查询
        CompletableFuture<Versioned<Object>> myBlog = asyncAtomicMap.get(asyncMapKey);
        Versioned<Object> objectVersioned = myBlog.get();
        System.out.printf("value:%s version:%s%n", objectVersioned.value(), objectVersioned.version());

        //Elector
        AsyncLeaderElector leaderElector = atomix.leaderElectorBuilder(asyncLeaderElectorName)
                .withProtocol(MultiRaftProtocol.builder(AtomixConstant.ATOMIX_GROUP_NAME)
                        .withRecoveryStrategy(Recovery.RECOVER)
                        .withMaxRetries(AtomixConstant.ATOMIX_MAX_RETRIES)
                        .withMaxTimeout(Duration.ofMillis(asyncLeaderElectorProtocolMaxTimeOut))
                        .build())
                .withReadOnly(false)
                .build()
                .async();
        //获取出当前节点
        Member localMember = atomix.getMembershipService().getLocalMember();
        System.out.println("localMember:" + localMember.toString());
        String topic = "this is a topic";
        //根据某一topic选举出leader,返回的是选举为leader的节点
        Leadership leadership = (Leadership) leaderElector.run(topic, localMember.toString()).get();
        System.out.println("==========" + leadership);
        //get leadership
        Leadership topicLeadership = (Leadership) leaderElector.getLeadership(topic).get();
        System.out.println("------------>" + topicLeadership);
        //输出所有的topic对应的leader
        Map topicLeadershipMaps = (Map) leaderElector.getLeaderships().get();
        System.out.println("++++++++++++" + topicLeadershipMaps.toString());

    }

}
