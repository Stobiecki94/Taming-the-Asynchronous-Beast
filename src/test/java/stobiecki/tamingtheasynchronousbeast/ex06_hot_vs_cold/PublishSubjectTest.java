package stobiecki.tamingtheasynchronousbeast.ex06_hot_vs_cold;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import reactor.core.publisher.EmitterProcessor;

import static java.lang.System.out;

@Slf4j
public class PublishSubjectTest {

    //todo and others processors
    @SneakyThrows
    @Test
    public void publishSubject() {
        EmitterProcessor<String> s = EmitterProcessor.create();

        s.subscribe(out::println);
        s.onNext("First event.");
        s.onNext("Last event.");

        Thread.sleep(100);
        s.onComplete();
    }

}
