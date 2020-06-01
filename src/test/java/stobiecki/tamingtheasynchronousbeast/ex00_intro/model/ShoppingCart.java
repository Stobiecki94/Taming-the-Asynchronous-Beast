package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
public class ShoppingCart {

    List<Product> products;

    @Value
    static class Product {
        String name;
        BigDecimal price;
    }
}
