package stobiecki.tamingtheasynchronousbeast.ex11_more;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

@Slf4j
public class BuildVsExecutionTrap {

    @Test
    @SneakyThrows
    public void buildVsExecutionTrap1() {
        Mono.just(blockingGet()); // <- executed even without subscribe
    }

    @Test
    @SneakyThrows
    public void buildVsExecutionTrap2() {
        Mono.fromCallable(this::blockingGet); // <- nothing happens until subscribe
    }

    @Test
    @SneakyThrows
    public void buildVsExecutionTrap3() {
        Mono.fromCallable(this::blockingGet) // <- nothing happens until subscribe
                .subscribe();
    }

    @Test
    @SneakyThrows
    public void buildVsExecutionTrap31() {
        nonBlockingGet(); // <-- there is no subscribe but...
    }
    //BUT this: INFO stobiecki.tamingtheasynchronousbeast.ex11_more.BuildVsExecutionTrap - Sending HTTP request <--- misleading log

    @Test
    @SneakyThrows
    public void buildVsExecutionTrap32() {
        Mono.defer(() -> nonBlockingGet());
    }

    @Test
    @SneakyThrows
    public void buildVsExecutionTrap4() {
        Mono.fromCallable(this::blockingGet)
                .defaultIfEmpty(fallback()) // <- executed even if not empty
                .subscribe();
    }

    @Test
    @SneakyThrows
    public void buildVsExecutionTrap5() {
        Mono.fromCallable(this::blockingGet)
                .defaultIfEmpty(fallback()); // <- executed even without subscribe
    }

    @Test
    @SneakyThrows
    public void buildVsExecutionTrap6() {
        Mono.fromCallable(this::blockingGet)
                .switchIfEmpty(Mono.fromCallable(() -> fallback()));
        //does nothing because not subscribed
    }

    @Test
    @SneakyThrows
    public void buildVsExecutionTrap7() {
        Mono.fromCallable(this::blockingGet)
                .switchIfEmpty(Mono.defer(() -> Mono.just(fallback())));
        //does nothing because not subscribed
    }

    @SneakyThrows
    private String fallback() {
        log.info("Executing fallback");
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("https://gfieast.com/en//");
        log.info("executed FALLBACK GET request");
        httpClient.execute(request);
        return "Fallback";
    }

    @SneakyThrows
    private String blockingGet() {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("https://gfieast.com/pl/aktualnosci/");
        log.info("executed GET request");
        httpClient.execute(request);
        return "Success";
    }


    private Mono<String> nonBlockingGet() {
        log.info("Sending HTTP request");
        return Mono.fromCallable(() -> blockingGet());
    }

}
