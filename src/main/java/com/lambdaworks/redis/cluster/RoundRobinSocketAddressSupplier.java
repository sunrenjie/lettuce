package com.lambdaworks.redis.cluster;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.function.Function;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.lambdaworks.redis.cluster.models.partitions.RedisClusterNode;

/**
 * Round-Robin socket address supplier. Cluster nodes are iterated circular/infinitely.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class RoundRobinSocketAddressSupplier implements Supplier<SocketAddress> {

    private final Collection<RedisClusterNode> partitions;
    private final Collection<RedisClusterNode> clusterNodes = Lists.newArrayList();
    private final Function<Collection<RedisClusterNode>, Collection<RedisClusterNode>> sort;
    private RoundRobin<? extends RedisClusterNode> roundRobin;

    public RoundRobinSocketAddressSupplier(Collection<RedisClusterNode> partitions,
            Function<Collection<RedisClusterNode>, Collection<RedisClusterNode>> sort) {
        this.sort = sort;
        checkArgument(partitions != null, "Partitions must not be null");
        checkArgument(sort != null, "Sort-Function must not be null");
        this.partitions = partitions;
        this.clusterNodes.addAll(partitions);
        this.roundRobin = new RoundRobin<>(clusterNodes);
        resetRoundRobin();
    }

    @Override
    public SocketAddress get() {
        if (!clusterNodes.containsAll(partitions)) {
            resetRoundRobin();
        }

        RedisClusterNode redisClusterNode = roundRobin.next();
        return getSocketAddress(redisClusterNode);

    }

    protected void resetRoundRobin() {
        clusterNodes.clear();
        clusterNodes.addAll(sort.apply(partitions));
        roundRobin.offset = null;
    }

    protected static SocketAddress getSocketAddress(RedisClusterNode redisClusterNode) {
        return redisClusterNode.getUri().getResolvedAddress();
    }
}