package com.ioteducloud.app;

public class Reading {
    private String timestamp;
    private double temperature;
    private double humidity;
    private double pressure;

    public Reading() {
    }

    public Reading(String timestamp, double temperature, double humidity, double pressure) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getPressure() {
        return pressure;
    }
}
