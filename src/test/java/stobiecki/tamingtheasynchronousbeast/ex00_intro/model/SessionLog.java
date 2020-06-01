package stobiecki.tamingtheasynchronousbeast.ex00_intro.model;

import lombok.Value;

import java.time.ZonedDateTime;

@Value
public class SessionLog {

    Long id;
    ZonedDateTime startTime;
    ZonedDateTime endTime;

}
