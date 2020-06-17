package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class Ex03_FutureAndCompletableFuture {

    /**
     * The Future interface was added in Java 5 to serve as a result of an asynchronous computation,
     * but it did not have any methods to combine these computations or handle possible errors.
     */
    @Test
    public void future() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<String> future = executorService.submit(() -> "result");

        String result = future.get();//Blocking
        assertThat(result).isEqualTo("result");
    }

    /**
     * In Java 8, the CompletableFuture class was introduced. Along with the Future interface,
     * it also implemented the CompletionStage interface. This interface defines the contract for
     * an asynchronous computation step that can be combined with other steps.
     */
    @Test
    public void completableFuture() {
        CompletableFuture<String> completableFuture = CompletableFuture
                .supplyAsync(() -> 1)
                .thenApply(result -> result * 2)
                .thenCompose(result -> toStringAsynFun(result))
                .exceptionally(ex -> handle(ex));

        String result = completableFuture.join();
        assertThat(result).isEqualTo("2");
    }

    /**
     * @throws ExecutionException   - encapsulating an exception that occurred during a computation
     * @throws InterruptedException - an exception signifying that a thread executing a method was interrupted
     */
    @Test
    public void completableFutureAsFuture() throws ExecutionException, InterruptedException {
        Future<String> completableFuture = calculateAsync();

        String result = completableFuture.get();
        assertThat(result).isEqualTo("Hello");
    }

    private Future<String> calculateAsync() {
        CompletableFuture<String> completableFuture
                = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            Thread.sleep(500);
            completableFuture.complete("Hello");
            return null;
        });

        return completableFuture;
    }

    @Test
    public void completableFutureWithEncapsulatedComputationLogic() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            log.info("Supplier - CompletableFuture"); // <-- thread from ForkJoinPool
            return "Hello";
        });

        assertThat(future.join()).isEqualTo("Hello");
    }

    @Test
    public void completableFuture_processingResultsOfAsynchronousComputations1() {
        CompletableFuture<String> completableFuture1
                = CompletableFuture.supplyAsync(() -> "Hello");

        CompletableFuture<String> completableFuture2 = completableFuture1
                .thenApply(s -> s + " World"); // thenApply == map in Stream/Optional

        assertThat(completableFuture2.join()).isEqualTo("Hello World");
    }

    @Test
    public void completableFuture_processingResultsOfAsynchronousComputations2() {
        CompletableFuture<String> completableFuture1
                = CompletableFuture.supplyAsync(() -> "Hello");

        CompletableFuture<Void> completableFuture2 = completableFuture1
                .thenAccept(s -> log.info("Computation returned: {}" + s));

        completableFuture2.join();
    }

    @Test
    public void completableFuture_processingResultsOfAsynchronousComputations3() {
        CompletableFuture<String> completableFuture1
                = CompletableFuture.supplyAsync(() -> "Hello");

        CompletableFuture<Void> completableFuture2 = completableFuture1
                .thenRun(() -> log.info("Computation finished."));

        completableFuture2.join();
    }

    @Test
    public void completableFuture_combining1() {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello")
                .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World")); // <-- thenCompose == flatMap in Stream/Optional

        assertThat(completableFuture.join()).isEqualTo("Hello World");
    }


    @Test
    public void completableFuture_combining2() {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello")
                .thenCombine(CompletableFuture.supplyAsync(() -> " World"), (s1, s2) -> s1 + s2); // <-- thenCombine - if you want to execute two independent Future and do something with their results

        assertThat(completableFuture.join()).isEqualTo("Hello World");
    }

    @Test
    public void completableFuture_combining3() {
        CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> "Hello")
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> " World"), // <-- thenAcceptBoth - when you want to do something with two Futures‘ results, but don't need to pass any resulting value down a Future chain
                        (s1, s2) -> log.info(s1 + s2));
        completableFuture.join();
    }

    @Test
    public void runningMultipleFuturesInParallel() {
        CompletableFuture<String> future1
                = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future2
                = CompletableFuture.supplyAsync(() -> "Beautiful");
        CompletableFuture<String> future3
                = CompletableFuture.supplyAsync(() -> "World");

        CompletableFuture<Void> combinedFuture
                = CompletableFuture.allOf(future1, future2, future3);

        Void result = combinedFuture.join(); // <-- it does not return the combined results of all Futures. Instead you have to manually get results from Futures. Fortunately

        assertThat(future1.isDone()).isTrue();
        assertThat(future2.isDone()).isTrue();
        assertThat(future3.isDone()).isTrue();
    }

    @Test
    public void runningMultipleFuturesInParallelAndCombineResult() {
        CompletableFuture<String> future1
                = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> future2
                = CompletableFuture.supplyAsync(() -> "Beautiful");
        CompletableFuture<String> future3
                = CompletableFuture.supplyAsync(() -> "World");

        String combined = Stream.of(future1, future2, future3)
                .map(CompletableFuture::join)
                .collect(Collectors.joining(" "));

        assertThat(combined).isEqualTo("Hello Beautiful World");
    }

    @Test
    public void runningMultipleFuturesInParallelAndCombineResult2() {
        CompletableFuture<String> future1 = toStringAsynFun(1);
        CompletableFuture<String> future2 = toStringAsynFun(2);
        CompletableFuture<String> future3 = toStringAsynFun(3);
        CompletableFuture<String> future4 = toStringAsynFun(4);
        CompletableFuture<String> future5 = toStringAsynFun(5);
        CompletableFuture<String> future6 = toStringAsynFun(6);

        String combined1 = future1.thenCompose(result1 -> future2 // <-- we can do that way because CompletableFuture is "hot"
                .thenCompose(result2 -> future3
                        .thenCompose(result3 -> future4
                                .thenCompose(result4 -> future5
                                        .thenCompose(result5 -> future6
                                                .thenApply(result6 -> result1 + result2 + result3 + result4 + result5 + result6))))))
                .join();
        log.info("Combined: {}", combined1);
        assertThat(combined1).isEqualTo("123456");

        //or

        String combined2 = Stream.of(future1, future2, future3, future4, future5, future6)
                .map(CompletableFuture::join) // <-- we can do that way because CompletableFuture is "hot"
                .collect(Collectors.joining(""));

        log.info("Combined: {}", combined2);
        assertThat(combined2).isEqualTo("123456");
    }

    /**
     * Reactor offers rich composition options, wherein code mirrors the organization of the abstract process,
     * and everything is generally kept at the same level (nesting is minimized).
     */
    @Test
    public void runningMultipleFuturesInParallelAndCombineResult2_reactorApproach() {
        String combined = Mono.zip(
                toStringReactiveFun(1),
                toStringReactiveFun(2),
                toStringReactiveFun(3),
                toStringReactiveFun(4),
                toStringReactiveFun(5),
                toStringReactiveFun(6)
        )
                .map(tuple -> tuple.getT1() + tuple.getT2() + tuple.getT3() + tuple.getT4() + tuple.getT5() + tuple.getT6())
                .block();

        log.info("Combined: {}", combined);
        assertThat(combined).isEqualTo("123456");
    }

    @Test
    public void handlingErrors() {
        String name = null;

        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            if (name == null) {
                throw new RuntimeException("Computation error!");
            }
            return "Hello, " + name;
        }).handle((s, t) -> s != null ? s : "Hello, Stranger!");

        assertThat(completableFuture.join()).isEqualTo("Hello, Stranger!");
    }

    @Test
    public void handlingErrors2() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        completableFuture.completeExceptionally(new RuntimeException("Calculation failed!"));

        assertThatThrownBy(completableFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseExactlyInstanceOf(RuntimeException.class)
                .hasMessage("java.lang.RuntimeException: Calculation failed!");
    }

    @Test
    public void asyncMethods() {
        CompletableFuture<String> completableFuture
                = CompletableFuture.supplyAsync(() -> "Hello");

        CompletableFuture<String> future = completableFuture
                .thenApplyAsync(s -> s + " World"); //  <-- under the hood the application of a function is wrapped into a ForkJoinTask instance

        assertThat(future.join()).isEqualTo("Hello World");
    }

    private Mono<String> toStringReactiveFun(Integer result) {
        return toStringReactiveFun(result, Schedulers.parallel());
    }

    private Mono<String> toStringReactiveFun(Integer result, Scheduler scheduler) {
        return Mono.fromCallable(result::toString)
                .subscribeOn(scheduler);
    }

    private String handle(Throwable ex) {
        log.error("Error", ex);
        return "empty";
    }

    private CompletableFuture<String> toStringAsynFun(Integer result) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("toString: {}", result);
            return result.toString();
        });
    }

}
