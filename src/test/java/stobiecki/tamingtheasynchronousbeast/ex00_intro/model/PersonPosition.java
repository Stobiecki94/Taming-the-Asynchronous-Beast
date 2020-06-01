package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import lombok.Value;

@Value
public class PersonPosition {
    private Customer customer;
    private ShoppingCart shoppingCart;
}
