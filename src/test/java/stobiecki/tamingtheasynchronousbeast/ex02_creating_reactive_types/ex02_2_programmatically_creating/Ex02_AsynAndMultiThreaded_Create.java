package stobiecki.tamingtheasynchronousbeast.ex02_creating_reactive_types.ex02_2_programmatically_creating;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class Ex02_AsynAndMultiThreaded_Create {

    //@formatter:off
    /**
     * Mono.create - creates a deferred emitter that can be used with callback-based APIs to signal at most one value, a complete or an error signal
     * The MonoSink of Mono’s create doesn’t allow several emissions. It will drop all signals after the first one
     *
     * public static <T> Mono<T> create(Consumer <MonoSink<T>> callback)
     *
     *
     * Flux.create - create is a more advanced form of programmatic creation of a Flux which is suitable for multiple emissions per round, even from multiple threads.
     *
     * It exposes a FluxSink, with its next, error, and complete methods.
     * Contrary to generate, it doesn’t have a state-based variant. On the other hand, it can trigger multi-threaded events in the callback.
     *
     *
     * public static <T> Flux<T> create(Consumer <? super FluxSink<T>> emitter)
     *
     * public static <T> Flux<T> create(Consumer <? super FluxSink<T>> emitter,
     *                                  FluxSink.OverflowStrategy backpressure
     */
    //@formatter:on
    private static final boolean docHolder = false;

    @Test
    public void create_mono() {
        Mono<String> create = Mono.create(emitter -> {
//            emitter.success(); // <-- empty
//            emitter.error(new RuntimeException()); // <-- error
            emitter.success("one value");
        });

        create
                .subscribe(event -> log.info("Got event {}", event),
                        ex -> log.error("Error callback.", ex),
                        () -> log.info("No more events."));
    }

    @Test
    public void create_flux() {
        Flux<String> create = Flux.create(emitter -> {
//            emitter.complete(); // <-- complete
//            emitter.error(new RuntimeException()); // <-- error
            emitter.next(String.valueOf(System.currentTimeMillis()));
        });

        create
                .take(1)
                .subscribe(event -> log.info("Got event {}", event),
                        ex -> log.error("Error callback.", ex),
                        () -> log.info("No more events."));
    }

    @Test
    public void generate_errorCallback() {
        Flux<Integer> flux = Flux.create(emitter -> {
            try {
                for (int i = -5; i < 6; i++) {
                    emitter.next(100 / i);//<-- Exception here when i == 0
                }
                emitter.complete();
            } catch (Exception ex) {
                emitter.error(ex);
            }
        });

        flux
                .subscribe(event -> log.info("Got event {}", event),
                        ex -> log.error("Error callback.", ex),
                        () -> log.info("No more events."));
    }


    //todo next example for Spring Event Listener https://stackoverflow.com/questions/60965045/how-to-bridge-between-a-non-reactive-spring-eventlistener-and-a-reactive-flux or maybe when next ?
    //todo
    @Test
    public void todooo() {
//        Flux<String> bridge = Flux.create(sink -> {
//            sink.onRequest(n -> channel.poll(n)) //1 poll for events form the channel when requests are made with the given number, "n" - the number of items requested
//                    .onCancel(channel::cancel) //call channel's cancel method when the Flux is cancelled
//                    .onDispose(channel::close); //the channel.close method is given to onDispose to be invoked for complete, error, cancel
//            channel.register(sink::next); //register the sink's "next" methods as a listerer to channel
//        });
    }

    //todo https://projectreactor.io/docs/core/release/reference/#producing.create ten WARNING
    @Test
    public void warning() {
//        subscrineOn(Scheduler, false)
    }
    //todo write example that spring REST api, if someone send request, then event and then something happened, or database example


}
