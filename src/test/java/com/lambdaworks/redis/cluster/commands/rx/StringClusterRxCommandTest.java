package com.lambdaworks.redis.cluster.commands.rx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import com.lambdaworks.redis.cluster.api.rx.RedisAdvancedClusterReactiveCommands;
import org.junit.*;

import com.google.common.collect.Maps;
import com.lambdaworks.redis.FastShutdown;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.TestSettings;
import com.lambdaworks.redis.api.sync.RedisCommands;
import com.lambdaworks.redis.cluster.ClusterTestUtil;
import com.lambdaworks.redis.cluster.RedisClusterClient;
import com.lambdaworks.redis.cluster.api.StatefulRedisClusterConnection;
import com.lambdaworks.redis.commands.StringCommandTest;
import com.lambdaworks.redis.commands.rx.RxSyncInvocationHandler;
import rx.Observable;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class StringClusterRxCommandTest extends StringCommandTest {
    private static RedisClusterClient redisClusterClient;
    private StatefulRedisClusterConnection<String, String> clusterConnection;

    @BeforeClass
    public static void setupClient() {
        redisClusterClient = new RedisClusterClient(RedisURI.Builder.redis(TestSettings.host(), TestSettings.port(900)).build());
    }

    @AfterClass
    public static void closeClient() {
        FastShutdown.shutdown(redisClusterClient);
    }

    @Before
    public void openConnection() throws Exception {
        redis = connect();
        ClusterTestUtil.flushDatabaseOfAllNodes(clusterConnection);
    }

    @Override
    protected RedisCommands<String, String> connect() {
        clusterConnection = redisClusterClient.connectCluster().getStatefulConnection();
        return RxSyncInvocationHandler.sync(redisClusterClient.connectCluster().getStatefulConnection());
    }

    @Test
    public void msetnx() throws Exception {
        redis.set("one", "1");
        Map<String, String> map = Maps.newLinkedHashMap();
        map.put("one", "1");
        map.put("two", "2");
        assertThat(redis.msetnx(map)).isTrue();
        redis.del("one");
        assertThat(redis.msetnx(map)).isTrue();
        assertThat(redis.get("two")).isEqualTo("2");
    }

    @Test
    public void mget() throws Exception {

        redis.set(key, value);
        redis.set("key1", value);
        redis.set("key2", value);

        RedisAdvancedClusterReactiveCommands<String, String> reactive = clusterConnection.reactive();

        Observable<String> mget = reactive.mget(key, "key1", "key2");
        String first = mget.toBlocking().first();
        assertThat(first).isEqualTo(value);
    }

}
