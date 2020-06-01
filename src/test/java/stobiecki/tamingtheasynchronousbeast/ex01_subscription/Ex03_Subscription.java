package stobiecki.tamingtheasynchronousbeast.ex01_subscription;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

@Slf4j
public class Ex03_Subscription {

    @Test
    public void publisherWithTwoSubscribers() {
        Flux<String> fruitsPublisher = Flux.just("Apple", "Banana", "Orange")
                .doOnSubscribe(subscription -> log.info("on subscribe, subscription: {}", subscription))
                .doOnComplete(() -> log.info("completed"));

        fruitsPublisher.subscribe(next -> log.info("First subscriber get '{}'", next));

        fruitsPublisher.subscribe(next -> log.info("Second subscriber get '{}'", next));
    }
}
