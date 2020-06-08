package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class Constants {

    static final Map<String, String> HOBBIES = new ImmutableMap.Builder<String, String>()
            .put("Adam", "Football")
            .put("Kuba", "Volleyball")
            .put("Grzegorz", "Chess")
            .build();
    static final Map<String, Integer> AGES = new ImmutableMap.Builder<String, Integer>()
            .put("Adam", 13)
            .put("Kuba", 14)
            .put("Grzegorz", 15)
            .build();

    static final List<String> NAMES = asList("Adam", "Kuba", "Grzegorz");
}
