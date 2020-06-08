package stobiecki.tamingtheasynchronousbeast.ex06_hot_vs_cold;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Ex02_ConnectableFlux {

    @Test
    public void connectableObservable() {
        Flux<Long> publisher = createSlowPublisher();

        ConnectableFlux<String> connectablePublisher = publisher
                .map(this::slowIdentity)//<-- how many times slowIdentity will be executed?
                .map(this::slowToString)//<-- how many times slowToString will be executed?
                .publish();// <-- here connectable observable is created

        log.info("Add subscribers");
        connectablePublisher.subscribe(value -> log.info("Value from subscriber ONE {}", value));
        connectablePublisher.subscribe(value -> log.info("Value from subscriber TWO {}", value));

        log.info("Two subscribers added, now let's connect");
        connectablePublisher.connect();//<-- and what is expected test execution time?
    }

    @Test
    public void hotConnectableObservable() {
        Flux<Long> publisher = createSlowPublisher();

        ConnectableFlux<String> connectablePublisher = publisher
                .map(this::slowIdentity)//<-- how many times slowIdentity will be executed?
                .map(this::slowToString)//<-- how many times slowToString will be executed?
                .publish();// <-- here connectable observable is created


        log.info("Connect called before two subscriber added.");
        connectablePublisher.connect();

        log.info("Two subscriber will be added");
        //How many events subscriber receive
        connectablePublisher.subscribe(value -> log.info("Value from subscriber ONE {}", value));
        connectablePublisher.subscribe(value -> log.info("Value from subscriber TWO {}", value));
    }

    private Flux<Long> createSlowPublisher() {
        return Flux.create(emitter -> {
            for (long i = 0; i < 3; ++i) {
                sleep(1000);
                log.info("Next value {} will be emitted.", i);
                emitter.next(i);
            }
            sleep(1000);
            emitter.complete();
            log.info("Slow observable completed");
        });
    }

    private Long slowIdentity(Long value) {
        sleep(1000);
        log.info("Slow identity transformation executed for value {}", value);
        return value;
    }

    private String slowToString(Long value) {
        sleep(1000);
        log.info("Slow to string executed for value {}", value);
        return value.toString();
    }


    @Test
    public void twoSubscribersWithAndWithoutBackPressure()  {
        Flux<Integer> flowable = Flux.range(1, 10)
                .publish()//<-- subject is created! (ConnectableFlux)
                .refCount()//<-- lets connect on first subscriber
                .subscribeOn(Schedulers.elastic());//<-- and run it on another thread

        flowable.subscribe(new Subscriber<Integer>() {

            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
                this.subscription = s;
            }

            @SneakyThrows
            @Override
            public void onNext(Integer integer) {
                log.info("With back pressure {}, wait 250 ms for next event", integer);
                sleep(250);
                subscription.request(1);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Oops, id something missing?", t);
            }

            @Override
            public void onComplete() {
                log.info("Complete :)");
            }
        });

        flowable.subscribe(integer -> log.info("Subscriber _without_ back pressure, {}", integer));
        //^^^ another subscriber

        sleep(3000);
    }

    @SneakyThrows
    private void sleep(long timeout) {
        TimeUnit.MILLISECONDS.sleep(timeout);
    }

}
