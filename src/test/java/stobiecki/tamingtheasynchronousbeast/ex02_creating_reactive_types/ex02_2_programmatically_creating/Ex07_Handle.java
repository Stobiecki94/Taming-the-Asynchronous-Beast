package stobiecki.tamingtheasynchronousbeast.ex02_creating_reactive_types.ex02_2_programmatically_creating;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

@Slf4j
public class Ex07_Handle {

    /**
     * Flux/Mono.handle()
     *
     * It is close to generate, in the sense that it uses a SynchronousSink and only allows one-by-one emissions.
     * However, handle can be used to generate an arbitrary value out of each source element, possibly skipping some elements.
     * In this way, it can serve as a combination of map and filter. The signature of handle is as follows:
     *
     * Flux<R> handle(BiConsumer<T, SynchronousSink<R>>);
     */
    private static final boolean docHolder = false;

    /**
     * Example use-case:
     *
     * Let’s consider an example. The reactive streams specification disallows null values in a sequence. What if you want to perform a map but you want to use a preexisting method as the map function, and that method sometimes returns null?
     */
    @Test
    public void handle_mapAndEliminateNulls() {
        Flux<String> alphabet = Flux.just(-1, 2, 18, 5, 1, 11)
                .handle((i, sink) -> {
                    String letter = alphabet(i);
                    if (letter != null)
                        sink.next(letter);
                });

        alphabet
                .subscribe(log::info);

        log.info(alphabet.toStream().collect(Collectors.joining())+ " 5 minutes");
    }

    public String alphabet(int letterNumber) {
        if (letterNumber < 1 || letterNumber > 52) {
            return null;
        }
        int letterIndexAscii = 'A' + letterNumber - 1;
        return "" + (char) letterIndexAscii;
    }
}
