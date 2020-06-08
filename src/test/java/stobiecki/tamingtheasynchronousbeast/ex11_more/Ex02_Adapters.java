package stobiecki.tamingtheasynchronousbeast.ex11_more;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import stobiecki.tamingtheasynchronousbeast.ex11_more.domain.User;
import stobiecki.tamingtheasynchronousbeast.ex11_more.repository.ReactiveRepository;
import stobiecki.tamingtheasynchronousbeast.ex11_more.repository.ReactiveUserRepository;

import java.util.concurrent.CompletableFuture;

public class Ex02_Adapters {

    ReactiveRepository<User> repository = new ReactiveUserRepository();

    @Test
    public void adaptToFlowable() {
        Flux<User> flux = repository.findAll();

        Flowable<User> flowable = Flowable.fromPublisher(flux);

        StepVerifier.create(Flux.from(flowable))
                .expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
                .verifyComplete();
    }

    @Test
    public void adaptToObservable() {
        Flux<User> flux = repository.findAll();

        Observable<User> observable = Observable.fromPublisher(flux);

        StepVerifier.create(Flux.from(observable.toFlowable(BackpressureStrategy.BUFFER)))
                .expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
                .verifyComplete();
    }

    @Test
    public void adaptToSingle() {
        Mono<User> mono = repository.findFirst();

        Single<User> single = Single.fromPublisher(mono);

        StepVerifier.create(Mono.from(single.toFlowable()))
                .expectNext(User.SKYLER)
                .verifyComplete();
    }

    @Test
    public void adaptToCompletableFuture() {
        Mono<User> mono = repository.findFirst();

        CompletableFuture<User> future = mono.toFuture();

        StepVerifier.create(Mono.fromFuture(future))
                .expectNext(User.SKYLER)
                .verifyComplete();
    }
}
