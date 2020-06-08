package stobiecki.tamingtheasynchronousbeast.ex05_backpressure_and_ways_to_reshape_requests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
public class Ex02_OperatorsThatChangeTheDemandFromDownstream {

    // One thing to keep in mind is that demand expressed at the subscribe level can be reshaped by each operator in the upstream chain

    @Test
    public void operatorThatChangeTheDemandFromDownstream_buffer() {
        Flux.range(1, 10)
                .doOnRequest(r -> log.info("request2 of {}", r)) // <-- request 2x3 = 6
                .buffer(3)
                .doOnRequest(r -> log.info("request1 of {}", r))
                .subscribe(new BaseSubscriber<List<Integer>>() {

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        request(2); // <-- request 2
                    }

                    @Override
                    public void hookOnNext(List<Integer> next) {
                        log.info("Received {}", next);
                    }
                });
    }

    @Test
    public void operatorThatChangeTheDemandFromDownstream_limitRate() {
        Flux.range(1, 10)
                .doOnRequest(r -> log.info("request2 of {}", r))
                .limitRate(2)
                .doOnRequest(r -> log.info("request1 of {}", r))
                .subscribe(new BaseSubscriber<Integer>() {

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        request(7);
                    }

                    @Override
                    public void hookOnNext(Integer next) {
                        log.info("Next {}", next);
                    }
                });
    }

    @Test
    public void operatorThatChangeTheDemandFromDownstream_limitRate2() {
        Flux.range(1, 10)
                .doOnRequest(r -> log.info("request2 of {}", r))
                .limitRate(3, 1)
                .doOnRequest(r -> log.info("request1 of {}", r))
                .subscribe(new BaseSubscriber<Integer>() {

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        request(7);
                    }

                    @Override
                    public void hookOnNext(Integer next) {
                        log.info("Next {}", next);
                    }
                });
    }

    @Test
    public void operatorThatChangeTheDemandFromDownstream_limitRequest() {
        Flux.range(1, 10)
                .doOnRequest(r -> log.info("request2 of {}", r))
                .limitRequest(5)
                .doOnRequest(r -> log.info("request1 of {}", r))
                .subscribe(new BaseSubscriber<Integer>() {

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        request(7);
                    }

                    @Override
                    public void hookOnNext(Integer next) {
                        log.info("Next {}", next);
                    }
                });
    }

}
