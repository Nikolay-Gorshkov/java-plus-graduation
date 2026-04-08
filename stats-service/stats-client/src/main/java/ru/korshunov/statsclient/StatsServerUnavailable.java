package ru.korshunov.statsclient;

public class StatsServerUnavailable extends RuntimeException {

    public StatsServerUnavailable(String message) {
        super(message);
    }
}
