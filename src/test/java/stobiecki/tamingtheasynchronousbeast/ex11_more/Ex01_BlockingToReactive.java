package stobiecki.tamingtheasynchronousbeast.ex11_more;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import stobiecki.tamingtheasynchronousbeast.ex11_more.domain.User;
import stobiecki.tamingtheasynchronousbeast.ex11_more.repository.BlockingUserRepository;
import stobiecki.tamingtheasynchronousbeast.ex11_more.repository.ReactiveRepository;
import stobiecki.tamingtheasynchronousbeast.ex11_more.repository.ReactiveUserRepository;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class Ex01_BlockingToReactive {

    @Test
    public void slowPublisherFastSubscriber() {
        BlockingUserRepository repository = new BlockingUserRepository();

        // a Flux for reading all users from the blocking repository deferred until the flux is subscribed, and run it with an elastic scheduler
        Flux<User> flux = Flux.defer(() -> Flux.fromIterable(repository.findAll()))
                .subscribeOn(Schedulers.elastic());

        assertThat(repository.getCallCount()).isEqualTo(0).withFailMessage("The call to findAll must be deferred until the flux is subscribed");
        StepVerifier.create(flux)
                .expectNext(User.SKYLER, User.JESSE, User.WALTER, User.SAUL)
                .verifyComplete();
    }

    @Test
    public void fastPublisherSlowSubscriber() {
        ReactiveRepository<User> reactiveRepository = new ReactiveUserRepository();
        BlockingUserRepository blockingRepository = new BlockingUserRepository(new User[]{});

        //Insert users contained in the Flux parameter in the blocking repository using an elastic scheduler and return a Mono<Void> that signal the end of the operation
        Mono<Void> complete = reactiveRepository.findAll()
                .publishOn(Schedulers.elastic())
                .doOnNext(blockingRepository::save)
                .then();

        assertThat(blockingRepository.getCallCount()).isEqualTo(0);
        StepVerifier.create(complete)
                .verifyComplete();
        Iterator<User> it = blockingRepository.findAll().iterator();
        assertThat(it.next()).isEqualTo(User.SKYLER);
        assertThat(it.next()).isEqualTo(User.JESSE);
        assertThat(it.next()).isEqualTo(User.WALTER);
        assertThat(it.next()).isEqualTo(User.SAUL);
        assertThat(it.hasNext()).isFalse();
    }

}
