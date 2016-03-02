package com.lambdaworks.examples.independent;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

class RedisClientHandler extends ChannelDuplexHandler {
    private RedisCommand command;
    protected Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        String msg = (String) obj;
        System.err.println("channelRead: " + msg);
        if (command != null) {
            command.complete(msg);
            command = null;
        }
    }

    public void dispatch(String command, String key, String value, RedisCommand cmd) {
        assert(this.command == null);
        this.command = cmd;
        channel.writeAndFlush(command + " " + key + " " + value + "\r\n");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
