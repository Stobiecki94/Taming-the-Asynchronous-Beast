package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.Customer;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.Order;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.ShoppingCart;

public interface AsynchronousReactiveService {

    // Ex01_CallbackHell
    Mono<Customer> findCustomer(String customerId);

    Flux<Order> findOrders(Customer customer);

    Mono<ShoppingCart> findShoppingCard(Order order);

    // Ex02_CallbackComposition
    Mono<Integer> findAge(String userId);

    Mono<String> findAddress(String userId);

    Mono<String> findHobby(String userId);

}
