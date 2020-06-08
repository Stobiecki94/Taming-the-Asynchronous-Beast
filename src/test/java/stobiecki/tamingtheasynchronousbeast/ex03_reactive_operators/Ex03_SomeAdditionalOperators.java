package stobiecki.tamingtheasynchronousbeast.ex03_reactive_operators;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
public class Ex03_SomeAdditionalOperators {

    @Test
    public void shouldCountFromTenToZero() {
        Flux.interval(Duration.ofSeconds(1))
                .map(number -> 10 - number)
                .doOnNext(number -> log.info("counting down {}", number))
                .take(11)
                .log("Reactor")
                .blockLast();
    }

    @Test
    public void scan() {
        naturalNumbers()
                .take(10)
                .scan((current, next) -> next + current)//<-- emits intermediate values into stream
                .subscribe(number -> log.info("Number emitted from scan {}", number));
    }

    @Test
    public void shouldScanStringObservable() {
        Flux.just("Everything", " ", "is", " ", "a", " ", "stream")
                .scan(String::concat)
                .subscribe(log::info);
    }

    @Test
    public void shouldReduceNaturalNumbers() {
        naturalNumbers()
                .takeWhile(number -> number <= 100)
                .reduce(Math::addExact)
                .doOnNext(sum -> log.info("Sum of numbers from 1 to 100 is equal to {}", sum))
                .subscribe();
    }

    @Test
    public void shouldConcatText() {
        Flux.just("Everything", " ", "is", " ", "a", " ", "stream")
                .reduce((current, next) -> current + next)
                .doOnNext(log::info)
                .subscribe();
    }

    @Test
    public void shouldUseWindow(){
        naturalNumbers()
                .window(10)//<-- use micro batch, returns  Flux<Flux<T>>
                .take(3)
                .flatMap(window -> window.reduce((x, y) -> x + y))//<-- sum element in each window
                .log()
                .subscribe(event -> log.info("Got number {}", event));
    }

    private Flux<Integer> naturalNumbers() {
        return Flux.generate(() -> 0, (state, sink) -> {
            sink.next(state);
            return ++state;
        });
    }
}
