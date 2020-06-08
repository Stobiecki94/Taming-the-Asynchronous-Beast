package stobiecki.tamingtheasynchronousbeast.ex01_subscription;

import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Ex01_LambdaBasedSubscribeVariants {

    @Mock
    private TemperatureService temperatureService;

    /**
     * subscribe and trigger the sequence
     */
    @Test
    public void test01_shouldSubscribe() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe();// <-- produces no visible output, but it does work
    }

    /**
     * Do something with each produced value
     */
    @Test
    public void test02_shouldGetTemperature() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe(temp -> log.info("Current temperature: {}", temp));
    }

    /**
     * Deal with values but also react to an error
     */
    @Test
    public void test03_subscribeWithErrorCallback() {
        given(temperatureService.getTemperature("Berlin")).willReturn(cannotConnectToBerlinError());

        temperatureService
                .getTemperature("Berlin")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong: {}", error.getMessage(), error));
    }

    /**
     * Deal with values and errors but also run some code when the sequence successfully completes
     */
    @Test
    public void test04_subscribeWithErrorCallbackAndCompleteConsumer() {
        //Warsaw -> return 3 temperature measurements and complete (stream has finished)
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong: {} <-- error signal", error.getMessage()),
                        () -> log.info("No more temperature data  <-- completion signal"));

    }

    /**
     * Error signals and completion signals are both terminal events and are exclusive of one another (you never get both)
     */
    @Test
    public void test05_subscribeWithErrorCallbackAndCompleteConsumer2() {
        //Warsaw -> return 3 temperature measurements and complete (stream has finished)
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        //Berlin -> return 3 temperature measurements and error
        given(temperatureService.getTemperature("Berlin")).willReturn(return3TemperatureMeasurementsAndConnectionLostError());

        log.info("Warsaw");
        temperatureService
                .getTemperature("Warsaw")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong: {} <-- error signal", error.getMessage()),
                        () -> log.info("No more temperature data  <-- completion signal"));

        log.info("--------------------------------------------------------------");
        log.info("Berlin");

        temperatureService
                .getTemperature("Berlin")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong: {} <-- error signal", error.getMessage()),
                        () -> log.info("No more temperature data  <-- completion signal"));

        //        note: Error signals and completion signals are both terminal events and are exclusive of one another (you never get both).
        log.info("note: Error signals and completion signals are both terminal events and are exclusive of one another (you never get both)");
    }

    /**
     * Deal with values and errors and successful completion but also do something with the Subscription produced by this subscribe call
     */
    @Test
    public void test06_subscribeWithSubscriptionConsumer() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong", error),
                        () -> log.info("No more temperature data"),
                        subscription -> log.info("Subscription: {}", subscription));
//        Note: That variant requires you to do something with the Subscription (perform a request(long) on it or cancel() it).
    }

    @Test
    public void test07_subscribeWithSubscriptionConsumer_cancel() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong", error),
                        () -> log.info("No more temperature data"),
                        sub -> sub.cancel());
    }

    @Test
    public void test08_subscribeWithSubscriptionConsumer_request() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong", error),
                        () -> log.info("No more temperature data"),
                        sub -> sub.request(2));
    }

    @Test
    public void test09_subscribeWithSubscriptionConsumer_request2() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong", error),
                        () -> log.info("No more temperature data"),
                        sub -> sub.request(1_000_000));
//        Note: If 'Subscription consumer' is not specified, then: The subscription will request unbounded demand (Long.MAX_VALUE).
    }

    @Test
    public void test10_subscribeMonoEmpty() {
        Mono.empty()
                .subscribe(
                        next -> log.info("Next element: {}", next),
                        error -> log.error("Error", error),
                        () -> log.info("Completed"));
    }

    @Test
    public void test11_subscribeFluxEmpty() {
        Flux.empty()
                .subscribe(
                        next -> log.info("Next element: {}", next),
                        error -> log.error("Error", error),
                        () -> log.info("Completed"));
    }

    @Test
    public void test12_subscribeVariants_summary() {
        Flux<String> fruits = Flux.just("Apple", "Banana");

        log.info("--------------------------------------------------------------------------------------------------------------------------------------");
        log.info("Subscribe and trigger the sequence");
        fruits.subscribe(); //"The preceding code produces no visible output, but it does work."

        log.info("--------------------------------------------------------------------------------------------------------------------------------------");
        log.info("Do something with each produced value.");
        fruits.subscribe(
                next -> log.info("Next element: {}", next));

        log.info("--------------------------------------------------------------------------------------------------------------------------------------");
        log.info("Deal with values but also react to an error.");
        fruits.subscribe(
                next -> log.info("Next element: {}", next),
                error -> log.error("Error", error));

        log.info("--------------------------------------------------------------------------------------------------------------------------------------");
        log.info("Deal with values and errors but also run some code when the sequence successfully completes.");
        fruits.subscribe(
                next -> log.info("Next element: {}", next),
                error -> log.error("Error", error),
                () -> log.info("Completed"));

        log.info("--------------------------------------------------------------------------------------------------------------------------------------");
        log.info("Deal with values and errors and successful completion but also do something with the Subscription produced by this subscribe call.");
        fruits.subscribe(
                next -> log.info("Next element: {}", next),
                error -> log.error("Error", error),
                () -> log.info("Completed"),
                subscription -> {
                    log.info("Subscription: {}", subscription);
                    subscription.cancel();
                });
    }

    private Flux<Double> cannotConnectToBerlinError() {
        return Flux.error(new RuntimeException("Cannot connect to Berlin weather server"));
    }

    private Flux<Double> return3TemperatureMeasurementsAndConnectionLostError() {
        return Flux.generate(
                () -> 0,
                (counter, sink) -> {
                    if (counter >= 3) {
                        sink.error(new RuntimeException("Lost connection to Berlin"));
                    }
                    sink.next(10.0 + counter);

                    return counter + 1;
                }
        );
    }


}
