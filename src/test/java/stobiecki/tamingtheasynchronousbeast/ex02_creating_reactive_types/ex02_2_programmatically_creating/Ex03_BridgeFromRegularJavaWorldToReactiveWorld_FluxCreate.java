package stobiecki.tamingtheasynchronousbeast.ex02_creating_reactive_types.ex02_2_programmatically_creating;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Ex03_BridgeFromRegularJavaWorldToReactiveWorld_FluxCreate {

    /**
     * TIP: create can be very useful to bridge an existing API with the reactive world - such as an asynchronous API based on listeners.
     */
    private static final boolean docHolder = false;

    @Test
    @SneakyThrows
    public void listenerBasedApi_processDataByChunks() {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        EventProcessor<String> eventProcessor = new EventProcessor<>();

        // Create the FLUX from the Event listener
        Flux<String> bridge = Flux.create(emitter -> {
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
//                .subscribeOn(Schedulers.single(), false) //variant: requestOnSeparateThread = false will use the Scheduler thread for the create and still let data flow by performing request in the original thread
                .publishOn(Schedulers.boundedElastic())//todo check that above!
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
        }, "event-processor-thread-1")
                .start();

        sleepForAWhile();

        new Thread(() -> {
            eventProcessor.newChunk(asList("the", "best"));
            eventProcessor.complete();
        }, "event-processor-thread-2")
                .start();

        boolean awaitASecond = countDownLatch.await(1, TimeUnit.SECONDS);
        assertThat(awaitASecond).withFailMessage("Await timed out!").isTrue();
    }


    @SneakyThrows
    private void sleepForAWhile() {
        Thread.sleep(100);
    }
}
