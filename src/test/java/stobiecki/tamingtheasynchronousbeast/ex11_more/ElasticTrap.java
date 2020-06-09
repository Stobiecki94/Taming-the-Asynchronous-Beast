package stobiecki.tamingtheasynchronousbeast.ex11_more;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
public class ElasticTrap {

    @Test
    @SneakyThrows
    public void elasticTrap() {
        Flux<Integer> fluxAsyncBackp = Flux.create(emitter -> {

            // Publish 1000 numbers
            for (int i = 0; i < 1_000; i++) { // <-- change
                emitter.next(i);
            }
            // When all values or emitted, call complete.
            emitter.complete();
        }, FluxSink.OverflowStrategy.IGNORE);

        fluxAsyncBackp
                .subscribeOn(Schedulers.newElastic("publisherThread"))
                .flatMap(this::someOperationOnElasticThread)
                .blockLast();
    }

    private Mono<String> someOperationOnElasticThread(int index) {
        return Mono.fromCallable(() -> {
            System.out.println(Thread.currentThread().getName() + " | operation = " + index);
            Thread.sleep(30_000);
            return String.valueOf(index);
        })
                .subscribeOn(Schedulers.elastic());
    }

    @Test
    @SneakyThrows
    public void elasticTrapFix() {
        LinkedBlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>(10_000);
        RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        ExecutorService executorService = new ThreadPoolExecutor(1000, 1000, 0L, MILLISECONDS, taskQueue, Executors.defaultThreadFactory(), rejectedExecutionHandler);
        Scheduler scheduler = Schedulers.fromExecutor(executorService);


        Flux<Integer> fluxAsyncBackp = Flux.create(emitter -> {

            // Publish 10_000 numbers
            for (int i = 0; i < 10_000; i++) {
                emitter.next(i);
            }
            // When all values or emitted, call complete.
            emitter.complete();
        }, FluxSink.OverflowStrategy.IGNORE);

        fluxAsyncBackp
                .subscribeOn(Schedulers.newElastic("publisherThread"))
                .flatMap(index -> someOperationOnElasticThread(index, scheduler))
                .blockLast();
    }

    private Mono<String> someOperationOnElasticThread(int index, Scheduler scheduler) {
        return Mono.fromCallable(() -> {
            System.out.println(Thread.currentThread().getName() + " | operation = " + index);
            Thread.sleep(30_000);
            return String.valueOf(index);
        })
                .subscribeOn(scheduler);
    }

}
