package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import lombok.Data;

@Data
public class Holder<T> {
    private T value;
}
