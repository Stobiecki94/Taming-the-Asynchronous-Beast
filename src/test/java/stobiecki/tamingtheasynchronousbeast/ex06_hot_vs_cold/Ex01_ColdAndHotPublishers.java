package stobiecki.tamingtheasynchronousbeast.ex06_hot_vs_cold;

import lombok.SneakyThrows;
import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class Ex01_ColdAndHotPublishers {

    /**
     * This example demostrates Cold Publishers
     * You will see that even when second
     * subscriber starts late, still he gets all
     * the values from the beginning of the sequence
     */
    @Test
    public void exampleColdPublisher() {
        // Start a cold Publisher which emits 0,1,2 every sec.
        Flux<Long> flux =  Flux.interval(Duration.ofSeconds(1));
        // Let's subscribe to that with multiple subscribers.

        flux.subscribe(i -> System.out.println("first_subscriber received value:" + i));

        sleepForAWhile();

        // Let a second subscriber come after some time 3 secs here.
        flux.subscribe(i -> System.out.println("second_subscriber received value:" + i)); // <-  even when the second subscriber is subscribing 3 seconds later,
        // still it starts getting the values from the beginning all the way

        sleepForAWhile();
    }

    /**
     * This example demonstrates Hot Publishers
     * You will see that when second
     * subscriber starts late, it will only get
     * the values after it has subscribed and
     * not from the beginning of the sequence
     */
    @Test
    public void exampleHotPublisher() {
        // a cold Publisher which emits 0,1,2 every sec.
        Flux<Long> flux =  Flux.interval(Duration.ofSeconds(1));

        // Make the Publisher Hot
        ConnectableFlux<Long> connectableFlux = flux.publish();

        // Now that we have a handle on the hot Publisher
        // Let's subscribe to that with multiple subscribers.
        connectableFlux.subscribe(i -> System.out.println("first_subscriber received value:" + i));

        // Start firing events with .connect() on the published flux.
        connectableFlux.connect();

        sleepForAWhile();

        // Let a second subscriber come after some time 3 secs here.
        connectableFlux.subscribe(i -> System.out.println("second_subscriber received value:" + i)); // <-- the second subscriber only gets value from 3 seconds which is 3 onwards and misses, 0,1,2

        sleepForAWhile();
    }

    @SneakyThrows
    private void sleepForAWhile() {
        Thread.sleep(3_100);
    }
}
