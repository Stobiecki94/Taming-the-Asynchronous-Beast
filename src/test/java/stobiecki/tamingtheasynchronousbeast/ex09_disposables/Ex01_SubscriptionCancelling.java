package stobiecki.tamingtheasynchronousbeast.ex09_disposables;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.Disposables;

@Slf4j
public class Ex01_SubscriptionCancelling {

//     Note:
//     All lambda-based variants of subscribe() have a Disposable return type.
//     In this case, the Disposable interface represents the fact that the subscription can be cancelled, by calling its dispose() method.


//    For a Flux or Mono, cancellation is a signal that the source should stop producing elements.
//        However, it is NOT guaranteed to be immediate: Some sources might produce elements so fast that they could complete even before receiving the cancel instruction.

//    reactor.core.Disposables - some utilities around reactor.core.Disposable

//    4.3.2

    @Test
    public void disposablesSwap() {
        Disposables.swap(); //creates a Disposable wrapper that lets you atomically cancel and replace a concrete Disposable
        //todo
    }


    @Test
    public void disposablesComposite() {
        Disposables.composite(); //collect several Disposable
        //todo
    }

    @Test
    public void disposablesSingle() {
        Disposables.single(); //a new Disposable initially not yet disposed
        //todo
    }

    @Test
    public void disposablesNever() {
        Disposables.never(); //a new Disposable that can never be disposed
        //todo
    }
}
