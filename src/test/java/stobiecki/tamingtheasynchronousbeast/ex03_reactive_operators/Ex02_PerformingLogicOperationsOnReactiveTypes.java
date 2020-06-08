package stobiecki.tamingtheasynchronousbeast.ex03_reactive_operators;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class Ex02_PerformingLogicOperationsOnReactiveTypes {

    @Test
    void all() {
        Flux<String> animalFlux = Flux.just("Elephant", "Eagle", "Kangaroo", "Cow");

        Mono<Boolean> haveAllA = animalFlux
                .all(animal -> animal.contains("a"));

        StepVerifier.create(haveAllA)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void any() {
        Flux<String> animalFlux = Flux.just("Elephant", "Eagle", "Kangaroo", "Cow");

        Mono<Boolean> hasAnyA = animalFlux
                .any(animal -> animal.contains("a"));

        StepVerifier.create(hasAnyA)
                .expectNext(true)
                .verifyComplete();
    }
}
