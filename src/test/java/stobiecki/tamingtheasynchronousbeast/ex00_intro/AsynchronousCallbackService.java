package stobiecki.tamingtheasynchronousbeast.ex00_intro;

import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.Customer;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.Order;
import stobiecki.tamingtheasynchronousbeast.ex00_intro.model.ShoppingCart;

import java.util.function.Consumer;

public interface AsynchronousCallbackService {

    void findCustomer(String customerId, Consumer<Customer> onSuccess, Consumer<Throwable> onError);

    void findOrders(Customer customer, Consumer<Order> onSuccess, Consumer<Throwable> onError);

    void findShoppingCard(Order order, Consumer<ShoppingCart> onSuccess, Consumer<Throwable> onError);


    void findAge(String userId, Consumer<Integer> onSuccess, Consumer<Throwable> onError);

    void findAddress(String userId, Consumer<String> onSuccess, Consumer<Throwable> onError);

    void findHobby(String userId, Consumer<String> onSuccess, Consumer<Throwable> onError);

}
