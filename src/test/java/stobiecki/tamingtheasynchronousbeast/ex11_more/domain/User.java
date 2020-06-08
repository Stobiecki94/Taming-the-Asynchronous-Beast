package stobiecki.tamingtheasynchronousbeast.ex11_more.domain;

import lombok.Value;

@Value
public class User {

    public static final User SKYLER = new User("swhite", "Skyler", "White");
    public static final User JESSE = new User("jpinkman", "Jesse", "Pinkman");
    public static final User WALTER = new User("wwhite", "Walter", "White");
    public static final User SAUL = new User("sgoodman", "Saul", "Goodman");

    String username;
    String firstname;
    String lastname;
}
