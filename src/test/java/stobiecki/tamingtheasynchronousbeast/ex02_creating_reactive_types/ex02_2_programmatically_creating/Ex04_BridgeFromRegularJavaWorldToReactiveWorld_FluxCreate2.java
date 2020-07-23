package stobiecki.tamingtheasynchronousbeast.ex02_creating_reactive_types.ex02_2_programmatically_creating;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class Ex04_BridgeFromRegularJavaWorldToReactiveWorld_FluxCreate2 {

    /**
     * TIP: create can be very useful to bridge an existing API with the reactive world - such as an asynchronous API based on listeners.
     */
    private static final boolean docHolder = false;

    interface HttpResponseListener {
        void onHttpClientResponse(HttpResponse httpResponse);
    }

    @RequiredArgsConstructor
    static class HttpClientWithListeners {

        private final HttpClient delegate;
        private final Collection<HttpResponseListener> listeners = new CopyOnWriteArrayList<>();

        @SneakyThrows
        HttpResponse execute(HttpUriRequest request) {
            HttpResponse response = delegate.execute(request);
            newResponse(response);
            return response;
        }

        void registerResponseListener(HttpResponseListener myEventListener) {
            this.listeners.add(myEventListener);
        }

        void removeResponseListener(HttpResponseListener myEventListener) {
            listeners.remove(myEventListener);
        }

        private void newResponse(HttpResponse response) {
            listeners.forEach(listener -> listener.onHttpClientResponse(response));
        }
    }

    @Test
    @SneakyThrows
    public void saveResponseContent() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        HttpClientWithListeners httpClient = new HttpClientWithListeners(HttpClientBuilder.create().build());

        Flux<String> bridge = Flux.create(sink -> {
            HttpResponseListener httpResponseListener = httpResponse -> {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode >= 300) {
                    sink.error(new RuntimeException(String.format("Failed, get response with status: %d", statusCode)));
                } else {
                    Try.ofCallable(() -> EntityUtils.toString(httpResponse.getEntity()))
                            .onSuccess(content -> {
                                if (content.isEmpty()) {
                                } else {
                                    sink.next(content);
                                }
                            })
                            .onFailure(sink::error);
                }
            };

            //register listener in http client
            httpClient.registerResponseListener(httpResponseListener);

            //remove listener on dispose
            sink.onDispose(() -> httpClient.removeResponseListener(httpResponseListener)); // <-- clean up
        });

        bridge
                .publishOn(Schedulers.newSingle("save-content-thread"))
                .take(2)
                .subscribe(
                        pageContent -> saveToTemporaryFile(pageContent, "saved"),
                        error -> log.error("Error", error),
                        () -> {
                            log.info("Done");
                            countDownLatch.countDown();
                        });

        new Thread(() -> {
            HttpGet request = new HttpGet("https://gfieast.com/pl/aktualnosci/");
            log.info("executed GET request");
            httpClient.execute(request);
        }, "http-client-thread")
                .start();

        new Thread(() -> {
            HttpGet request = new HttpGet("https://gfieast.com/pl/");
            log.info("executed GET request");
            httpClient.execute(request);
        }, "http-client-thread2")
                .start();

        boolean await10Seconds = countDownLatch.await(10, TimeUnit.SECONDS);
        assertThat(await10Seconds).withFailMessage("Await timed out!").isTrue();
    }

    @SneakyThrows
    private void saveToTemporaryFile(String pageContent, String fileName) {
        File tmpFile = File.createTempFile(fileName, ".html");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
            writer.write(pageContent);
            log.info("Saved: {}", tmpFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Something went wrong...", e);
        }
    }
}
