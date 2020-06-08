package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FutureService {

    // Ex04_FutureCombination
    CompletableFuture<Integer> getAge(String id);

    CompletableFuture<String> getHobby(String id);

    CompletableFuture<List<String>> getUsersNames();
}
