package stobiecki.tamingtheasynchronousbeast.ex04_combining_reactive_types;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

@Slf4j
public class Ex01_CombiningReactiveTypesTest {

    /**
     * The concatenation is achieved by sequentially subscribing to the first source
     * then waiting for it to complete before subscribing to the next, and so on until the last source completes
     */
    @Test
    void concatFluxes() {
        Flux<String> characterFlux = Flux.just("Garfield", "Barbossa")
                .delayElements(Duration.ofMillis(500));
        Flux<String> foodFlux = Flux.just("lasagne", "apples")
                .delaySubscription(Duration.ofMillis(250))
                .delayElements(Duration.ofMillis(500));

        Flux<String> concatedFlux = Flux.concat(characterFlux, foodFlux);

        StepVerifier.create(concatedFlux)
                .expectNext("Garfield", "Barbossa", "lasagne", "apples")
                .verifyComplete();
    }

    /**
     *  opposed to concat (lazy subscription), the sources are subscribed eagerly.
     */
    @Test
    void mergeFluxes() {
        Flux<String> characterFlux = Flux.just("Garfield", "Barbossa")
                .delayElements(Duration.ofMillis(500));
        Flux<String> foodFlux = Flux.just("lasagne", "apples")
                .delaySubscription(Duration.ofMillis(250))
                .delayElements(Duration.ofMillis(500));

        Flux<String> mergedFlux = characterFlux.mergeWith(foodFlux);

        StepVerifier.create(mergedFlux)
                .expectNext("Garfield", "lasagne", "Barbossa", "apples")
                .verifyComplete();
    }

    /**
     * Unlike concat, sources are subscribed to eagerly.
     */
    @Test
    void mergeSequentialFluxes() {
        Flux<String> characterFlux = Flux.just("Garfield", "Barbossa")
                .delayElements(Duration.ofMillis(500));
        Flux<String> foodFlux = Flux.just("lasagne", "apples")
                .delaySubscription(Duration.ofMillis(250))
                .delayElements(Duration.ofMillis(500));

        Flux<String> mergedFlux = Flux.mergeSequential(characterFlux, foodFlux);

        StepVerifier.create(mergedFlux)
                .expectNext("Garfield", "Barbossa", "lasagne", "apples")
                .verifyComplete();
    }

    /**
     * The static method zip agglutinates multiple sources together, i.e., waits for all the sources to emit
     * one element and combines these elements into an output value (constructed by the provided combinator function).
     *
     * The operator will continue doing so until any of the sources completes
     */
    @Test
    void zipFluxes() {
        Flux<String> characterFlux = Flux.just("Garfield", "Barbossa", "Snoopy");
        Flux<String> foodFlux = Flux.just("lasagne", "apples");

        Flux<String> zippedFlux = characterFlux.zipWith(foodFlux, (character, food) -> String.format("%s likes %s", character, food));
        StepVerifier.create(zippedFlux)
                .expectNext("Garfield likes lasagne", "Barbossa likes apples")
                .verifyComplete();

        Flux<String> zippedFlux2 = Flux.zip(characterFlux, foodFlux, (character, food) -> String.format("%s likes %s", character, food));
        StepVerifier.create(zippedFlux2)
                .expectNext("Garfield likes lasagne", "Barbossa likes apples")
                .verifyComplete();
    }

    @Test
    void firstFlux() {
        Flux<String> slowFlux = Flux.just("turtle", "snail", "sloth")
                .delaySubscription(Duration.ofMillis(100));
        Flux<String> fastFlux = Flux.just("hare", "puma", "squirrel");

        Flux<String> firstFlux = Flux.first(slowFlux, fastFlux);

        StepVerifier.create(firstFlux)
                .expectNext("hare", "puma", "squirrel")
                .verifyComplete();
    }


}
