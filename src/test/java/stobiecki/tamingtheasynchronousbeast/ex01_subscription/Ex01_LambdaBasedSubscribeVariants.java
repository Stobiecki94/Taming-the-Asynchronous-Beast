package stobiecki.tamingtheasynchronousbeast.ex01_subscription;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.BDDMockito.given;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class Ex01_LambdaBasedSubscribeVariants {

    @Mock
    private TemperatureService temperatureService;

    @Test
    public void shouldSubscribeButForWhat() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe();
    }

    @Test
    public void shouldGetTemperature() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe(temp -> log.info("Current temperature: {}", temp));
    }

    @Test
    public void subscribeWithErrorCallback() {
        given(temperatureService.getTemperature("Berlin")).willReturn(Flux.error(new RuntimeException("Cannot connect to Berlin weather server")));

        temperatureService
                .getTemperature("Berlin")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong", error));
    }

    @Test
    public void subscribeWithErrorCallbackAndCompleteConsumer() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        given(temperatureService.getTemperature("Berlin")).willReturn(Flux.generate(
                () -> 0,
                (counter, sink) -> {
                    if(counter > 3) {
                        sink.error(new RuntimeException("Lost connection to Berlin"));
                    }
                    sink.next(10.0 + counter);

                    return counter + 1;
                }
        ));

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
    }

    @Test
    public void subscribeWithSubscriptionConsumer_fluxHangs() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong", error),
                        () -> log.info("No more temperature data"),
                        subscription -> log.info("Subscription: {}", subscription));
//        Note: That variant requires you to do something with the Subscription (perform a request(long) on it or cancel() it). Otherwise the Flux hangs.
    }

    @Test
    public void subscribeWithSubscriptionConsumer() {
        given(temperatureService.getTemperature("Warsaw")).willReturn(Flux.just(10.0, 15.5, 20.0));

        temperatureService
                .getTemperature("Warsaw")
                .subscribe(
                        temp -> log.info("Current temperature: {}", temp),
                        error -> log.error("Something went wrong", error),
                        () -> log.info("No more temperature data"),
                        sub -> sub.request(10));
//        Note: If 'Subscription consumer' is not specified, then: The subscription will request unbounded demand (Long.MAX_VALUE).
    }

    @Test
    public void subscribeMonoEmpty() {
        Mono.empty()
                .subscribe(
                        next -> log.info("Next element: {}", next),
                        error -> log.error("Error", error),
                        () -> log.info("Completed"));
    }

    @Test
    public void subscribeFluxEmpty() {
        Flux.empty()
                .subscribe(
                        next -> log.info("Next element: {}", next),
                        error -> log.error("Error", error),
                        () -> log.info("Completed"));
    }

    @Test
    public void subscribeVariants_summary() {
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


}
