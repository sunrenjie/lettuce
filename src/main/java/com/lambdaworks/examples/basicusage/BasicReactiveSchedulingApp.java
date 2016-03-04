package com.lambdaworks.examples.basicusage;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.api.rx.RedisStringReactiveCommands;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

// Demo of subscribeOn vs. observeOn
// Taken from https://github.com/mp911de/lettuce/wiki/Reactive-API-%284.0%29
// Summary: SubscribeOn() will have all subscribe chain scheduled (exception: in lettuce's case, the observer of
// network response is still on netty's EventLoop, however) , while observeOn() will have the downstream part
// (the observer) scheduled.
public class BasicReactiveSchedulingApp {
    public static void main(String[] args) {
        try {
            RedisClient client = RedisClient.create("redis://localhost");
            RedisStringReactiveCommands<String, String> commands = client.connect().reactive();

            System.out.println("Case 1): no explicit scheduling");
            // Here all threads shall be executed on the main thread, except for the . the 1st flatMap()Given this usage, "Map 1: ..." will be println()'ed by RxJava's computation threads, whereas the
            // "Map 2: ..." part on the netty's NioEventLoop. The reasons are that:
            // 1. The subscribeOn() operator will affect thread choice of the observable chain before it.
            // 2. Lettuce's networking part is based on netty; and netty will use its own thread.
            // See also http://reactivex.io/documentation/operators/subscribeon.html
            Observable.just("Ben", "Michael", "Mark").flatMap(new Func1<String, Observable<String>>() {
                @Override
                public Observable<String> call(String key) {
                    System.out.println("Map 1: " + key + " (" + Thread.currentThread().getName() + ")");
                    return commands.set(key, key);
                }
            }).flatMap(new Func1<String, Observable<String>>() {
                @Override
                public Observable<String> call(String value) {
                    System.out.println("Map 2: " + value + " (" + Thread.currentThread().getName() + ")");
                    return Observable.just(value);
                }
            }).subscribe();
            Thread.sleep(1000);

            System.out.println("Case 2) with subscribeOn()");
            // The official doc http://reactivex.io/documentation/operators/subscribeon.html
            // states that SubscribeOn "specify the Scheduler on which an Observable will operate". In our case here,
            // the whole subscribe chain shall be executed on the scheduler. Therefore, the subscribeOn() part can
            // be moved anywhere in the chain, for example, between the two flatMap() parts (in our wiki's word,
            // 'it does not matter where you add it (the subscribeOn() clause)' ).
            // But there is one exception: the "Map 2: ..." part is executed in netty's NioEventLoop. The wiki page
            // simply states the reason to be that 'the lettuce observables are executed and completed on the netty
            // EventLoop threads by default.' Basically, how this part is scheduled is decided largely by lettuce.
            // Anyway, data from network will be read by netty in netty's EventLoop. The decoding,
            // dispatching-to-client part is lettuce's job. Maybe there are some other reasons leading to this decision
            // (thread switching, performance concerns, compatibility with the normal case, i.e., case 1) above).
            // Otherwise, this disagreement with the specs is surprising.
            Observable.just("Ben", "Michael", "Mark").flatMap(new Func1<String, Observable<String>>() {
                @Override
                public Observable<String> call(String key) {
                    System.out.println("Map 1: " + key + " (" + Thread.currentThread().getName() + ")");
                    return commands.set(key, key);
                }
            }).flatMap(new Func1<String, Observable<String>>() {
                @Override
                public Observable<String> call(String value) {
                    System.out.println("Map 2: " + value + " (" + Thread.currentThread().getName() + ")");
                    return Observable.just(value);
                }
            }).subscribeOn(Schedulers.computation()).subscribe();
            Thread.sleep(1000);

            System.out.println("Case 3) with observeOn()");
            // The official doc http://reactivex.io/documentation/operators/observeon.html
            // states that observeOn() "specify the Scheduler on which an observer will observe this Observable".
            // The wiki describes this part as simply as:
            // "Everything before the observeOn() call is executed in main, everything below in the scheduler."
            // The fact that the "Map 2: ..." part is executed in different thread pool in case 2) and 3) shall only
            // due to lettuce's decision. All network response shall no doubt be dispatch()'ed in netty's EventLoop
            // to lettuce. It's lettuce's decision that which thread is used to deliver the content to the
            // observer. Here, however, lettuce's behavior accords with the specs.

            Observable.just("Ben", "Michael", "Mark").flatMap(new Func1<String, Observable<String>>() {
                @Override
                public Observable<String> call(String key) {
                    System.out.println("Map 1: " + key + " (" + Thread.currentThread().getName() + ")");
                    return commands.set(key, key);
                }
            }).observeOn(Schedulers.computation()).flatMap(new Func1<String, Observable<String>>() {
                @Override
                public Observable<String> call(String value) {
                    System.out.println("Map 2: " + value + " (" + Thread.currentThread().getName() + ")");
                    return Observable.just(value);
                }
            }).subscribe();
            Thread.sleep(1000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
