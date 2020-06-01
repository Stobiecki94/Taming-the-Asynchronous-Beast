package stobiecki.tamingtheasynchronousbeast.ex02_creating_reactive_types.ex02_2_programmatically_creating;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Ex05_AsynchronousButSingleThreaded_Push {

    /**
     * push is a middle ground between generate and create which is suitable for processing events from a single producer
     *
     * It is similar to create in the sense that it can also be asynchronous and can manage backpressure using any of the overflow strategies supported by create.
     * However, only one producing thread may invoke next, complete or error at a time.
     *
     * 	public static <T> Flux<T> push(Consumer<? super FluxSink<T>> emitter)
     *
     * 	public static <T> Flux<T> push(Consumer<? super FluxSink<T>> emitter, OverflowStrategy backpressure)
     */


    @Test
    @SneakyThrows
    public void push_asBridgeToAnExistingSingleThreadedMultiValuesAsyncApi_with_theReactiveWorld() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        EventProcessor<String> eventProcessor = new EventProcessor<>();

        Flux<String> bridge = Flux.push(emitter -> {
            MyEventListener<String> myEventListener = new MyEventListener<String>() {
                public void onDataChunk(List<String> chunk) {
                    log.info("on data chunk {}", chunk);
                    chunk.forEach(emitter::next);
                }

                public void processComplete() {
                    emitter.complete();
                }
            };

            eventProcessor.register(myEventListener);

            emitter.onDispose(() -> eventProcessor.removeListener(myEventListener)); // <-- clean up
        });

        bridge
                .subscribe(
                        log::info,
                        error -> log.error("Error", error),
                        () -> {
                            log.info("It is true! Done");
                            countDownLatch.countDown();
                        });

        sleepForAWhile();
        log.info("Starting producing chunks");

        new Thread(() -> {
            eventProcessor.newChunk(asList("Reactor", "is"));
            eventProcessor.newChunk(asList("the", "best"));
            eventProcessor.complete();
        }, "event-processor-thread")
                .start();

        boolean awaitASecond = countDownLatch.await(1, TimeUnit.SECONDS);
        assertThat(awaitASecond).withFailMessage("Await timed out!").isTrue();
    }

    @SneakyThrows
    private void sleepForAWhile() {
        Thread.sleep(100);
    }
}
