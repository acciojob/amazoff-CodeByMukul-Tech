package com.driver;

public class Order {

    private String id;
    private int deliveryTime; // stored in minutes

    public Order(String id, String deliveryTime) {
        this.id = id;
        // accepts "HH:MM" or minutes as string
        if (deliveryTime == null) {
            this.deliveryTime = 0;
        } else if (deliveryTime.contains(":")) {
            String[] parts = deliveryTime.split(":");
            int HH = Integer.parseInt(parts[0]);
            int MM = Integer.parseInt(parts[1]);
            this.deliveryTime = HH * 60 + MM;
        } else {
            this.deliveryTime = Integer.parseInt(deliveryTime);
        }
    }

    public String getId() {
        return id;
    }

    public int getDeliveryTime() {
        return deliveryTime;
    }
}
