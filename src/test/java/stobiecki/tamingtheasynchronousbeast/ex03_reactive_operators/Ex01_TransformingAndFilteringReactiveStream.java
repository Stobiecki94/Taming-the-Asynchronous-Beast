package stobiecki.tamingtheasynchronousbeast.ex03_reactive_operators;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Ex01_TransformingAndFilteringReactiveStream {

    @Test
    void skipAFew() {
        Flux<Integer> skipFlux = Flux.range(1, 10)
                .skip(8);

        StepVerifier.create(skipFlux)
                .expectNext(9, 10)
                .verifyComplete();
    }

    @Test
    void skipAFewSeconds() {
        Flux<Integer> skipFlux = Flux.range(1, 100)
                .delayElements(Duration.ofSeconds(1))
                .skip(Duration.ofSeconds(3))
                .take(3);

        StepVerifier.create(skipFlux)
                .expectNext(3, 4, 5)
                .verifyComplete();
    }

    @Test
    void take() {
        Flux<Integer> take3Flux = Flux.range(1, 999)
                .take(3);

        StepVerifier.create(take3Flux)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    void takeMoreThanAvailable() {
        Flux<Integer> take = Flux.range(1, 2)
                .take(5);

        StepVerifier.create(take)
                .expectNext(1, 2)
                .verifyComplete();
    }

    @Test
    void takeZero() {
        Flux<Integer> take = Flux.range(1, 9999)
                .take(0);

        StepVerifier.create(take)
                .verifyComplete();
    }

    @Test
    void takeByDuration() {
        Flux<Integer> takeFlux = Flux.range(1, 100)
                .delayElements(Duration.ofSeconds(1))
                .take(Duration.ofMillis(3500));

        StepVerifier.create(takeFlux)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }

    @Test
    void takeByZeroDuration() {
        Flux<Integer> takeFlux = Flux.range(1, 100)
                .delayElements(Duration.ofSeconds(1))
                .take(Duration.ZERO);

        StepVerifier.create(takeFlux)
                .verifyComplete();
    }

    @Test
    void takeByDurationWhenNoElementsEmitted() {
        Flux<Object> takeFlux = Flux.empty()
                .take(Duration.ofMillis(100));

        StepVerifier.create(takeFlux)
                .verifyComplete();
    }

    @Test
    void filter() {
        Flux<String> flux = Flux.just("a", "b b", "c", "d d")
                .filter(word -> !word.contains(" "));

        StepVerifier.create(flux)
                .expectNext("a", "c")
                .verifyComplete();
    }

    @Test
    void filterWhenExceptionThrownByPredicate() {
        Flux<String> flux = Flux.just("a", "b", "c", "d")
                .filter(word -> {
                    if (word.equals("c")) {
                        throw new RuntimeException("Error");
                    }
                    return true;
                });

        StepVerifier.create(flux)
                .expectNext("a", "b")
                .verifyError();
    }

    @Test
    void distinct() {
        Flux<String> animals = Flux.just("dog", "cat", "horse", "cat", "horse", "cow")
                .distinct();

        StepVerifier.create(animals)
                .expectNext("dog", "cat", "horse", "cow")
                .verifyComplete();
    }

    @Test
    void distinctWithDistinctCollection() {
        Flux<String> animals = Flux.just("dog", "cat", "horse", "cat", "horse", "cow")
                .distinct(animal -> animal, HashSet::new);

        StepVerifier.create(animals)
                .expectNext("dog", "cat", "horse", "cow")
                .verifyComplete();
    }

    @Test
    void distinctWithDistinctCollection2() {
        HashSet<Object> distinctCollection = new HashSet<>(asList("dog", "cat", "horse"));
        Flux<String> animals = Flux.just("dog", "cat", "horse", "cat", "horse", "cow")
                .distinct(animal -> animal, () -> distinctCollection);

        StepVerifier.create(animals)
                .expectNext("cow")
                .verifyComplete();
    }

    @Test
    void distinctWithDistinctCollection3() {
        HashSet<Object> distinctCollection = new HashSet<>();
        Flux<String> animals = Flux.just("dog", "cat", "horse", "cat", "horse", "cow")
                .distinct(animal -> animal, () -> distinctCollection);

        StepVerifier.create(animals)
                .expectNext("dog", "cat", "horse", "cow")
                .then(() -> assertThat(distinctCollection).isEmpty()) //distinct collection is cleared
                .verifyComplete();
    }

    @Test
    void complexDistinct() {
        Flux<String> animals = Flux.just("dog", "cat", "horse", "cat", "horse", "cow")
                .distinct(animal -> animal, HashSet::new, (distinctStore, element) -> {
                    if (distinctStore.contains(element)) {
                        return false;
                    }
                    distinctStore.add(element);
                    return true;
                }, HashSet::clear);

        StepVerifier.create(animals)
                .expectNext("dog", "cat", "horse", "cow")
                .verifyComplete();
    }

    @Test
    void map() {
        Flux<Player> players = Flux.just("Michael Jordan", "Steve Kerr")
                .map(Player::fromString);

        StepVerifier.create(players)
                .expectNext(new Player("Michael", "Jordan"))
                .expectNext(new Player("Steve", "Kerr"))
                .verifyComplete();
    }

    @Test
    void flatMap() {
        Flux<Player> players = Flux.just("Michael Jordan", "Steve Kerr")
                .flatMap(player -> Mono.just(player)
                        .map(Player::fromString)
                        .subscribeOn(Schedulers.parallel()));

        List<Player> playerList = asList(new Player("Michael", "Jordan"), new Player("Steve", "Kerr"));

        StepVerifier.create(players)
                .expectNextMatches(playerList::contains)
                .expectNextMatches(playerList::contains)
                .verifyComplete();
    }

    @Test
    void buffer() {
        Flux<String> fruitFlux = Flux.just("Apple", "Banana", "Pineapple", "Orange", "Strawberry");

        Flux<List<String>> bufferedFlux = fruitFlux.buffer(2);

        StepVerifier.create(bufferedFlux)
                .expectNext(asList("Apple", "Banana"))
                .expectNext(asList("Pineapple", "Orange"))
                .expectNext(asList("Strawberry"))
                .verifyComplete();
    }

    @Test
    void bufferAndThenTake() {
        Flux<String> fruitFlux = Flux.just("Apple", "Banana", "Pineapple", "Orange", "Strawberry");

        Flux<List<String>> bufferedFlux = fruitFlux
                .buffer(2)
                .take(1);

        StepVerifier.create(bufferedFlux)
                .expectNext(asList("Apple", "Banana"))
                .verifyComplete();
    }

    @Test
    void bufferAndFlatMap() {
        Flux.just("Apple", "Banana", "Pineapple", "Orange", "Strawberry")
                .buffer(2)
                .flatMap(fruits -> Flux.fromIterable(fruits)
                        .map(String::toUpperCase)
                        .subscribeOn(Schedulers.parallel())
                        .log()
                ).subscribe();
    }

    @Test
    void bufferWithoutArgument() {
        Flux<List<String>> buffer = Flux.just("Apple", "Banana", "Pineapple", "Orange", "Strawberry")
                .buffer();

        StepVerifier.create(buffer)
                .expectNext(asList("Apple", "Banana", "Pineapple", "Orange", "Strawberry"))
                .verifyComplete();
    }

    @Test
    void collectList() {
        Mono<List<String>> collectList = Flux.just("Apple", "Banana", "Pineapple", "Orange", "Strawberry")
                .collectList();

        StepVerifier.create(collectList)
                .expectNext(asList("Apple", "Banana", "Pineapple", "Orange", "Strawberry"))
                .verifyComplete();
    }

    @Test
    void collectMap() {
        Mono<Map<Character, String>> mapMono = Flux.just("Apple", "Banana", "Avocado")
                .collectMap(fruit -> fruit.charAt(0));

        StepVerifier.create(mapMono)
                .expectNextMatches(map -> map.size() == 2 &&
                        map.get('A').equals("Avocado") && //overwriting!
                        map.get('B').equals("Banana"))
                .verifyComplete();
    }

    @Value
    static class Player {
        String name;
        String surname;

        static Player fromString(String fromString) {
            String[] split = fromString.split("\\s");
            return new Player(split[0], split[1]);
        }
    }
}
