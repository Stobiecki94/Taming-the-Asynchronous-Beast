package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Ex04_FutureCombination {

    @Test
    public void combination_completableFutureApproach() {
        CompletableFuture<List<String>> ids = getNames_Future();

        CompletableFuture<List<String>> result = ids.thenComposeAsync(l -> {
            Stream<CompletableFuture<String>> zip =
                    l.stream().map(name -> {
                        CompletableFuture<String> hobbyTask = getHobby_Future(name);
                        CompletableFuture<Integer> ageTask = getAge_Future(name);

                        return hobbyTask.thenCombineAsync(ageTask, (hobby, age) -> name + " is " + age + " years old and likes " + hobby);
                    });
            List<CompletableFuture<String>> combinationList = zip.collect(Collectors.toList());
            CompletableFuture<String>[] combinationArray = combinationList.toArray(new CompletableFuture[combinationList.size()]);

            CompletableFuture<Void> allDone = CompletableFuture.allOf(combinationArray);
            return allDone.thenApply(v -> combinationList.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList()));
        });

        List<String> results = result.join();

        assertThat(results).containsExactlyInAnyOrder(
                "Adam is 13 years old and likes Football",
                "Kuba is 14 years old and likes Volleyball",
                "Grzegorz is 15 years old and likes Chess");
    }

    @Test
    public void combination_reactorApproach() {
        Mono<List<String>> flux = getNames_Flux()
                .flatMap(name -> Mono.zip(getHobby_Mono(name), getAge_Mono(name),
                        (hobby, age) -> name + " is " + age + " years old and likes " + hobby))
                .collectList();

        List<String> results = flux.block();

        assertThat(results).containsExactlyInAnyOrder(
                "Adam is 13 years old and likes Football",
                "Kuba is 14 years old and likes Volleyball",
                "Grzegorz is 15 years old and likes Chess");
    }

    private Mono<Integer> getAge_Mono(String id) {
        return Mono.fromCallable(() -> AGES.get(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> getHobby_Mono(String id) {
        return Mono.fromCallable(() -> HOBBIES.get(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Flux<String> getNames_Flux() {
        return Flux.just("Adam", "Kuba", "Grzegorz")
                .subscribeOn(Schedulers.boundedElastic());
    }

    private CompletableFuture<Integer> getAge_Future(String id) {
        return CompletableFuture.supplyAsync(() -> AGES.get(id));
    }

    private CompletableFuture<String> getHobby_Future(String id) {
        return CompletableFuture.supplyAsync(() -> HOBBIES.get(id));
    }

    private CompletableFuture<List<String>> getNames_Future() {
        return CompletableFuture.supplyAsync(() -> asList("Adam", "Kuba", "Grzegorz"));
    }

    private final Map<String, String> HOBBIES = new ImmutableMap.Builder<String, String>()
            .put("Adam", "Football")
            .put("Kuba", "Volleyball")
            .put("Grzegorz", "Chess")
            .build();
    private final Map<String, Integer> AGES = new ImmutableMap.Builder<String, Integer>()
            .put("Adam", 13)
            .put("Kuba", 14)
            .put("Grzegorz", 15)
            .build();
}
