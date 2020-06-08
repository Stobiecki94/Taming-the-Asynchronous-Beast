package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.*;

@Slf4j
public class Ex03_FutureAndCompleatbleFuture {

    /**
     * Future - Java 1.5 (2005), placeholder for a result, BUT: no notify on completion, no chaining, no exception handling....
     */
    @Test
    public void future() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<String> future = executorService.submit(() -> "result");

        String result = future.get();//Blocking
    }

    /**
     * CompletableFuture - Java 8 (2014), Futures + missing features, inspired by Google's Listenable Futures
     */
    @Test
    public void completableFuture() {
        CompletableFuture
                .supplyAsync(() -> 1)
                .thenApply(result -> result * 2)
                .thenCompose(result -> toStringAsynFun(result))
                .exceptionally(ex -> handle(ex));
    }

    @Test
    public void composition_CompletableFuture() {
        CompletableFuture<String> future1 = toStringAsynFun(1);
        CompletableFuture<String> future2 = toStringAsynFun(2);
        CompletableFuture<String> future3 = toStringAsynFun(3);
        CompletableFuture<String> future4 = toStringAsynFun(4);
        CompletableFuture<String> future5 = toStringAsynFun(5);
        CompletableFuture<String> future6 = toStringAsynFun(6);

        String compositionResult = future1.thenCompose(result1 -> future2
                .thenCompose(result2 -> future3
                        .thenCompose(result3 -> future4
                                .thenCompose(result4 -> future5
                                        .thenCompose(result5 -> future6
                                                .thenApply(result6 -> result1 + result2 + result3 + result4 + result5 + result6))))))
                .join();
        log.info("Composition result {}", compositionResult);
    }


    /**
     * Reactor offers rich composition options, wherein code mirrors the organization of the abstract process,
     * and everything is generally kept at the same level (nesting is minimized).
     */
    @Test
    public void combination_ReactorApproach() {
        String compositionResult = Mono.zip(
                toStringReactiveFun(1),
                toStringReactiveFun(2),
                toStringReactiveFun(3),
                toStringReactiveFun(4),
                toStringReactiveFun(5),
                toStringReactiveFun(6)
        )
                .map(tuple -> tuple.getT1() + tuple.getT2() + tuple.getT3() + tuple.getT4() + tuple.getT5() + tuple.getT6())
                .block();

        log.info("Composition result {}", compositionResult);
    }

    private Mono<String> toStringReactiveFun(Integer result) {
        return toStringReactiveFun(result, Schedulers.parallel());
    }

    private Mono<String> toStringReactiveFun(Integer result, Scheduler scheduler) {
        return Mono.fromCallable(() -> result.toString())
                .subscribeOn(scheduler);
    }

    private String handle(Throwable ex) {
        log.error("Error", ex);
        return "empty";
    }

    private CompletableFuture<String> toStringAsynFun(Integer result) {
        return CompletableFuture.supplyAsync(() -> result.toString());
    }

}
