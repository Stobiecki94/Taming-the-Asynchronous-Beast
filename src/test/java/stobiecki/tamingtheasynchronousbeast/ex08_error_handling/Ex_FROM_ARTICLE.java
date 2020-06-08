//package stobiecki.tamingtheasynchronousbeast.ex08_error_handling;
//
//import org.junit.Test;
//import reactor.core.publisher.Flux;
//
//public class Ex_FROM_ARTICLE {
//
//    /**
//     * Error handling in Subscriber
//     * Callback
//     */
//    @Test
//    public void fluxErrorHanflingInSubscriber() {
//        Flux.range(1, 10)
//                .map(this::doSomethingDangerous)
//                .subscribe(
//                        value -> System.out.println("RECEIVED " + value),
//                        error -> System.err.println("CAUGHT " + error)
//                );
//    }
//
//    @Test
//    public void abc() { //todo name and logger
//        Flux.just(1, 2, 0)
//                .map(i -> "100 / " + i + " = " + (100 / i)) //this triggers an error with 0
//                .onErrorReturn("Divided by zero :(") // error handling example
//                .subscribe(
//                        value -> System.out.println("RECEIVED " + value),
//                        error -> System.err.println("CAUGHT " + error)
//                );
//    }
//
//    @Test
//    public void handleErrorWithStaticFallbackValue() {
//        // Imperative Approach
//        try {
//            return doSomethingDangerous(10);
//        }
//        catch (Throwable error) {
//            return "RECOVERED";
//        }
//
//// Reactive Approach
//        Flux.just(10)
//                .map(this::doSomethingDangerous)
//                .onErrorReturn("RECOVERED");
//    }
//
//    @Test
//    public void handleErrorWithAFallbackMethodCall() {
//        // Imperative Approach
//
//        String v1;
//        try {
//            v1 = callExternalService("key1");
//        }
//        catch (Throwable error) {
//            v1 = getFromCache("key1");
//        }
//
//        String v2;
//        try {
//            v2 = callExternalService("key2");
//        }
//        catch (Throwable error) {
//            v2 = getFromCache("key2");
//        }
//
//// Reactive Approach
//
//        Flux.just("key1", "key2")
//                .flatMap(k -> callExternalService(k)
//                        .onErrorResume(e -> getFromCache(k))
//                );
//    }
//
//    @Test
//    public void errorHandlingCatchAndRethrow() {
//        try {
//            return callExternalService(k);
//        }
//        catch (Throwable error) {
//            throw new BusinessException("oops, SLA exceeded", error);
//        }
//
//
//        Flux.just("timeout1")
//                .flatMap(k -> callExternalService(k))
//                .onErrorResume(original -> Flux.error(
//                        new BusinessException("oops, SLA exceeded", original))
//                );
//
//// OR
//
//        Flux.just("timeout1")
//                .flatMap(k -> callExternalService(k))
//                .onErrorMap(original -> new BusinessException("oops, SLA exceeded", original));
//    }
//
//    @Test
//    public void logOrReactOnTheSide() {
//        try {
//            return callExternalService(k);
//        }
//        catch (RuntimeException error) {
//            //make a record of the error
//            log("uh oh, falling back, service failed for key " + k);
//            throw error;
//        }
//
//
//        LongAdder failureStat = new LongAdder();
//        Flux<String> flux =
//                Flux.just("unknown")
//                        .flatMap(k -> callExternalService(k)
//                                .doOnError(e -> {
//                                    failureStat.increment();
//                                    log("uh oh, falling back, service failed for key " + k);
//                                })
//
//                        );
//    }
//
//    @Test
//    public void theFinallyBlock() {
//        Stats stats = new Stats();
//        stats.startTimer();
//        try {
//            doSomethingDangerous();
//        }
//        finally {
//            stats.stopTimerAndRecordTiming();
//        }
//
//        Stats stats = new Stats();
//        LongAdder statsCancel = new LongAdder();
//
//        Flux<String> flux =
//                Flux.just("foo", "bar")
//                        .doOnSubscribe(s -> stats.startTimer())
//                        .doFinally(type -> {
//                            stats.stopTimerAndRecordTiming();
//                            if (type == SignalType.CANCEL)
//                                statsCancel.increment();
//                        })
//                        .take(1);
//    }
//
//
//
//
//
//
//
//
//    /**
//     * Simple method which simulates a crash
//     * at some point.
//     *
//     * @param a Integer input
//     */
//    public String doSomethingDangerous(Integer a) {
//        // Just crash for vaule 6
//        if (a == 6) {
//            throw new RuntimeException("Oops! something broke");
//        }
//        return a.toString();
//    }
//
//
//
//
//
//
//}
