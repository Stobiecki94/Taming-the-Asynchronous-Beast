package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.ReactiveService;

import java.math.BigDecimal;
import java.time.Duration;

import static java.util.Arrays.asList;
import static stobiecki.tamingtheasynchronousbeast.ex00_intro.model.Constants.*;

@Slf4j
public class ReactiveServiceImpl implements ReactiveService {

    @Override
    public Mono<Customer> findCustomer(String customerId) {
        return Mono.just(new Customer(customerId, "Adam", "Warsaw, Woloska 24"))
                .delayElement(Duration.ofMillis(ioOperationDelay()))
                .doOnSuccess(customer -> log.debug("Customer found: {}", customer));
    }

    @Override
    public Flux<Order> findOrders(Customer customer) {
        return Mono.just(true)
                .delayElement(Duration.ofMillis(ioOperationDelay()))
                .thenMany(Flux.just(
                        new Order(1L, "Warsaw, Woloska 24", Order.Status.COMPLETED),
                        new Order(2L, "Warsaw, Grzybowska 1", Order.Status.CANCELLED),
                        new Order(3L, "Warsaw, Woloska 24", Order.Status.PROCESSING))
                        .doOnNext(order -> log.debug("Found next order: {} for customer: {}", order, customer.getId())));

    }

    @Override
    public Mono<ShoppingCart> findShoppingCard(Order order) {
        return Mono.just(new ShoppingCart(asList(
                new ShoppingCart.Product("Ball", BigDecimal.TEN),
                new ShoppingCart.Product("Pen", BigDecimal.ONE))))
                .delayElement(Duration.ofMillis(ioOperationDelay()))
                .doOnNext(shoppingCard -> log.debug("Found shopping Card: {} for order: {}", shoppingCard, order.getId()));
    }

    @Override
    public Mono<Integer> findAge(String userId) {
        return Mono.just(10)
                .delayElement(Duration.ofMillis(ioOperationDelay()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> findAddress(String userId) {
        return Mono.just("Warsaw, Woloska 24")
                .delayElement(Duration.ofMillis(ioOperationDelay()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> findHobby(String userId) {
        return Mono.just("Football")
                .delayElement(Duration.ofMillis(ioOperationDelay()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<String> getUsersNames() {
        return Flux.fromIterable(NAMES)
                .delayElements(Duration.ofMillis(ioOperationDelay()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Integer> getAge(String id) {
        return Mono.fromCallable(() -> AGES.get(id))
                .delayElement(Duration.ofMillis(ioOperationDelay()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> getHobby(String id) {
        return Mono.fromCallable(() -> HOBBIES.get(id))
                .delayElement(Duration.ofMillis(ioOperationDelay()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private long ioOperationDelay() {
        return (long) (Math.random() * 2000);
    }
}
