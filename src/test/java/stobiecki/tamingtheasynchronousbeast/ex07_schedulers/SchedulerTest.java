package stobiecki.tamingtheasynchronousbeast.ex07_schedulers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.*;

@Slf4j
public class SchedulerTest {

    @Test
    public void withoutSchedulers() {
        Flux<Long> observable = createSlowObservable();//<-- explain how slow observable works

        Flux<String> slowObservableWithOperations = observable
                .map(this::slowIdentity)//<-- explain how slowIdentity works
                .map(this::slowToString);//<-- explain how slowToString works

        //this is blocking, why?
        slowObservableWithOperations.subscribe(value -> log.info("Value from slow observable {}", value));
    }

    private Flux<Long> createSlowObservable() {
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







    @Test
    public void withoutSchedulersTwoSubscribers() {
        Flux<Long> observable = createSlowObservable();

        Flux<String> slowObservableWithOperations = observable
                .map(this::slowIdentity)
                .map(this::slowToString);

        //what is expected test execution time?
        slowObservableWithOperations.subscribe(value -> log.info("Value from subscriber ONE {}", value));
        slowObservableWithOperations.subscribe(value -> log.info("Value from subscriber TWO {}", value));
    }







    //todo
    @Test
    public void connectableObservable() {
        Flux<Long> observable = createSlowObservable();

        ConnectableFlux<String> connectableObservable = observable
                .map(this::slowIdentity)//<-- how many times slowIdentity will be executed?
                .map(this::slowToString)//<-- how many times slowToString will be executed?
                .publish();// <-- here connectable observable is created

        log.info("Add subscribers");
        connectableObservable.subscribe(value -> log.info("Value from subscriber ONE {}", value));
        connectableObservable.subscribe(value -> log.info("Value from subscriber TWO {}", value));

        log.info("Two subscribers added, now let's connect");
        connectableObservable.connect();//<-- and what is expected test execution time?
    }







    //todo
    @Test
    public void hotConnectableObservable() {
        Flux<Long> observable = createSlowObservable();

        ConnectableFlux<String> connectableObservable = observable
                .map(this::slowIdentity)//<-- how many times slowIdentity will be executed?
                .map(this::slowToString)//<-- how many times slowToString will be executed?
                .publish();// <-- here connectable observable is created


        log.info("Connect called before two subscriber added.");
        connectableObservable.connect();

        log.info("Two subscriber will be added");
        //How many events subscriber receive
        connectableObservable.subscribe(value -> log.info("Value from subscriber ONE {}", value));
        connectableObservable.subscribe(value -> log.info("Value from subscriber TWO {}", value));
    }




    @Test
    public void shouldSubscribeOnScheduler() {
        Flux<Long> observable = createSlowObservable();

        Flux<String> observableWithTransformations = observable
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.boundedElastic());//<-- let's run in in separate thread

        //what does it print?
        observableWithTransformations.subscribe(value -> log.info("Get value {}", value));
    }




    @Test
    public void shouldSubscribeOnScheduler2() {
        Flux<Long> observable = createSlowObservable();

        Flux<String> observableWithTransformations = observable
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.boundedElastic());//<-- let's run in in separate thread

        //what does it print?
        observableWithTransformations
                .doOnNext(value -> log.info("Get value {}", value))
                .blockLast();
    }



    @Test
    public void twoConcurrentSubscriptions() {
        Flux<Long> observable = createSlowObservable();

        Flux<String> observableWithTransformations = observable
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.boundedElastic());//<-- let's run in in separate thread

        //how many thread can you see
        observableWithTransformations.subscribe(value -> log.info("Subscriber ONE {}", value));
        observableWithTransformations.subscribe(value -> log.info("Subscriber TWO {}", value));
        sleep(10000);
    }



    @Test
    public void manyInvocationOfSubscribeOnIsPointless() {
        Flux<Long> observable = createSlowObservable();

        Flux<String> observableWithTransformations = observable
                .subscribeOn(Schedulers.parallel())//<-- Run it on computation scheduler
                .map(this::slowIdentity)
                .subscribeOn(Schedulers.newSingle("single"))//<-- Run it on New Thread scheduler
                .map(this::slowToString)
                .subscribeOn(Schedulers.elastic());//<-- Run it on IO scheduler

        //which schedulers are used?
        observableWithTransformations
                .doOnNext(value -> log.info("Got value {}", value))
                .blockLast();
    }



    @Test
    public void shouldPublishOnOtherScheduler() {
        Flux<Long> observable = createSlowObservable();

        Flux<String> observableWithTransformations = observable
                .publishOn(Schedulers.parallel())//<-- Observe on computation scheduler
                .map(this::slowIdentity)
                .publishOn(Schedulers.newSingle("single"))//<-- Run it on New Thread scheduler
                .map(this::slowToString)
                .subscribeOn(Schedulers.elastic());//<-- Run it on IO scheduler

        //which schedulers are used?
        observableWithTransformations
                .doOnNext(value -> log.info("Got value {}", value))
                .blockLast();
    }



    @Test
    public void shouldExecuteBlockingGet() {
        Flux<Long> observable = createSlowObservable();

        Flux<String> observableWithTransformations = observable
                .map(this::slowIdentity)
                .map(this::slowToString)
                .subscribeOn(Schedulers.elastic());//<-- run it on IO scheduler

        //what does it print, in which thread?
        observableWithTransformations
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

    @Test
    public void eventsEmittedTooQuickly() {
        Flux.interval(Duration.ofMillis(1), Schedulers.elastic())//<-- emits 1000 event per second
                .take(5)
                .map(this::slowIdentity)//<-- takes one second
                .map(this::slowToString)//<-- takes one second
                .doOnNext(value -> log.info("How many message per sec will be printed? Is it better?"))
                .blockLast();
    }

    @Test
    public void eventsEmittedTooQuickly2() {
        Flux.interval(Duration.ofMillis(1), Schedulers.newElastic("elastic1"))//<-- emits 1000 event per second
                .take(5)
                .publishOn(Schedulers.newElastic("elastic2")) //<-- scheduler added
                .map(this::slowIdentity)
                .map(this::slowToString)
                .doOnNext(value -> log.info("How many message per sec will be printed? Is it better?"))
                .blockLast();
    }

    @Test
    public void eventsEmittedTooQuickly3IdAnyBetterWithFlatMap() {
        Flux.interval(Duration.ofMillis(1), Schedulers.elastic())//<-- emits 1000 event per second
                .take(5)
                .publishOn(Schedulers.elastic()) //<-- scheduler added
                .flatMap(number -> Mono.just(slowIdentity(number)))
                .flatMap(number -> Mono.just(slowToString(number)))
                .doOnNext(value -> log.info("How many message per sec will be printed? Is it better?"))
                .blockLast();
    }

    @Test
    public void eventsEmittedTooQuickly4FlatMapAndSubscribeOn() {
        Flux.interval(Duration.ofMillis(1), Schedulers.parallel())//<-- emits 1000 event per second
                .take(5)
                .flatMap(value -> Mono.just(value).map(this::slowIdentity).subscribeOn(Schedulers.elastic()))//<-- flatMap + subscribeOn
                .flatMap(value -> Mono.just(value).map(this::slowToString).subscribeOn(Schedulers.elastic()))//<-- flatMap + subscribeOn
                //^^^ this is declarative concurrency
                .blockLast();
    }

    @Test
    public void withoutConcurrency() {
        log.info("Test start");
        Flux.just(1, 2, 3)
                .flatMap(this::findUserBadDesignMethod)//<-- explain what findUserBadDesignMethod does
                .subscribe(user -> log.info("How much time does it takes to get all 3 users? '{}'", user));
        //test execution takes about 9 second
    }

    private Flux<String> findUserBadDesignMethod(long id) {
        log.info("Long findUserBadDesignMethod method started. In which thread method is executed?");
        //this method simulated slow database query or rest service invocation
        sleep(3000);//<-- every long operation should be executed in Publisher.create in onSubscribe callback.
        //// Here is not, but it a way to convert legacy API into reactive
        return Flux.just(String.format("User with id %d", id));
    }

    @Test
    public void ownBadScheduler() {
        Scheduler scheduler = Schedulers.fromExecutor(Executors.newCachedThreadPool());//<-- own scheduler

        Flux<Long> slowObservable = createSlowObservable();

        slowObservable
                .subscribeOn(scheduler)
                .doOnNext(value -> log.info("Get value {}", value))
                .blockLast();
    }

    //todo
    @Test
    public void ownGoodScheduler(){
        /*@formatter:off*/
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(false)
                .setNameFormat("custom-descriptive-thread-name" + "-%d")//<-- very important, present in logs and JMX
                .setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception in Reactor scheduler thread {}", t.getName(), e))//<-- all exceptions must be logged.
                .build();
        ExecutorService executorService = new ThreadPoolExecutor(3,//<-- three thread almost always available
                15,//<-- do not create more then 15 threads
                30, TimeUnit.SECONDS,//<-- release thread after 30s if there is not task for it
                new LinkedBlockingQueue<>(15),//<-- if no thread available submit no more then 15 task in non blocking fashion
                new ThreadFactoryLoggableWrapper(threadFactory),//<-- log thread creation
                new ThreadPoolExecutor.CallerRunsPolicy());//<-- sometime it is better to return error
        /*@formatter:on*/
        Scheduler goodScheduler = Schedulers.fromExecutor(executorService);//here is good scheduler<--

        Flux<Long> slowObservable = createSlowObservable();

        slowObservable
                .subscribeOn(goodScheduler)
                .doOnNext(value -> log.info("Get value {}", value))
                .blockLast();
    }

    @RequiredArgsConstructor
    private static class ThreadFactoryLoggableWrapper implements ThreadFactory{

        private final ThreadFactory threadFactory;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = threadFactory.newThread(r);
            log.debug("New thread '{}' created for Reactive scheduler", thread.getName());
            return thread;
        }
    }
}
