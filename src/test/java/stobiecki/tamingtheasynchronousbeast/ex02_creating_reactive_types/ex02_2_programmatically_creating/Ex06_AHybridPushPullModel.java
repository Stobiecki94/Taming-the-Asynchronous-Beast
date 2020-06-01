package stobiecki.tamingtheasynchronousbeast.ex02_creating_reactive_types.ex02_2_programmatically_creating;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static java.util.Arrays.asList;

@Slf4j
public class Ex06_AHybridPushPullModel {

    /**
     * Most Reactor operators, like create, follow a hybrid push/pull model.
     * What we mean by that is that despite most of the processing being asynchronous (suggesting a push approach), there is a small pull component to it: the request.
     * <p>
     * The consumer pulls data from the source in the sense that it won’t emit anything until first requested.
     * The source pushes data to the consumer whenever it becomes available, but within the bounds of its requested amount.
     * <p>
     * push() and create() both allow to set up an onRequest consumer in order to manage the request amount and to ensure that data is pushed through the sink only when there is pending request.
     */
    private static final boolean docHolder = false;

    /**
     * get N records from the beginning (if history does not contain enough data, then keep open and listen....
     */
    @Test
    @SneakyThrows
    public void pushPull_getNRecordsFromTheBeginning() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        EventProcessor<String> eventProcessor = new EventProcessor<>();
        HistoryRepository historyRepository = new HistoryRepository();

        Flux<String> bridge = Flux.create(sink -> {
            MyEventListener<String> myEventListener = new MyEventListener<String>() {
                public void onDataChunk(List<String> chunk) {
                    chunk.forEach(sink::next);
                }

                public void processComplete() {
                    sink.complete();
                }
            };

            sink.onRequest(n -> {
                List<String> historyChunks = historyRepository.getHistory(n - 4); //we assume that history contains N-4 requested records
                historyChunks.forEach(sink::next);
            });

            eventProcessor.register(myEventListener);

            sink.onDispose(() -> eventProcessor.removeListener(myEventListener)); // <-- clean up
        });

        bridge
                .subscribe(
                        log::info,
                        error -> log.error("Error", error),
                        () -> {
                            log.info("Done");
                            countDownLatch.countDown();
                        },
                        subscription -> subscription.request(10));

        eventProcessor.newChunk(asList("a new chunk", "a new chunk"));
        eventProcessor.newChunk(asList("a new chunk", "a new chunk"));
        eventProcessor.newChunk(asList("a new chunk", "a new chunk"));
        eventProcessor.newChunk(asList("a new chunk", "a new chunk"));
        eventProcessor.newChunk(asList("a new chunk", "a new chunk"));
    }

    static class HistoryRepository {

        public List<String> getHistory(long n) {
            return LongStream.range(1, n + 1)
                    .boxed()
                    .map(next -> "historical chunk " + next)
                    .collect(Collectors.toList());
        }
    }

}
