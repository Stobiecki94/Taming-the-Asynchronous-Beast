package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.AsynchronousCallbackServiceImpl;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.AsynchronousReactiveServiceImpl;

import java.util.concurrent.Executors;

@Slf4j
public class Ex01_CallbackHell {

    @Test
    @SneakyThrows
    public void shouldFindCustomerAndItsOrdersAndTheirShoppingCarts_CallbackHell() {
        AsynchronousCallbackService service = new AsynchronousCallbackServiceImpl(Executors.newFixedThreadPool(2));

        service.findCustomer("customerId", customer -> {
            service.findOrders(customer, order -> {
                service.findShoppingCard(order, shoppingCart -> {
                    log.info("shoppingCart: {}", shoppingCart);
                }, ex -> log.error("Error handling here - findShoppingCard", ex));
            }, ex -> log.error("Error handling here - findOrders", ex));
        }, ex -> log.error("Error handling here - findCustomer", ex));

        Thread.sleep(10000);
    }

    @Test
    @SneakyThrows
    public void shouldFindCustomerAndItsOrdersAndTheirShoppingCarts_ReactorApproach() {
        AsynchronousReactiveService service = new AsynchronousReactiveServiceImpl();

        service.findCustomer("customerId")
                .flatMapMany(service::findOrders)
                .flatMap(service::findShoppingCard)
                .doOnNext(shoppingCart -> log.info("shoppingCart: {}", shoppingCart))
                .doOnError(error -> log.error("error", error))
                .blockLast();
    }

}
