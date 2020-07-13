package stobiecki.tamingtheasynchronousbeast.ex13_learning;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.*;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.Executors;

import static java.time.temporal.ChronoUnit.MILLIS;

@Slf4j
public class Learning {

    @Test
    public void whatIfSubscribeOnWhenInterval1() {
        Flux.interval(Duration.ofSeconds(1))
                .doOnNext(next -> log.info("Next: {}", next))
                .take(5)
                .blockLast();
    }

    @Test
    public void whatIfSubscribeOnWhenInterval2() {
        Flux.interval(Duration.ofSeconds(1))
                .subscribeOn(Schedulers.single())
                .doOnNext(next -> log.info("Next: {}", next))
                .take(5)
                .blockLast();
    }

    @Test
    public void whatIfSubscribeOnWhenInterval3() {
        Flux.interval(Duration.ofSeconds(1))
                .doOnNext(next -> log.info("Next: {}", next))
                .flatMap(next -> Mono.just(next)
                        .subscribeOn(Schedulers.newSingle("Inner"))
                        .doOnNext(innerNext -> log.info("Inner next: {}", innerNext)))
                .doOnNext(next -> log.info("Next after: {}", next))
                .take(5)
                .subscribeOn(Schedulers.newSingle("Outer"))
                .blockLast();
    }

    @Test
    public void intervalWhenEmptyInFlatMap() {
        Flux.interval(Duration.ofSeconds(1))
                .flatMap(next -> next < 2 ? Mono.empty() : Mono.just(next))
                .doOnNext(next -> log.info("Next after: {}", next))
                .take(5)
                .blockLast();
    }

    @Test
    public void virtualTime() {
        StepVerifier
                .withVirtualTime(() -> Flux.interval(Duration.ofSeconds(1)).take(2))
                .expectSubscription()
                .expectNoEvent(Duration.ofSeconds(1))
                .expectNext(0L)
                .thenAwait(Duration.ofSeconds(1))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    public void processor() {
        EmitterProcessor<String> emitterProcessor = EmitterProcessor.create();

        FluxSink<String> sink = emitterProcessor.sink();
        sink.next("e");



        emitterProcessor.subscribe(log::info);
    }

    @Test
    @SneakyThrows
    public void processor2() {
        EmitterProcessor<String> emitter = EmitterProcessor.create();
        FluxSink<String> sink = emitter.sink();
        emitter.publishOn(Schedulers.single())
                .map(String::toUpperCase)
                .filter(s -> s.startsWith("HELLO"))
                .delayElements(Duration.of(1000, MILLIS))
                .subscribe(System.out::println);

        sink.next("Hello World!");
        sink.next("Goodbye World");
        sink.next("Again");
        Thread.sleep(3000);
    }

    @Test
    public void processor3() {
        TopicProcessor<Long> data = TopicProcessor.<Long>builder()
                .executor(Executors.newFixedThreadPool(2)).build();
        data.subscribe(t -> System.out.println(t));
        data.subscribe(t -> System.out.println(t));
        FluxSink<Long> sink= data.sink();
        sink.next(10L);
        sink.next(11L);
        sink.next(12L);
    }

    @Test
    public void processor4() {
        WorkQueueProcessor<Long> data = WorkQueueProcessor.<Long>builder().build();
        data.subscribe(t -> System.out.println("1. "+t));
        data.subscribe(t -> System.out.println("2. "+t));
        FluxSink<Long> sink= data.sink();
        sink.next(10L);
        sink.next(11L);
        sink.next(12L);
    }
}
