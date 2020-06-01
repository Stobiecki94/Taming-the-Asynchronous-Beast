package stobiecki.tamingtheasynchronousbeast.ex01_subscription;

import reactor.core.publisher.Flux;

interface TemperatureService {

    Flux<Double> getTemperature(String cityName);
}
