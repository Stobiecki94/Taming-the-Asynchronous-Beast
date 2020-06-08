package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import stobiecki.tamingtheasynchronousbeast.ex00_intro.FutureService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static stobiecki.tamingtheasynchronousbeast.ex00_intro.model.Constants.*;

public class FutureServiceImpl implements FutureService {

    @Override
    public CompletableFuture<Integer> getAge(String id) {
        return CompletableFuture.supplyAsync(() -> AGES.get(id));
    }

    @Override
    public CompletableFuture<String> getHobby(String id) {
        return CompletableFuture.supplyAsync(() -> HOBBIES.get(id));
    }

    @Override
    public CompletableFuture<List<String>> getUsersNames() {
        return CompletableFuture.supplyAsync(() -> NAMES);
    }

}
