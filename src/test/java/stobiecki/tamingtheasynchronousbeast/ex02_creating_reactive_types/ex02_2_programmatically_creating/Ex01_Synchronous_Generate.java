package stobiecki.tamingtheasynchronousbeast.ex02_creating_reactive_types.ex02_2_programmatically_creating;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Slf4j
public class Ex01_Synchronous_Generate {

    //@formatter:off
    /**
     * Flux.generate - for synchronous and one-by-one emissions, meaning that the sink is a SynchronousSink and that its next() method can only
     * be called at most once per callback invocation. You can then additionally call error(Throwable) or complete(), but this is optional.
     *
     * public static  <T> Flux<T> generate(Consumer <SynchronousSink<T>> generator)
     *
     * public static <T,S> Flux<T> generate(Callable <S> stateSupplier,
     *                                      BiFunction <S,SynchronousSink<T>,S> generator
     *
     * public static <T,S> Flux<T> generate(Callable <S> stateSupplier,
     *                                      BiFunction <S,SynchronousSink<T>,S> generator,
     *                                      Consumer <? super S> stateConsumer
     *
     */
    //@formatter:on
    @Test
    public void generate_stateless() {
        Flux<String> flux = Flux.generate(
                sink -> sink.next(ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));

        flux
                .log()
                .take(3)
                .subscribe();
    }

    /**
     * this case lets you keep a state that you can refer to in your sink usage to decide what to emit nex
     */
    @Test
    public void generate_stateBased() {
        Flux<String> flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("state: " + state);
                    if (state == 5) {
                        sink.complete();
                    }
                    return state + 1;
                });

        flux
                .log()
                .subscribe();
    }

    @Test
    public void generate_mutableStateVariant() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long currentState = state.getAndIncrement();
                    sink.next("state: " + currentState);
                    if (currentState == 5) {
                        sink.complete();
                    }
                    return state;
                });

        flux
                .log()
                .subscribe();
    }

    /**
     * If your state object needs to clean up some resources, use the generate(Supplier<S>, BiFunction, Consumer<S>) variant to clean up the last state instance
     * <p>
     * e.g. In the case of the state containing a database connection or other resource that needs to be handled at the end of the process,
     * the Consumer lambda could close the connection or otherwise handle any tasks that should be done at the end of the process.
     */
    @Test
    public void generate_cleanUpStateObjectResource() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long currentState = state.getAndIncrement();
                    sink.next("state: " + currentState);
                    if (currentState == 5) {
                        sink.complete();
                    }
                    return state;
                },
                (state) -> log.info("cleaning up resources"));

        flux
                .log()
                .subscribe();
    }

    @Test
    public void create_errorCallback() {
        Flux<Integer> flux = Flux.generate(
                () -> -2,
                (state, sink) -> {
                    try {
                        sink.next(1 / state);//<-- Exception here when i == 0
                    } catch (Exception ex) {
                        sink.error(ex);
                    }
                    return state + 1;
                });

        flux
                .subscribe(event -> log.info("Got event {}", event),
                        ex -> log.error("Error callback", ex),
                        () -> log.info("No more events"));
    }

    /**
     * Generate - Programmatically create a Flux by generating signals one-by-one via a consumer callback
     * java.lang.IllegalStateException: More than one call to onNext
     */
    @Test
    public void create_moreThanOneCallToOnNext() {
        Flux<Integer> flux = Flux.generate(emitter -> {
            IntStream.range(0, 10)
                    .forEach(emitter::next); // <-- More than one call to onNext
            emitter.complete();
        });

        flux
                .subscribe(event -> log.info("Got event {}", event),
                        ex -> log.error("Error callback", ex),
                        () -> log.info("No more events"));
    }
}
