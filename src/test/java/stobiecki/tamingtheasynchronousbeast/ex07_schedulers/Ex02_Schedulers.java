package stobiecki.tamingtheasynchronousbeast.ex07_schedulers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Ex02_Schedulers {

    @Test
    @SneakyThrows
    public void publishOnAndSubscribeOn() {
        CountDownLatch completionSignal = new CountDownLatch(1);
        Flux.just("Apple", "Banana")
                .doOnNext(next -> log.info("before publishOn {}", next))
                .publishOn(Schedulers.newSingle("publishOn"))
                .doOnNext(next -> log.info("after publishOn {}", next))
                .subscribeOn(Schedulers.newSingle("subscribeOn"))
                .doOnNext(next -> log.info("after publishOn {}", next))
                .subscribe(next -> {
                }, error -> log.error("Error", error), completionSignal::countDown);

        boolean awaitASecond = completionSignal.await(1, TimeUnit.SECONDS);
        assertThat(awaitASecond).withFailMessage("Await timed out!").isTrue();
    }

    /**
     * Basic example to show that Reactor
     * by itself will run the pipeline or code
     * on the same thread on which .subscribe() happened.
     */
    @Test
    public void publisherOnMainThread() {
        Flux.range(1, 5)
                .flatMap(a -> Mono.just(blockingGetInfo(a)))
                .subscribe(System.out::println); // <-- This will run the code in main thread
    }

    // This will run the code in a separate thread
    @Test
    public void publisherOnSeparateThread() throws InterruptedException {
        Thread t = new Thread(() -> {
            Mono.just("Hello")
                    .subscribe(log::info);
        });
        t.start();
        t.join();
    }

    /**
     * Adding a Scheduler puts the workload
     * of the main thread and hands it over to the
     * Scheduler orchestration.
     */
    @Test
    @SneakyThrows
    public void publishOn() {
        CountDownLatch completionSignal = new CountDownLatch(1);

        Flux.range(1, 5)
                .publishOn(Schedulers.elastic())
                .flatMap(a -> Mono.just(blockingGetInfo(a)))
                .subscribe(System.out::println, error -> System.out.println("Error " + error.getMessage()), completionSignal::countDown);

        boolean await10Second = completionSignal.await(10, TimeUnit.SECONDS);
        assertThat(await10Second).withFailMessage("Await timed out!").isTrue();
    }


    /**
     * Parallel Flux which would runOn a Scheduler and will be submitted as parallel
     * tasks depending on the .parallel() inputs
     * By default it creates parallism of the same
     * number of cores you have
     */
    @Test
    @SneakyThrows
    public void parallelWithRunOn() {
        CountDownLatch completionSignal = new CountDownLatch(1);
        Flux.range(1, 5)
                .parallel() // <- cat /proc/cpuinfo | grep processor | wc -l
                .runOn(Schedulers.parallel())
                .flatMap(a -> Mono.just(blockingGetInfo(a)))
                .sequential()
                .subscribe(System.out::println, error -> System.out.println("Error " + error.getMessage()), completionSignal::countDown);

        boolean await10Second = completionSignal.await(10, TimeUnit.SECONDS);
        assertThat(await10Second).withFailMessage("Await timed out!").isTrue();
    }

    @Test
    @SneakyThrows
    public void parallelWithRunOn2() {
        CountDownLatch completionSignal = new CountDownLatch(2);
        Flux.range(1, 5)
                .parallel(2)
                .runOn(Schedulers.boundedElastic())
                .flatMap(a -> Mono.just(blockingGetInfo(a)))
                .subscribe(System.out::println, error -> System.out.println("Error " + error.getMessage()), completionSignal::countDown);

        boolean await10Second = completionSignal.await(10, TimeUnit.SECONDS);
        assertThat(await10Second).withFailMessage("Await timed out!").isTrue();
    }


    /**
     * customer thread pool -> still Flux.paraller
     */
    @Test
    @SneakyThrows
    public void parallelWithCustomThreadPool() {
        CountDownLatch completionSignal = new CountDownLatch(1);

        ExecutorService myPool = Executors.newFixedThreadPool(10);

        Flux.range(1, 5)
                .parallel()
                .runOn(Schedulers.fromExecutorService(myPool))
                .flatMap(a -> Mono.just(blockingGetInfo(a)))
                .sequential()
                .subscribe(System.out::println, error -> System.out.println("Error " + error.getMessage()), completionSignal::countDown);

        boolean await10Second = completionSignal.await(10, TimeUnit.SECONDS);
        assertThat(await10Second).withFailMessage("Await timed out!").isTrue();
    }


    /**
     * passing a parallelism number to the
     * parallel() method then that many
     * Parallel Flux will be created from the Schedulers
     */
    @Test
    @SneakyThrows
    public void parallelWithSpecifiedParallelism() {
        CountDownLatch completionSignal = new CountDownLatch(1);

        ExecutorService myPool = Executors.newFixedThreadPool(5);
        Flux.range(1, 5)
                .parallel(5)
                .runOn(Schedulers.fromExecutorService(myPool))
                .flatMap(a -> Mono.just(blockingGetInfo(a)))
                .sequential()
                .subscribe(System.out::println, error -> System.out.println("Error " + error.getMessage()), completionSignal::countDown);

        boolean await10Second = completionSignal.await(10, TimeUnit.SECONDS);
        assertThat(await10Second).withFailMessage("Await timed out!").isTrue();
    }


    @Test
    @SneakyThrows
    public void parallelFinal() {
        CountDownLatch completionSignal = new CountDownLatch(1);

        Flux.range(1, 10)
                .flatMap(this::getInfoCallable)
                .subscribe(System.out::println, error -> System.out.println("Error " + error.getMessage()), completionSignal::countDown);

        boolean await10Second = completionSignal.await(10, TimeUnit.SECONDS);
        assertThat(await10Second).withFailMessage("Await timed out!").isTrue();

    }

    public Mono<String> getInfoCallable(Integer a) {
        // Returns a non-blocking Publisher with a Single Value (Mono)
        return Mono
                .fromCallable(() -> blockingGetInfo(a)) // Define blocking call
                .subscribeOn(Schedulers.elastic()); // Define the execution model
    }

    /**
     * Get the info about the current code and time
     * This is a generic Example of a Blocking call
     * In reality it can be any Blocking call
     */
    public String blockingGetInfo(Integer input) {
        delay();
        return String.format("[%d] on thread [%s] at time [%s]",
                input,
                Thread.currentThread().getName(),
                ZonedDateTime.now());
    }

    // Just simulate some delay on the current thread.
    public void delay() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
