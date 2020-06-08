package stobiecki.tamingtheasynchronousbeast.ex04_combining_reactive_types;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
public class Ex02_Karaoke {

    private static final int TIME_PER_LETTER = 120;
    public static final String LYRICS = "Maybe you can't understand;\n" +
            "Slow down a bit for me;\n" +
            "Take me to wonderland;\n" +
            "Go where we want to be;";
    public static final String REGEXP_SPLIT_TEXT = "\\s";

    @Test
    public void karaokePublisherTest(){
         karaokePublisher(LYRICS)
                 .doOnNext(System.out::println)
                 .blockLast();
    }

    private Flux<String> karaokePublisher(String lyrics){
        return Flux.fromArray(lyrics.split(REGEXP_SPLIT_TEXT))
                .map(String::length)
                .map(length -> length * TIME_PER_LETTER)
                .scan(Math::addExact)
                .flatMap(wordSummaryDelay -> Flux
                        .just(wordSummaryDelay)
                        .delayElements(Duration.ofMillis(wordSummaryDelay)))
                .startWith(0)// <-- delay should be after a word print
                .zipWith(splitText(lyrics), (delay, word) -> word);
    }

    private Flux<String> splitText(String text){
        return Flux.fromArray(text.split(REGEXP_SPLIT_TEXT));
    }
}
