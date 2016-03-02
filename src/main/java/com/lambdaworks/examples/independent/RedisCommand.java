package com.lambdaworks.examples.independent;

import java.util.concurrent.CompletableFuture;

public class RedisCommand extends CompletableFuture<String>  {
    private final RedisClientHandler commandHandler;

    public RedisCommand(RedisClientHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    void dispatch(String command, String key, String value) {
        commandHandler.dispatch(command, key, value, this);
    }
}
