package stobiecki.tamingtheasynchronousbeast.ex08_error_handling;

import org.junit.Test;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import stobiecki.tamingtheasynchronousbeast.ex11_more.domain.User;

public class Ex02_Rethrowing {


    @Test
    public void handleCheckedExceptions() {
        Flux<User> flux = capitalizeMany(Flux.just(User.SAUL, User.JESSE));

        StepVerifier.create(flux)
                .verifyError(GetOutOfHereException.class);
    }


    Flux<User> capitalizeMany(Flux<User> flux) {
        return flux.map(user -> {
            try {
                return capitalizeUser(user);
            }
            catch (GetOutOfHereException e) {
                throw Exceptions.propagate(e); // <-- todo!
            }
        });
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
