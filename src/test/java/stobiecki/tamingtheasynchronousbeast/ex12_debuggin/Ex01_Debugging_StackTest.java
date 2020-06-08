package stobiecki.tamingtheasynchronousbeast.ex12_debuggin;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.io.File;

public class Ex01_Debugging_StackTest {

    //TRAP and unobvious
    @Test
    public void debugging() {
        Hooks.onOperatorDebug(); // <-- uncomment

        Mono<Long> totalTxtSize = Flux
                .just("/tmp", "/home", "/404")
                .map(File::new)
                .concatMap(file -> Flux.just(file.listFiles()))
                .filter(File::isFile)
                .filter(file -> file.getName().endsWith(".txt"))
                .map(File::length)
                .reduce(0L, Math::addExact);


        totalTxtSize.subscribe(System.out::println);
    }
}
