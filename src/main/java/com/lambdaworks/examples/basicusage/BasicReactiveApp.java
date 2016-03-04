package com.lambdaworks.examples.basicusage;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.rx.RedisStringReactiveCommands;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;

import java.util.List;

public class BasicReactiveApp {
    public static void main(String[] args) {
        try {
            RedisClient client = RedisClient.create("redis://localhost");
            RedisStringReactiveCommands<String, String> commands = client.connect().reactive();
            Subscriber<String> subscriber = new Subscriber<String>() {
                @Override
                public void onCompleted() {
                    System.out.println("Completed operation");
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("onError: " + throwable);
                    System.exit(-1);
                }

                @Override
                public void onNext(String s) {
                    System.out.println("get content: " + s);
                }
            };
            Observable<String> set = commands.set("a", "100");
            set.subscribe(subscriber);
            Observable<String> get = commands.get("a");
            get.subscribe(subscriber); // v -> System.out.println("got value: " + v)
            Observable.just("A", "B", "C").flatMap(new Func1<String, Observable<String>>() {
                @Override
                public Observable<String> call(String s) {
                    return commands.get(s);
                }
            }).subscribe(new Action1<String>() {
                @Override
                public void call(String document) {
                    System.out.println("Got value: " + document);
                }
            });
            Observable.just("Ben", "Michael", "Mark").groupBy(new Func1<String, String>() {
                @Override
                public String call(String key) {
                    return key.substring(0, 1);
                }
            }).subscribe(new Action1<GroupedObservable<String, String>>() {
                @Override
                public void call(GroupedObservable<String, String> groupedObservable) {
                    groupedObservable.toList().subscribe(
                            new Action1<List<String>>() {
                                @Override
                                public void call(List<String> strings) {
                                    System.out.println("First character: " + groupedObservable.getKey() + ", elements: " + strings);
                                }
                            });
                }
            });
            Thread.sleep(3000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
