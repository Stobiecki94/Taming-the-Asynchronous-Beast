package stobiecki.tamingtheasynchronousbeast.ex07_schedulers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;

@Slf4j
public class Ex03_threadingAndSchedulers {

    //@formatter:off
    /**
     * NOTE:
     * Reactor can be considered to be concurrency-agnostic.
     * That is, it does not enforce a concurrency model. Rather, it leaves you, the developer, in command.
     * However, that does not prevent the library from helping you with concurrency.
     *
     * Obtaining a Flux or a Mono does not necessarily mean that it runs in a dedicated Thread.
     * Instead, most operators continue working in the Thread on which the previous operator executed.
     *
     * Unless specified, the topmost operator (the source) itself runs on the Thread in which the subscribe() call was made.
     *
     */
    //@formatter:on
    @Test
    @SneakyThrows
    public void thread() {
        final Mono<String> mono = Mono.just("hello");

        Thread thread = new Thread(() -> mono
                .map(msg -> msg + " thread ")
                .subscribe(v ->
                        System.out.println(v + Thread.currentThread().getName())
                )
        );
        thread.start();
        thread.join();
    }

    /**
     * While boundedElastic is made to help with legacy blocking code if it cannot be avoided, single and parallel are not.
     * As a consequence, the use of Reactor blocking APIs (block(), blockFirst(), blockLast() (as well as iterating over toIterable() or toStream()) inside the default single and parallel schedulers) results in an IllegalStateException being thrown.
     */
    @Test
    public void block_boundedElastic() {
        Mono<Boolean> blockInsideBoundedElastic = Mono.just(true)
                .map(ignore -> Mono.just(true).subscribeOn(Schedulers.elastic()).block())
                .subscribeOn(Schedulers.boundedElastic());

        StepVerifier.create(blockInsideBoundedElastic)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @SneakyThrows
    public void block_single() {
        Mono<Boolean> blockInsideSingle = Mono.just(true)
                .map(ignore -> Mono.just(true).subscribeOn(Schedulers.elastic()).block())
                .subscribeOn(Schedulers.single());

        StepVerifier.create(blockInsideSingle)
                .expectErrorMatches(error -> error instanceof IllegalStateException && error.getMessage().equals("block()/blockFirst()/blockLast() are blocking, which is not supported in thread single-1"))
                .verify();
    }

    @Test
    public void block_parallel() {
        Mono<Boolean> blockInsideParallel = Mono.just(true)
                .map(ignore -> Mono.just(true).subscribeOn(Schedulers.elastic()).block())
                .subscribeOn(Schedulers.parallel());

        StepVerifier.create(blockInsideParallel)
                .expectErrorMatches(error -> error instanceof IllegalStateException && error.getMessage().equals("block()/blockFirst()/blockLast() are blocking, which is not supported in thread parallel-1"))
                .verify();
    }

    /**
     * Some operators use a specific scheduler from Schedulers by default (and usually give you the option of providing a different one).
     * <p>
     * For instance, calling the Flux.interval(Duration.ofMillis(300)) factory method produces a Flux<Long> that ticks every 300ms.
     * By default, this is enabled by Schedulers.parallel(). The following line changes the Scheduler to a new instance similar to Schedulers.single():
     */
    @Test
    public void changeDefaultScheduler() {
        log.info("Default scheduler");
        Flux.interval(Duration.ofMillis(300))
                .log()
                .blockFirst();

        log.info("My scheduler");
        Flux.interval(Duration.ofMillis(300), Schedulers.newSingle("test"))
                .log()
                .blockFirst();
    }

    /**
     * https://projectreactor.io/docs/core/release/reference/#_the_publishon_method
     */
    @Test
    public void publishOn() {
        Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 4);

        final Mono<String> flux = Mono.just("initialValue")
                .doOnNext(log::info)
                .map(i -> "next value")
                .doOnNext(log::info)
                .publishOn(scheduler)
                .doOnNext(log::info)
                .map(i -> "next next value");

        new Thread(() -> flux.subscribe(log::info))
                .start();

        sleepForAWhile();
    }

    /**
     *
     */
    @Test
    public void subscribeOn() {
        Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 4);

        final Mono<String> flux = Mono.just("initialValue")
                .doOnNext(log::info)
                .map(i -> "next value")
                .doOnNext(log::info)
                .map(i -> "next next value")
                .subscribeOn(scheduler);

        new Thread(() -> flux.subscribe(log::info))
                .start();

        sleepForAWhile();
    }

    @Test
    public void multipleSubscribeOn() {

        final Mono<String> flux = Mono.just("value")
                .subscribeOn(Schedulers.newSingle("the earliest"))
                .subscribeOn(Schedulers.newSingle("the latest"));

        new Thread(() -> flux.subscribe(log::info)) // <-- Only the earliest subscribeOn call in the chain is actually taken into account.
                .start();

        sleepForAWhile();
    }

    @SneakyThrows
    private void sleepForAWhile() {
        Thread.sleep(100);
    }
}
