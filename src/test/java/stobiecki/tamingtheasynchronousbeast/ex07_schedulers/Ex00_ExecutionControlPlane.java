package stobiecki.tamingtheasynchronousbeast.ex07_schedulers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Ex00_ExecutionControlPlane {

    @Test
    @SneakyThrows
    public void without_publishOn_and_without_subscribeOn() {
        CountDownLatch completionSignal = new CountDownLatch(1);

        final Mono<String> flux = Mono.just("value")
                .doOnNext(msg -> log.info("[doOnNext]: {}", msg));

        new Thread(() -> {
            flux.subscribe(
                    msg -> log.info("[consumer]: {}", msg),
                    error -> log.error("Error", error),
                    completionSignal::countDown);
        }, "thread-that-triggered-subscription"
        ).start();

        boolean await2Second = completionSignal.await(2, TimeUnit.SECONDS);
        assertThat(await2Second).withFailMessage("Await timed out!").isTrue();
    }

    @Test
    @SneakyThrows
    public void publishOn() {
        CountDownLatch completionSignal = new CountDownLatch(1);

        final Mono<String> flux = Mono.just("value")
                .doOnNext(msg -> log.info("[doOnNext1]: {}", msg))
                .publishOn(Schedulers.newSingle("publishOn"))
                .doOnNext(msg -> log.info("[doOnNext2]: {}", msg));

        new Thread(() -> {
            flux.subscribe(
                    msg -> log.info("[consumer]: {}", msg),
                    error -> log.error("Error", error),
                    completionSignal::countDown);
        }, "thread-that-triggered-subscription"
        ).start();

        boolean await2Second = completionSignal.await(2, TimeUnit.SECONDS);
        assertThat(await2Second).withFailMessage("Await timed out!").isTrue();
    }

    @Test
    @SneakyThrows
    public void subscribeOn() {
        CountDownLatch completionSignal = new CountDownLatch(1);

        final Mono<String> flux = Mono.just("value")
                .doOnNext(msg -> log.info("[doOnNext]: {}", msg))
                .subscribeOn(Schedulers.newSingle("subscribeOn-new"));

        new Thread(() -> {
            flux.subscribe(
                    msg -> log.info("[consumer]: {}", msg),
                    error -> log.error("Error", error),
                    completionSignal::countDown);
        }, "thread-that-triggered-subscription"
        ).start();

        boolean await2Second = completionSignal.await(2, TimeUnit.SECONDS);
        assertThat(await2Second).withFailMessage("Await timed out!").isTrue();
    }

    @Test
    @SneakyThrows
    public void multipleSubscribeOn() {
        CountDownLatch completionSignal = new CountDownLatch(1);

        final Mono<String> flux = Mono.just("value")
                .subscribeOn(Schedulers.newSingle("the earliest"))
                .subscribeOn(Schedulers.newSingle("in the middle"))
                .subscribeOn(Schedulers.newSingle("the latest"))
                .doOnNext(msg -> log.info("[doOnNext]: {}", msg));

        new Thread(() -> {
            flux.subscribe(
                    msg -> log.info("[consumer]: {}", msg),
                    error -> log.error("Error", error),
                    completionSignal::countDown);
        }, "thread-that-triggered-subscription"
        ).start();
        // ^^^ Only the earliest subscribeOn call in the chain is actually taken into account.

        boolean await2Second = completionSignal.await(2, TimeUnit.SECONDS);
        assertThat(await2Second).withFailMessage("Await timed out!").isTrue();
    }

    @Test
    @SneakyThrows
    public void subscribeOn_and_publishOn() {
        CountDownLatch completionSignal = new CountDownLatch(1);

        final Mono<String> flux = Mono.just("value")
                .doOnNext(msg -> log.info("[doOnNext1]: {}", msg))
                .publishOn(Schedulers.newSingle("publishOn"))
                .doOnNext(msg -> log.info("[doOnNext2]: {}", msg))
                .subscribeOn(Schedulers.newSingle("subscribeOn"));

        new Thread(() -> {
            flux.subscribe(
                    msg -> log.info("[consumer]: {}", msg),
                    error -> log.error("Error", error),
                    completionSignal::countDown);
        }, "thread-that-triggered-subscription"
        ).start();

        boolean await2Second = completionSignal.await(2, TimeUnit.SECONDS);
        assertThat(await2Second).withFailMessage("Await timed out!").isTrue();
    }


    @org.junit.Test
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

    // This will run the code in a separate thread
    @org.junit.Test
    public void publisherOnSeparateThread() throws InterruptedException {
        Thread t = new Thread(() -> {
            Mono.just("Hello")
                    .subscribe(log::info);
        });
        t.start();
        t.join();
    }
}
