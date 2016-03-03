package com.lambdaworks.examples.independent;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class RedisClient {
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "6379"));

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            RedisClientInitializer init = new RedisClientInitializer();
            b.group(group).channel(NioSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(init);
            Channel ch = b.connect(HOST, PORT).sync().channel();
            ChannelFuture lastWriteFuture = null;
            RedisCommand cmd = new RedisCommand(init.getClientHandler());
            cmd.dispatch("get", "a", "");
            Thread.sleep(1000);
            System.out.println("command 'get a' result: " + cmd.get());
            cmd = new RedisCommand(init.getClientHandler());
            cmd.dispatch("set", "b", "5");
            Thread.sleep(1000);
            System.out.println("command 'set b 5' result:" + cmd.get());
            System.exit(0);
            // telnet-like UI
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (;;) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                lastWriteFuture = ch.writeAndFlush(line + "\r\n");

                if ("exit".equals(line.toLowerCase())) {
                    ch.closeFuture().sync();
                    break;
                }
            }

            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } catch (Exception e) {
            group.shutdownGracefully();
        }
    }
}
