package ru.practicum.ewm.stats.avro;

public enum ActionTypeAvro {
    VIEW(0.4),
    REGISTER(0.8),
    LIKE(1.0);

    private final double weight;

    ActionTypeAvro(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
