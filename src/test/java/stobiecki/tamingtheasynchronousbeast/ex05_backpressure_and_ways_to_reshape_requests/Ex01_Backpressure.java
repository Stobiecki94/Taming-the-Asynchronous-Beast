package stobiecki.tamingtheasynchronousbeast.ex05_backpressure_and_ways_to_reshape_requests;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Ex01_Backpressure {

    //Long.MAX_VALUE represents an unbounded request (meaning “produce as fast as you can” — basically disabling backpressure)

    /**
     * The simplest way of customizing the original request is to subscribe with a BaseSubscriber with the hookOnSubscribe method overridden
     */
    @Test
    public void shouldRespectBackpressure() {
        Flux.range(1, 10)
                .doOnRequest(r -> log.info("request of {}", r))
                .subscribe(new BaseSubscriber<Integer>() {

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    public void hookOnNext(Integer integer) {
                        log.info("Next: {}", integer);
                    }
                });
    }

    @Test
    public void shouldRespectBackpressure2() {
        Flux.range(1, 10)
                .doOnRequest(r -> log.info("request of {}", r))
                .subscribe(new BaseSubscriber<Integer>() {

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    public void hookOnNext(Integer integer) {
                        log.info("Next: {}", integer);
                        if (integer > 5) {
                            log.info("Cancelling after having received {}", integer);
                            cancel();
                        } else {
                            request(1);
                        }
                    }
                });
    }

    @SneakyThrows
    @Test
    public void customFluxWithBackpressureSupport() {
        CountDownLatch completionSignal = new CountDownLatch(1);
        Flux<Double> fluxWithBackpressureSupport = Flux
                .generate(emitter -> emitter.next(Math.random()));
        //              ^^^ cannot call more then one time onNext from emitter in generate method

        fluxWithBackpressureSupport
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(new BaseSubscriber<Double>() {

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        log.info("Request one element");
                        request(1);
                    }

                    @Override
                    public void hookOnNext(Double randomNumber) {
                        log.info("Get random number {}", randomNumber);
                        if (randomNumber < 0.2) {
                            log.info("Cancel subscription");
                            cancel();
                            completionSignal.countDown();
                            return;
                        }
                        sleep(500);
                        log.info("Request next one event");
                        request(1);
                    }

                    @Override
                    protected void hookOnError(Throwable throwable) {
                        log.error("Error here", throwable);
                    }

                    @Override
                    protected void hookOnComplete() {
                        log.info("Completed.");
                    }
                });

        boolean await10Seconds = completionSignal.await(10, TimeUnit.SECONDS);
        assertThat(await10Seconds).withFailMessage("Await timed out!").isTrue();

    }

    @SneakyThrows
    private void sleep(long timeout) {
        TimeUnit.MILLISECONDS.sleep(timeout);
    }
}
