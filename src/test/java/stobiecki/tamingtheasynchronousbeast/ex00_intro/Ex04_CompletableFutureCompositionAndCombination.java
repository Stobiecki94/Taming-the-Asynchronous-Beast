package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.FutureServiceImpl;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.ReactiveServiceImpl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Ex04_CompletableFutureCompositionAndCombination {

    //scenario: getUsersName -> get their hobbies and age -> then combine

    @Test
    public void combination_completableFutureApproach() {
        FutureService futureService = new FutureServiceImpl();

        CompletableFuture<List<String>> result = futureService.getUsersNames()
                .thenComposeAsync(userNames -> {
                    List<CompletableFuture<String>> combinationList =
                            userNames.stream().map(name -> {
                                CompletableFuture<String> hobbyTask = futureService.getHobby(name);
                                CompletableFuture<Integer> ageTask = futureService.getAge(name);
                                return hobbyTask.thenCombineAsync(ageTask, (hobby, age) -> name + " is " + age + " years old and likes " + hobby);
                            })
                                    .collect(Collectors.toList());

                    CompletableFuture<Void> allDone = CompletableFuture.allOf(combinationList.toArray(new CompletableFuture[combinationList.size()]));
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
        ReactiveService reactiveService = new ReactiveServiceImpl();

        Mono<List<String>> flux = reactiveService.getUsersNames()
                .flatMap(name -> Mono.zip(reactiveService.getHobby(name), reactiveService.getAge(name),
                        (hobby, age) -> name + " is " + age + " years old and likes " + hobby))
                .collectList();

        List<String> results = flux.block();

        assertThat(results).containsExactlyInAnyOrder(
                "Adam is 13 years old and likes Football",
                "Kuba is 14 years old and likes Volleyball",
                "Grzegorz is 15 years old and likes Chess");
    }


}
