package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private HashMap<String, Order> orderMap;
    private HashMap<String, DeliveryPartner> partnerMap;
    private HashMap<String, HashSet<String>> partnerToOrderMap;
    private HashMap<String, String> orderToPartnerMap;

    public OrderRepository(){
        this.orderMap = new HashMap<String, Order>();
        this.partnerMap = new HashMap<String, DeliveryPartner>();
        this.partnerToOrderMap = new HashMap<String, HashSet<String>>();
        this.orderToPartnerMap = new HashMap<String, String>();
    }

    public void saveOrder(Order order){
        orderMap.put(order.getId(),order);
    }

    public void savePartner(String partnerId){
        DeliveryPartner deliveryPartner = new DeliveryPartner(partnerId);
        partnerMap.put(partnerId,deliveryPartner);
    }

    public void saveOrderPartnerMap(String orderId, String partnerId) {

        // Validation
        if (!orderMap.containsKey(orderId) || !partnerMap.containsKey(partnerId)) {
            return;
        }

        // Save mapping: order → partner
        orderToPartnerMap.put(orderId, partnerId);

        // Save mapping: partner → list of orders
        partnerToOrderMap.putIfAbsent(partnerId, new HashSet<>());
        partnerToOrderMap.get(partnerId).add(orderId);

        // Update number of orders for the partner
        DeliveryPartner partner = partnerMap.get(partnerId);
        partner.setNumberOfOrders(partnerToOrderMap.get(partnerId).size());
    }


    public Order findOrderById(String orderId) {
        return orderMap.getOrDefault(orderId, null);
    }


    public DeliveryPartner findPartnerById(String partnerId) {
        return partnerMap.getOrDefault(partnerId, null);
    }


    public Integer findOrderCountByPartnerId(String partnerId) {

        if (!partnerToOrderMap.containsKey(partnerId)) {
            return 0;   // no orders assigned
        }

        return partnerToOrderMap.get(partnerId).size();
    }


    public List<String> findOrdersByPartnerId(String partnerId) {

        if (!partnerToOrderMap.containsKey(partnerId)) {
            return new ArrayList<>();   // no orders assigned
        }

        return new ArrayList<>(partnerToOrderMap.get(partnerId));
    }


    public List<String> findAllOrders() {

        return new ArrayList<>(orderMap.keySet());
    }


    public void deletePartner(String partnerId) {

        // If partner does NOT exist, nothing to delete
        if (!partnerMap.containsKey(partnerId)) {
            return;
        }

        // Step 1: Unassign all orders linked to this partner
        if (partnerToOrderMap.containsKey(partnerId)) {
            for (String orderId : partnerToOrderMap.get(partnerId)) {
                orderToPartnerMap.remove(orderId);  // unassign order
            }
            partnerToOrderMap.remove(partnerId); // remove partner's order list
        }

        // Step 2: Finally remove partner from partnerMap
        partnerMap.remove(partnerId);
    }


    public void deleteOrder(String orderId) {

        // Step 1: If this order is assigned to any partner, unassign it
        if (orderToPartnerMap.containsKey(orderId)) {

            String partnerId = orderToPartnerMap.get(orderId);

            // Remove the order from this partner's list
            if (partnerToOrderMap.containsKey(partnerId)) {
                partnerToOrderMap.get(partnerId).remove(orderId);

                // Update number of orders for this partner
                DeliveryPartner partner = partnerMap.get(partnerId);
                partner.setNumberOfOrders(partnerToOrderMap.get(partnerId).size());
            }

            // Remove mapping order → partner
            orderToPartnerMap.remove(orderId);
        }

        // Step 2: Remove order from orderMap
        orderMap.remove(orderId);
    }


    public Integer findCountOfUnassignedOrders() {
        return orderMap.size() - orderToPartnerMap.size();
    }


    public Integer findOrdersLeftAfterGivenTimeByPartnerId(String timeString, String partnerId) {

        if (!partnerToOrderMap.containsKey(partnerId)) {
            return 0;  // no orders assigned
        }

        // Convert HH:MM to minutes
        String[] parts = timeString.split(":");
        int givenTime = Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);

        int count = 0;

        for (String orderId : partnerToOrderMap.get(partnerId)) {
            Order order = orderMap.get(orderId);
            if (order != null && order.getDeliveryTime() > givenTime) {
                count++;
            }
        }

        return count;
    }


    public String findLastDeliveryTimeByPartnerId(String partnerId) {

        if (!partnerToOrderMap.containsKey(partnerId) || partnerToOrderMap.get(partnerId).isEmpty()) {
            return "00:00";  // no orders assigned
        }

        int lastTime = 0;

        for (String orderId : partnerToOrderMap.get(partnerId)) {
            Order order = orderMap.get(orderId);
            if (order != null && order.getDeliveryTime() > lastTime) {
                lastTime = order.getDeliveryTime();
            }
        }

        // Convert minutes back to HH:MM
        int hh = lastTime / 60;
        int mm = lastTime % 60;

        // Format as 2-digit string
        return String.format("%02d:%02d", hh, mm);
    }

}