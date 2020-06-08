package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.ReactiveService;

import static stobiecki.tamingtheasynchronousbeast.ex00_intro.model.Constants.*;

public class ReactiveServiceImpl implements ReactiveService {

    @Override
    public Mono<Integer> getAge(String id) {
        return Mono.fromCallable(() -> AGES.get(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> getHobby(String id) {
        return Mono.fromCallable(() -> HOBBIES.get(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<String> getUsersNames() {
        return Flux.fromIterable(NAMES)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
