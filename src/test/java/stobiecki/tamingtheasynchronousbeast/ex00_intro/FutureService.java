package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FutureService {

    // Ex04_CompletableFutureCompositionAndCombination
    CompletableFuture<List<String>> getUsersNames();

    CompletableFuture<Integer> getAge(String id);

    CompletableFuture<String> getHobby(String id);

}
