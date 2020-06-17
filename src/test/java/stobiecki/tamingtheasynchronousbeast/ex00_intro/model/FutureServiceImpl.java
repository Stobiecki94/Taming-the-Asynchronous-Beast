package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import lombok.SneakyThrows;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.FutureService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static stobiecki.tamingtheasynchronousbeast.ex00_intro.model.Constants.*;

public class FutureServiceImpl implements FutureService {

    @Override
    public CompletableFuture<List<String>> getUsersNames() {
        return CompletableFuture.supplyAsync(() -> {
            requestToServerSimulation();
            return NAMES;
        });
    }

    @Override
    public CompletableFuture<Integer> getAge(String id) {
        return CompletableFuture.supplyAsync(() -> {
            requestToServerSimulation();
            return AGES.get(id);
        });
    }

    @Override
    public CompletableFuture<String> getHobby(String id) {
        return CompletableFuture.supplyAsync(() -> {
            requestToServerSimulation();
            return HOBBIES.get(id);
        });
    }

    @SneakyThrows
    private void requestToServerSimulation() {
        long delay = (long) (Math.random() * 2000);
        Thread.sleep(delay);
    }

}
