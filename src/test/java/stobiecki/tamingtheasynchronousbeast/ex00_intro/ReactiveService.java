package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveService {

    // Ex04_FutureCombination
    Mono<Integer> getAge(String id);

    Mono<String> getHobby(String id);

    Flux<String> getUsersNames();
}
