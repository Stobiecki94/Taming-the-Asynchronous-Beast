package stobiecki.tamingtheasynchronousbeast.ex02_creating_reactive_types.ex02_1_factory_methods;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

@Slf4j
public class Ex01_FactoryMethods {

    @Test
    void createAMono_just() {
        Mono<String> fruitMono = Mono.just("Apple")
                .doOnNext(fruit -> log.info("Fruit: {}", fruit));

        StepVerifier.create(fruitMono)
                .expectNext("Apple")
                .verifyComplete();
    }

    @Test
    void createAFlux_just() {
        Flux<String> fruitFlux = Flux.just("Apple", "Orange", "Pineapple")
                .doOnNext(fruit -> log.info("Fruit: {}", fruit));

        StepVerifier.create(fruitFlux)
                .expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Pineapple")
                .verifyComplete();
    }

    @Test
    void createAFlux_fromArray() {
        String[] fruits = {"Apple", "Orange", "Pineapple"};
        Flux<String> fruitFlux = Flux.fromArray(fruits)
                .doOnNext(fruit -> log.info("Fruit: {}", fruit));

        StepVerifier.create(fruitFlux)
                .expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Pineapple")
                .verifyComplete();
    }

    @Test
    void createAFlux_fromIterable() {
        List<String> fruitList = asList("Apple", "Orange", "Pineapple");
        Flux<String> fruitFlux = Flux.fromIterable(fruitList)
                .doOnNext(fruit -> log.info("Fruit: {}", fruit));

        StepVerifier.create(fruitFlux)
                .expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Pineapple")
                .verifyComplete();
    }

    @Test
    void createAFlux_fromStream() {
        Stream<String> fruitStream = Stream.of("Apple", "Orange", "Pineapple");
        Flux<String> fruitFlux = Flux.fromStream(fruitStream)
                .doOnNext(fruit -> log.info("Fruit: {}", fruit));

        StepVerifier.create(fruitFlux)
                .expectNext("Apple")
                .expectNext("Orange")
                .expectNext("Pineapple")
                .verifyComplete();
    }

    @Test
    void createAFlux_range() {
        Flux<Integer> rangeFlux = Flux.range(1, 5);

        StepVerifier.create(rangeFlux)
                .expectNext(1, 2, 3, 4, 5)
                .verifyComplete();
    }

    @Test
    void createAFlux_interval() {
        Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1)) //TRAP `Runs on the Schedulers.parallel() Scheduler`
                .take(5);

        StepVerifier.create(intervalFlux)
                .expectNext(0L, 1L, 2L, 3L, 4L)
                .verifyComplete();
    }

    @Test
    void createAFlux_empty() {
        Flux<Long> empty = Flux.empty();

        StepVerifier.create(empty)
                .verifyComplete();
    }
}
