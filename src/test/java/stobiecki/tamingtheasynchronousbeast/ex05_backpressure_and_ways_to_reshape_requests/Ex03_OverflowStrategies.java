package stobiecki.tamingtheasynchronousbeast.ex05_backpressure_and_ways_to_reshape_requests;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class Ex03_OverflowStrategies {

    //create and push
    // https://itsallbinary.com/reactor-basics-with-example-backpressure-overflow-drop-error-latest-ignore-buffer-good-for-beginners/

    /**
     * DROP to drop the incoming signal if the downstream is not ready to receive it.
     *
     * DROP strategy drops the most recent next value if the downstream can’t keep up because its too slow.
     * There are also ways provided to consume dropped values and handle them separately.
     */
    @Test
    @SneakyThrows
    public void overflowStrategy_DROP() {
        Flux<Object> fluxAsyncBackp = Flux.create(emitter -> {

            // Publish 1000 numbers
            for (int i = 0; i < 1000; i++) {
                System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
                emitter.next(i);
            }
            // When all values or emitted, call complete.
            emitter.complete();
        }, FluxSink.OverflowStrategy.DROP)
                .onBackpressureDrop(i -> System.out.println(Thread.currentThread().getName() + " | DROPPED = " + i));

        fluxAsyncBackp.subscribeOn(Schedulers.newElastic("publisherThread")).publishOn(Schedulers.newElastic("subscriberThread")).subscribe(i -> {
            // Process received value.
            System.out.println(Thread.currentThread().getName() + " | Received = " + i);
            // 500 mills delay to simulate slow subscriber
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        /*
         * Notice above -
         *
         * OverflowStrategy.DROP - If subscriber can't keep up with values, then drop the values.
         *
         * subscribeOn & publishOn - Put subscriber & publishers on different threads.
         */

        // Since publisher & subscriber run on different thread than main thread, keep
        // main thread active for 100 seconds.
        Thread.sleep(100000);

        // In below output you can see that till 256 (default buffer size), values were successfully published & then values starts dropping. Subscriber also received 256 values successfully
    }

    /**
     * LATEST to let downstream only get the latest signals from upstream.
     *
     * LATEST strategy keeps only the latest next value, overwriting any previous value if the downstream can’t keep up because its too slow.
     */
    @Test
    @SneakyThrows
    public void overflowStrategy_LATEST() {
        Flux<Object> fluxAsyncBackp = Flux.create(emitter -> {

            // Publish 1000 numbers
            for (int i = 0; i < 1000; i++) {
                System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
                emitter.next(i);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            // When all values or emitted, call complete.
            emitter.complete();

        }, FluxSink.OverflowStrategy.LATEST);

        fluxAsyncBackp.subscribeOn(Schedulers.newElastic("publisherThread")).publishOn(Schedulers.newElastic("subscriberThread")).subscribe(i -> {
            // Process received value.
            System.out.println(Thread.currentThread().getName() + " | Received = " + i);
            // 100 mills delay to simulate slow subscriber
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }, e -> {
            // Process error
            System.err.println(Thread.currentThread().getName() + " | Error = " + e.getMessage());
        });
        /*
         * Notice above -
         *
         * OverflowStrategy.LATEST - Overwrites values if subscriber can't keep up.
         *
         * subscribeOn & publishOn - Put subscriber & publishers on different threads.
         */

        // Since publisher & subscriber run on different thread than main thread, keep
        // main thread active for 100 seconds.
        Thread.sleep(100000);

        // In below output you can see that publishing of all 999 values seems to have gone fine.
        // Subscriber also started asynchronously receiving values. But you can see subscriber directly received 999 after 255.
        // This means that after 255 (default buffer of 256), all values were replaced with latest & finally last value of 999 received by subscriber.
    }


    /**
     *  ERROR to signal an IllegalStateException when the downstream can’t keep up.
     *
     *  ERROR strategy throws OverflowException in case the downstream can’t keep up due to slowness.
     *  Publisher can handle exception & make sure to call error handle so that subscriber can do handling on subscriber side for such error scenarios.
     */
    @Test
    @SneakyThrows
    public void overflowStrategy_ERROR() {
        Flux<Object> fluxAsyncBackp = Flux.create(emitter -> {

            // Publish 1000 numbers
            for (int i = 0; i < 1000; i++) {
                System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
                // BackpressureStrategy.ERROR will cause MissingBackpressureException when
                // subscriber can't keep up. So handle exception & call error handler.
                emitter.next(i);
            }
            // When all values or emitted, call complete.
            emitter.complete();

        }, FluxSink.OverflowStrategy.ERROR);

        fluxAsyncBackp.subscribeOn(Schedulers.elastic()).publishOn(Schedulers.elastic()).subscribe(i -> {
            // Process received value.
            System.out.println(Thread.currentThread().getName() + " | Received = " + i);
        }, e -> {
            // Process error
            System.err.println(Thread.currentThread().getName() + " | Error = " + e.getClass().getSimpleName() + " "
                    + e.getMessage());
        });
        /*
         * Notice above -
         *
         * OverflowStrategy.ERROR - Throws MissingBackpressureException is subscriber
         * can't keep up.
         *
         * subscribeOn & publishOn - Put subscriber & publishers on different threads.
         */

        // Since publisher & subscriber run on different thread than main thread, keep
        // main thread active for 100 seconds.
        Thread.sleep(100000);

        //You can see in below output that publishing & subscribing started on different threads.
        // Subscriber received values till 255 & then error handler was called due to OverflowException. After that subscriber stopped.
    }

    /**
     *  BUFFER (the default) to buffer all signals if the downstream can’t keep up. (this does unbounded buffering and may lead to OutOfMemoryError).
     *
     *  With BUFFER strategy, as name suggests all values are buffered so that subscriber can receive all values.
     *  As per program below, buffer is infinite, so if published values are large in count & subscriber is too slow, then there is chance of out of memory just like Observable.
     */
    @Test
    @SneakyThrows
    public void overflowStrategy_BUFFER() {
        Flux<Object> fluxAsyncBackp = Flux.create(emitter -> {

            // Publish 1000 numbers
            for (int i = 0; i < 999_000_000; i++) {
                System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
                emitter.next(i);
                // Thread.sleep(10);
            }
            // When all values or emitted, call complete.
            emitter.complete();
        }, FluxSink.OverflowStrategy.BUFFER);

        fluxAsyncBackp.subscribeOn(Schedulers.newElastic("publisherThread")).publishOn(Schedulers.newElastic("subscriberThread")).subscribe(i -> {
            // Process received value.
            System.out.println(Thread.currentThread().getName() + " | Received = " + i);
            // 500 mills delay to simulate slow subscriber
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }, e -> {
            // Process error
            System.err.println(Thread.currentThread().getName() + " | Error = " + e.getClass().getSimpleName() + " "
                    + e.getMessage());
        });
        /*
         * Notice above -
         *
         * subscribeOn & publishOn - Put subscriber & publishers on different threads.
         */

        // Since publisher & subscriber run on different thread than main thread, keep
        // main thread active for 100 seconds.
        Thread.sleep(1000000);
    }



    /**
     *  IGNORE to Completely ignore downstream backpressure requests. This may yield IllegalStateException when queues get full downstream.
     *  BUFFER (the default) to buffer all signals if the downstream can’t keep up. (this does unbounded buffering and may lead to OutOfMemoryError).
     */
    @Test
    @SneakyThrows
    public void overflowStrategy_IGNORE() {
        Flux<Object> fluxAsyncBackp = Flux.create(emitter -> {

            // Publish 1000 numbers
            for (int i = 0; i < 1000; i++) {
                System.out.println(Thread.currentThread().getName() + " | Publishing = " + i);
                // BackpressureStrategy.MISSING will cause MissingBackpressureException
                // eventually
                emitter.next(i);
            }
            // When all values or emitted, call complete.
            emitter.complete();

        }, FluxSink.OverflowStrategy.IGNORE);

        fluxAsyncBackp.subscribeOn(Schedulers.newElastic("publisherThread")).publishOn(Schedulers.newElastic("subscriberThread")).subscribe(i -> {
            // Process received value.
            System.out.println(Thread.currentThread().getName() + " | Received = " + i);
        }, e -> {
            // Process error
            System.err.println(Thread.currentThread().getName() + " | Error = " + e.getClass().getSimpleName() + " "
                    + e.getMessage());
        });
        /*
         * Notice above -
         *
         * subscribeOn & publishOn - Put subscriber & publishers on different threads.
         */

        // Since publisher & subscriber run on different thread than main thread, keep
        // main thread active for 100 seconds.
        Thread.sleep(100000);
    }

}
