package com.driver;

public class Order {

    private String id;
    private int deliveryTime; // in minutes

    public Order(String id, String deliveryTime) {
        this.id = id;
        this.deliveryTime = convertTimeToMinutes(deliveryTime);
    }

    private int convertTimeToMinutes(String time) {
        String[] parts = time.split(":"); // split HH and MM
        int hh = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);
        return hh * 60 + mm;
    }

    public String getId() {
        return id;
    }

    public int getDeliveryTime() {
        return deliveryTime;
    }
}
