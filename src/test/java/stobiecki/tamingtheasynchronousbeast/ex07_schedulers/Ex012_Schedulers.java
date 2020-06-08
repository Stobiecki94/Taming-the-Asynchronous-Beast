package stobiecki.tamingtheasynchronousbeast.ex07_schedulers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Ex012_Schedulers {

    /**
     * Reactor uses a Scheduler as a contract for arbitrary task execution. It provides some guarantees required by Reactive Streams flows like FIFO execution.
     *
     * You can use or create efficient schedulers to jump thread on the producing flows (subscribeOn) or receiving flows (publishOn):
     */

    @Test
    public void withoutSchedulers() {
        Flux<Long> publisher = createSlowPublisher();//<-- explain how slow observable works

        Flux<String> slowPublisherWithOperations = publisher
                .map(this::slowIdentity)//<-- explain how slowIdentity works
                .map(this::slowToString);//<-- explain how slowToString works

        //this is blocking, why?
        slowPublisherWithOperations.subscribe(value -> log.info("Value from slow publisher {}", value));
    }

    @Test
    public void withoutSchedulersTwoSubscribers() {
        Flux<Long> publisher = createSlowPublisher();

        Flux<String> slowPublisherWithOperations = publisher
                .map(this::slowIdentity)
                .map(this::slowToString);

        //what is expected test execution time?
        slowPublisherWithOperations.subscribe(value -> log.info("Value from subscriber ONE {}", value));
        slowPublisherWithOperations.subscribe(value -> log.info("Value from subscriber TWO {}", value));
    }

    @Test
    public void shouldSubscribeOnScheduler() {
        Flux<Long> publisher = createSlowPublisher();

        Flux<String> slowPublisherWithOperations = publisher
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.boundedElastic());//<-- let's run in in separate thread

        //what does it print?
        slowPublisherWithOperations.subscribe(value -> log.info("Get value {}", value));
    }

    @Test
    public void shouldSubscribeOnScheduler2() {
        Flux<Long> publisher = createSlowPublisher();

        Flux<String> slowPublisherWithOperations = publisher
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.boundedElastic());//<-- let's run in in separate thread

        //what does it print?
        slowPublisherWithOperations
                .doOnNext(value -> log.info("Get value {}", value))
                .blockLast();
    }

    @Test
    public void twoConcurrentSubscriptions() {
        Flux<Long> publisher = createSlowPublisher();

        Flux<String> slowPublisherWithOperations = publisher
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.boundedElastic());//<-- let's run in in separate thread

        //how many thread can you see
        slowPublisherWithOperations.subscribe(value -> log.info("Subscriber ONE {}", value));
        slowPublisherWithOperations.subscribe(value -> log.info("Subscriber TWO {}", value));
        sleep(10000);
    }

    @Test//todo hot? move to HOT!? or maybe stay here?
    public void twoConcurrentSubscriptionsToHotSource() {
        Flux<Long> publisher = createSlowPublisher();

        ConnectableFlux<String> slowPublisherWithOperations = publisher
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.boundedElastic())//<-- let's run in in separate thread
                .publish();

        //how many thread can you see
        slowPublisherWithOperations.subscribe(value -> log.info("Subscriber ONE {}", value));
        slowPublisherWithOperations.subscribe(value -> log.info("Subscriber TWO {}", value));

        slowPublisherWithOperations.connect();
        sleep(10000);
    }

    @Test
    public void shouldPublishOnOtherScheduler() {
        Flux<Long> publisher = createSlowPublisher();

        Flux<String> slowPublisherWithOperations = publisher
                .publishOn(Schedulers.parallel())//<-- Observe on computation scheduler
                .map(this::slowIdentity)
                .publishOn(Schedulers.newSingle("single"))//<-- Run it on New Thread scheduler
                .map(this::slowToString)
                .subscribeOn(Schedulers.elastic());//<-- Run it on IO scheduler

        //which schedulers are used?
        slowPublisherWithOperations
                .doOnNext(value -> log.info("Got value {}", value))
                .blockLast();
    }

    @Test
    public void shouldExecuteBlockingGet() {
        Flux<Long> publisher = createSlowPublisher();

        Flux<String> slowPublisherWithOperations = publisher
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.elastic());//<-- run it on IO scheduler

        //what does it print, in which thread?
        slowPublisherWithOperations
                .doOnNext(value -> log.info("Get value {} in blocking manner.", value))
                .blockLast();
        //there is plenty of blocking... method
    }

    @Test
    public void manyMethodAcceptScheduler() {
        Flux.interval(Duration.ofMillis(100), Schedulers.elastic()) //<-- use provided scheduler
                .timeout(Duration.ofSeconds(10), Schedulers.parallel())//<-- use provided scheduler
                .take(3)
                .doOnNext(value -> log.info("Got value {}", value))
                .blockLast();
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

    @SneakyThrows
    private void sleep(long timeout) {
        TimeUnit.MILLISECONDS.sleep(timeout);
    }
}
