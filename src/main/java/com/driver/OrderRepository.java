package com.driver;

import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private final HashMap<String, Order> orderMap = new HashMap<>();
    private final HashMap<String, DeliveryPartner> partnerMap = new HashMap<>();
    private final HashMap<String, HashSet<String>> partnerToOrderMap = new HashMap<>();
    private final HashMap<String, String> orderToPartnerMap = new HashMap<>();

    public void addOrder(Order order){
        orderMap.put(order.getId(), order);
    }

    public void addPartner(String partnerId){
        partnerMap.put(partnerId, new DeliveryPartner(partnerId));
    }

    public void addOrderPartnerPair(String orderId, String partnerId){
        if(orderMap.containsKey(orderId) && partnerMap.containsKey(partnerId)) {

            // If previously assigned → remove old mapping
            if(orderToPartnerMap.containsKey(orderId)){
                String prevPartner = orderToPartnerMap.get(orderId);
                partnerToOrderMap.get(prevPartner).remove(orderId);
                partnerMap.get(prevPartner).decrementOrders();
            }

            orderToPartnerMap.put(orderId, partnerId);
            partnerToOrderMap.putIfAbsent(partnerId, new HashSet<>());

            boolean added = partnerToOrderMap.get(partnerId).add(orderId);
            if(added){
                partnerMap.get(partnerId).incrementOrders();
            }
        }
    }

    public Order getOrderById(String orderId){
        return orderMap.get(orderId);
    }

    public DeliveryPartner getPartnerById(String partnerId){
        return partnerMap.get(partnerId);
    }

    public Integer getOrderCountByPartnerId(String partnerId){
        return partnerToOrderMap.getOrDefault(partnerId, new HashSet<>()).size();
    }

    public List<String> getOrdersByPartnerId(String partnerId){
        return new ArrayList<>(partnerToOrderMap.getOrDefault(partnerId, new HashSet<>()));
    }

    public List<String> getAllOrders(){
        return new ArrayList<>(orderMap.keySet());
    }

    public void deletePartner(String partnerId){
        if(!partnerMap.containsKey(partnerId)) return;

        if(partnerToOrderMap.containsKey(partnerId)){
            for(String orderId : partnerToOrderMap.get(partnerId)){
                orderToPartnerMap.remove(orderId);
            }
            partnerToOrderMap.remove(partnerId);
        }

        partnerMap.remove(partnerId);
    }

    public void deleteOrder(String orderId){
        if(!orderMap.containsKey(orderId)) return;

        if(orderToPartnerMap.containsKey(orderId)){
            String partnerId = orderToPartnerMap.get(orderId);
            partnerToOrderMap.get(partnerId).remove(orderId);
            partnerMap.get(partnerId).decrementOrders();

            if(partnerToOrderMap.get(partnerId).isEmpty()){
                partnerToOrderMap.remove(partnerId);
            }

            orderToPartnerMap.remove(orderId);
        }

        orderMap.remove(orderId);
    }

    public Integer getCountOfUnassignedOrders(){
        return orderMap.size() - orderToPartnerMap.size();
    }

    public Integer getOrdersLeftAfterGivenTimeByPartnerId(String time, String partnerId){
        if(!partnerToOrderMap.containsKey(partnerId)) return 0;

        String[] parts = time.split(":");
        int HH = Integer.parseInt(parts[0]);
        int MM = Integer.parseInt(parts[1]);
        int givenTime = HH * 60 + MM;

        int count = 0;

        for(String orderId : partnerToOrderMap.get(partnerId)){
            if(orderMap.get(orderId).getDeliveryTime() > givenTime){
                count++;
            }
        }

        return count;
    }

    public String getLastDeliveryTimeByPartnerId(String partnerId){
        if(!partnerToOrderMap.containsKey(partnerId)) return "";

        int latest = 0;

        for(String orderId : partnerToOrderMap.get(partnerId)){
            latest = Math.max(latest, orderMap.get(orderId).getDeliveryTime());
        }

        int HH = latest / 60;
        int MM = latest % 60;
        return String.format("%02d:%02d", HH, MM);
    }
}
