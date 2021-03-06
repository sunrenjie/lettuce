package com.lambdaworks.redis.protocol;

import java.util.concurrent.CountDownLatch;

/**
 * A wrapper for commands within a {@literal MULTI} transaction. Commands triggered within a transaction will be completed
 * twice. Once on the submission and once during {@literal EXEC}. Only the second completion will complete the underlying
 * command.
 * 
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @param <T> Command output type.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class TransactionalCommand<K, V, T> extends AsyncCommand<K, V, T> implements RedisCommand<K, V, T> {

    public TransactionalCommand(RedisCommand<K, V, T> command) {
        super(command);
        latch = new CountDownLatch(2);
    }

}
