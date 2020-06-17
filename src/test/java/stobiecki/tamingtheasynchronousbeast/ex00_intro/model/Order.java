package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import lombok.Value;

@Value
public class Order {

    Long id;
    String address;
    Status status;

    public enum Status {
        PROCESSING,
        CANCELLED,
        COMPLETED
    }

}
