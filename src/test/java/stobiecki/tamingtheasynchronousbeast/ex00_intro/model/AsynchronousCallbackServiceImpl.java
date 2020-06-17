package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.AsynchronousCallbackService;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

@Slf4j
@RequiredArgsConstructor
public class AsynchronousCallbackServiceImpl implements AsynchronousCallbackService {

    private final ExecutorService executorService;

    @Override
    public void findCustomer(String customerId, Consumer<Customer> onSuccess, Consumer<Throwable> onError) {
        executorService.submit(() -> {
            requestToServerSimulation();
            Customer customer = new Customer(customerId, "Adam", "Warsaw, Woloska 24");
            log.debug("Customer found: {}", customer);
            onSuccess.accept(customer);
        });
    }

    @Override
    public void findOrders(Customer customer, Consumer<Order> onSuccess, Consumer<Throwable> onError) {
        executorService.submit(() -> {
            requestToServerSimulation();
            Stream.of(new Order(1L, "Warsaw, Woloska 24", Order.Status.COMPLETED),
                    new Order(2L, "Warsaw, Grzybowska 1", Order.Status.CANCELLED),
                    new Order(3L, "Warsaw, Woloska 24", Order.Status.PROCESSING))
                    .forEach(order -> {
                        log.debug("Found next order: {} for customer: {}", order, customer.getId());
                        onSuccess.accept(order);
                    });
        });
    }

    @Override
    public void findShoppingCard(Order order, Consumer<ShoppingCart> onSuccess, Consumer<Throwable> onError) {
        executorService.submit(() -> {
            requestToServerSimulation();
            ShoppingCart shoppingCart = new ShoppingCart(asList(
                    new ShoppingCart.Product("Ball", BigDecimal.TEN),
                    new ShoppingCart.Product("Pen", BigDecimal.ONE)
            ));
            log.debug("Found shopping Card: {} for order: {}", shoppingCart, order.getId());
            onSuccess.accept(shoppingCart);
        });
    }

    @Override
    public void findAge(String userId, Consumer<Integer> onSuccess, Consumer<Throwable> onError) {
        executorService.submit(() -> {
            requestToServerSimulation();
            onSuccess.accept(10);
        });
    }

    @Override
    public void findAddress(String userId, Consumer<String> onSuccess, Consumer<Throwable> onError) {
        executorService.submit(() -> {
            requestToServerSimulation();
            onSuccess.accept("Warsaw, Woloska 24");
        });
    }

    @Override
    public void findHobby(String userId, Consumer<String> onSuccess, Consumer<Throwable> onError) {
        executorService.submit(() -> {
            requestToServerSimulation();
            onSuccess.accept("Football");
        });
    }

    @SneakyThrows
    private void requestToServerSimulation() {
        long delay = (long) (Math.random() * 2000);
        Thread.sleep(delay);
    }
}
