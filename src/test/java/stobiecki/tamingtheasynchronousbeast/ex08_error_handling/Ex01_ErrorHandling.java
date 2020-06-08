package stobiecki.tamingtheasynchronousbeast.ex08_error_handling;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import stobiecki.tamingtheasynchronousbeast.ex11_more.domain.User;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class Ex01_ErrorHandling {

    // In Reactive Streams, errors are terminal events.
    // As soon as an error occurs, it stops the sequence and gets propagated down the chain of operators to the last step, the Subscriber you defined and its onError method.

    // Such errors should still be dealt with at the application level. For instance, you might display an error notification in a UI or send a meaningful error payload in a REST endpoint.
    // For this reason, the subscriber’s onError method should always be defined.

    // Before you learn about error-handling operators, you must keep in mind that any error in a reactive sequence is a terminal event.
    // Even if an error-handling operator is used, it does not let the original sequence continue.
    // Rather, it converts the onError signal into the start of a new sequence (the fallback one). In other words, it replaces the terminated sequence upstream of it.

    @Test
    public void staticFallbackValue() {
        Flux.range(0, 100)
                .map(this::doSomethingDangerous)
                .onErrorReturn("RECOVERED")
                .doOnNext(next -> log.info("Next {}", next))
                .blockLast();
    }

    @Test
    public void staticFallbackValueWithPredicate() {
        Flux.range(0, 100)
                .map(this::doSomethingDangerous)
                .onErrorReturn(e -> e.getMessage().contains("Boom"), "RECOVERED")
                .doOnNext(next -> log.info("Next {}", next))
                .blockLast();
    }

    @Test
    public void fallbackMethod() {
        Mono.just("input")
                .flatMap(input -> getFromDb(input)
                        .onErrorResume(e -> getFromCache(input)))
                .doOnNext(log::info)
                .block();
    }

    @Test
    public void catchAndRethrow() {
        Mono.just("input")
                .flatMap(this::getFromDb)
                .onErrorResume(original -> Mono.error(new RuntimeException(original)))
                .doOnError(error -> log.error("Error", error))
                .block();
    }

    @Test
    public void handleCheckedExceptions() {
        Mono<User> mono = Mono.just(User.SAUL)
                .map(user -> {
                    try {
                        return capitalizeUser(user);
                    }
                    catch (GetOutOfHereException e) {
                        throw Exceptions.propagate(e);
                    }
                });

        StepVerifier.create(mono)
                .verifyError(GetOutOfHereException.class);
    }

    //a'la try with resources
    @Test
    public void using() {
        AtomicBoolean isDisposed = new AtomicBoolean();
        Disposable disposableInstance = new Disposable() {
            @Override
            public void dispose() {
                isDisposed.set(true);
            }

            @Override
            public String toString() {
                return "DISPOSABLE";
            }
        };

        Flux.using(
                () -> disposableInstance,
                disposable -> Flux.just(disposable.toString()),
                Disposable::dispose
        );
    }

    private Mono<String> getFromCache(String input) {
        return Mono.just("Value from cache");
    }

    private Mono<String> getFromDb(String input) {
        return Mono.error(new RuntimeException("Cannot connect to DB"));
    }


    private String doSomethingDangerous(int input) {
        Random random = new Random();
        if (random.nextDouble() < 0.1) {
            throw new RuntimeException("Boom");
        }
        return "success";
    }

    User capitalizeUser(User user) throws GetOutOfHereException {
        if (user.equals(User.SAUL)) {
            throw new GetOutOfHereException();
        }
        return new User(user.getUsername(), user.getFirstname(), user.getLastname());
    }

    protected final class GetOutOfHereException extends Exception {
        private static final long serialVersionUID = 0L;
    }
}
