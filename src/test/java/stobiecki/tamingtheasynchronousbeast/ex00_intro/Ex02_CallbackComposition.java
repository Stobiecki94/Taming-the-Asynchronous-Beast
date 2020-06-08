package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.Mono;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.AsynchronousCallbackServiceImpl;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.AsynchronousReactiveServiceImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Ex02_CallbackComposition {

    private static final String USER_ID = "userId";

    // scenario: get age, address and hobby simultaneously

    @Test
    @SneakyThrows
    public void executeParallelAndCombine_Callback() {
        AsynchronousCallbackService service = new AsynchronousCallbackServiceImpl(Executors.newFixedThreadPool(2));

        CountDownLatch completionSignal = new CountDownLatch(3);

        Holder<Integer> ageHolder = new Holder<>();
        Holder<String> addressHolder = new Holder<>();
        Holder<String> hobbyHolder = new Holder<>();

        //three operations happens in parallel
        service.findAge(USER_ID, age -> {
            ageHolder.setValue(age);
            completionSignal.countDown();
        }, ex -> log.error("Error handling here", ex));
        service.findAddress(USER_ID, address -> {
            addressHolder.setValue(address);
            completionSignal.countDown();
        }, ex -> log.error("Error handling here", ex));
        service.findHobby(USER_ID, hobby -> {
            hobbyHolder.setValue(hobby);
            completionSignal.countDown();
        }, ex -> log.error("Error handling here", ex));


        //operations may accomplish in different time. It is our responsibility to wait for completion of all opearations
        boolean await10Seconds = completionSignal.await(10, TimeUnit.SECONDS);
        assertThat(await10Seconds).withFailMessage("Await timed out!").isTrue();

        log.info("Age: {} address: {} hobby: {}", ageHolder.getValue(), addressHolder.getValue(), hobbyHolder.getValue());
    }
    // Age: 10 address: Warsaw, Woloska 24 hobby: Football

    @Test
    @SneakyThrows
    public void executeParallelAndCombine_Reactor() {
        AsynchronousReactiveServiceImpl service = new AsynchronousReactiveServiceImpl();

        Mono.zip(service.findAge(USER_ID),
                service.findAddress(USER_ID),
                service.findHobby(USER_ID))
                .doOnNext(next -> log.info("Age: {} address: {} hobby: {}", next.getT1(), next.getT2(), next.getT3()))
                .block();
    }
    // Age: 10 address: Warsaw, Woloska 24 hobby: Football

    @Data
    static class Holder<T> {
        private T value;
    }
}
