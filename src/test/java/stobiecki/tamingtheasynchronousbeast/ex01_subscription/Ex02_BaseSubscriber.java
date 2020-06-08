package stobiecki.tamingtheasynchronousbeast.ex01_subscription;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import static org.mockito.BDDMockito.given;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class Ex02_BaseSubscriber {

    @Mock
    private TemperatureService temperatureService;

    @Before
    public void setUp() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));
    }

    @Test
    public void unboundedSubscriber() {
        temperatureService
                .getTemperature("Warsaw")
//                .log()
                .subscribe(new UnboundedSubscriber<>());
    }

    @Test
    public void boredAfterFirstElementSubscriber() {
        temperatureService
                .getTemperature("Warsaw")
//                .log()
                .subscribe(new BoredSubscriber<>()); //<-- this subscriber will cancel subscription

//        A cancellation is a signal that the source should stop producing elements.
//        However, it is NOT guaranteed to be immediate: Some sources might produce elements so fast that they could complete even before receiving the cancel instruction.
    }

    @Test
    public void requestOneMoreWhenReceiveSubscriber() {
        temperatureService
                .getTemperature("Warsaw")
//                .log()
                .subscribe(new TemperatureSubscriber<>());
    }


    /**
     * Note: reactor.core.publisher.BaseSubscriber is the recommended abstract class for user-defined Subscribers in Reactor.
     */
    static class TemperatureSubscriber<T> extends BaseSubscriber<T> {

        // when the subscription is established
        public void hookOnSubscribe(Subscription subscription) {
            log.info("Subscribed");
            log.info("Requesting 1 element");
            request(1); //how much data to request
        }

        public void hookOnNext(T value) {
            log.info("Next: {}", value);
            log.info("Requesting 1 element more");
            request(1); //how much data to request
        }

        public void hookOnComplete() {
            log.info("Done");
        }

        public void hookOnError(Throwable throwable) {
            log.error("Error", throwable);
        }

        public void hookOnCancel() {
            log.warn("Cancel");
        }

        protected void hookFinally(SignalType type) {
            log.info("Finished with type: {}", type);
        }
    }


    /**
     * BaseSubscriber offers a requestUnbounded() method to switch to unbounded mode
     */
    static class UnboundedSubscriber<T> extends BaseSubscriber<T> {

        // when the subscription is established
        @Override
        public void hookOnSubscribe(Subscription subscription) {
            log.info("Subscribed");
            log.info("Requesting unbounded");
            requestUnbounded(); // == equivalent to request(Long.MAX_VALUE))
        }

        @Override
        public void hookOnNext(T value) {
            log.info("Next: {}", value);
        }
    }

    /**
     * BaseSubscriber offers a cancel() method.
     */
    static class BoredSubscriber<T> extends BaseSubscriber<T> {

        // when the subscription is established
        @Override
        public void hookOnSubscribe(Subscription subscription) {
            log.info("Subscribed");
            log.info("Requesting unbounded");
            requestUnbounded();
        }

        @Override
        public void hookOnNext(T value) {
            log.info("I am bored, cancelling after received: {}", value);
            cancel();
        }
    }
}
