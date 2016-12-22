package io.vieira.xtremebanking;

import io.vieira.xtremebanking.http.TradeServer;

import java.time.format.DateTimeFormatter;

public class Example {

    public static void main(String[] args) {
        new TradeServer()
                .onPort(8080)
                .withDayDuration(2)
                .handleRequestsForTheDay((localDateTime, byteBufs) -> System.out.println(String.format(
                        "%s has seen %d requests",
                        localDateTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                        byteBufs.size()
                )))
                .start();
    }
}
