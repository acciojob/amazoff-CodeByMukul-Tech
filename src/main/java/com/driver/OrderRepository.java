package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private final HashMap<String, Order> orderMap;
    private final HashMap<String, DeliveryPartner> partnerMap;
    private final HashMap<String, HashSet<String>> partnerToOrderMap;
    private final HashMap<String, String> orderToPartnerMap;

    public OrderRepository(){
        this.orderMap = new HashMap<>();
        this.partnerMap = new HashMap<>();
        this.partnerToOrderMap = new HashMap<>();
        this.orderToPartnerMap = new HashMap<>();
    }

    public void saveOrder(Order order){
        orderMap.put(order.getId(), order);
    }

    public void savePartner(String partnerId){
        DeliveryPartner deliveryPartner = new DeliveryPartner(partnerId);
        partnerMap.put(partnerId, deliveryPartner);
    }

    public void saveOrderPartnerMap(String orderId, String partnerId){
        if(orderMap.containsKey(orderId) && partnerMap.containsKey(partnerId)){
            // if order already assigned to another partner, remove that mapping first
            if(orderToPartnerMap.containsKey(orderId)){
                String prevPartner = orderToPartnerMap.get(orderId);
                if(partnerToOrderMap.containsKey(prevPartner)){
                    partnerToOrderMap.get(prevPartner).remove(orderId);
                    DeliveryPartner dp = partnerMap.get(prevPartner);
                    if(dp != null) dp.decrementOrders();
                }
            }

            orderToPartnerMap.put(orderId, partnerId);
            partnerToOrderMap.putIfAbsent(partnerId, new HashSet<>());
            boolean added = partnerToOrderMap.get(partnerId).add(orderId);
            if(added){
                DeliveryPartner dp = partnerMap.get(partnerId);
                if(dp != null) dp.incrementOrders();
            }
        }
    }

    public Order findOrderById(String orderId){
        return orderMap.getOrDefault(orderId, null);
    }

    public DeliveryPartner findPartnerById(String partnerId){
        return partnerMap.getOrDefault(partnerId, null);
    }

    public Integer findOrderCountByPartnerId(String partnerId){
        if(!partnerToOrderMap.containsKey(partnerId)) return 0;
        return partnerToOrderMap.get(partnerId).size();
    }

    public List<String> findOrdersByPartnerId(String partnerId){
        if(!partnerToOrderMap.containsKey(partnerId)) return new ArrayList<>();
        return new ArrayList<>(partnerToOrderMap.get(partnerId));
    }

    public List<String> findAllOrders(){
        return new ArrayList<>(orderMap.keySet());
    }

    public void deletePartner(String partnerId){
        if(!partnerMap.containsKey(partnerId)) return;

        // remove partner's orders assignments (make them unassigned)
        if(partnerToOrderMap.containsKey(partnerId)){
            for(String orderId : partnerToOrderMap.get(partnerId)){
                orderToPartnerMap.remove(orderId);
            }
            partnerToOrderMap.remove(partnerId);
        }

        // finally remove the partner
        partnerMap.remove(partnerId);
    }

    public void deleteOrder(String orderId){
        if(!orderMap.containsKey(orderId)) return;

        if(orderToPartnerMap.containsKey(orderId)){
            String partnerId = orderToPartnerMap.get(orderId);
            orderToPartnerMap.remove(orderId);
            if(partnerToOrderMap.containsKey(partnerId)){
                boolean removed = partnerToOrderMap.get(partnerId).remove(orderId);
                if(removed){
                    DeliveryPartner dp = partnerMap.get(partnerId);
                    if(dp != null) dp.decrementOrders();
                }
                // if partner now has no orders, you may keep empty set or remove the entry
                if(partnerToOrderMap.get(partnerId).isEmpty()){
                    partnerToOrderMap.remove(partnerId);
                }
            }
        }
        orderMap.remove(orderId);
    }

    public Integer findCountOfUnassignedOrders(){
        return orderMap.size() - orderToPartnerMap.size();
    }

    // Accepts "HH:MM" or minutes string. Returns count of partner's orders with deliveryTime > given time
    public Integer findOrdersLeftAfterGivenTimeByPartnerId(String timeString, String partnerId){
        if(!partnerToOrderMap.containsKey(partnerId)) return 0;
        int timelimit;
        if(timeString.contains(":")){
            String[] parts = timeString.split(":");
            int HH = Integer.parseInt(parts[0]);
            int MM = Integer.parseInt(parts[1]);
            timelimit = HH * 60 + MM;
        } else {
            timelimit = Integer.parseInt(timeString);
        }

        int count = 0;
        for(String orderId : partnerToOrderMap.get(partnerId)){
            Order order = orderMap.get(orderId);
            if(order != null && order.getDeliveryTime() > timelimit) count++;
        }
        return count;
    }

    // Returns last delivery time as "HH:MM" (00 padded)
    public String findLastDeliveryTimeByPartnerId(String partnerId){
        if(!partnerToOrderMap.containsKey(partnerId) || partnerToOrderMap.get(partnerId).isEmpty()) return "";
        int lasttime = 0;
        for(String orderId : partnerToOrderMap.get(partnerId)){
            Order order = orderMap.get(orderId);
            if(order != null) lasttime = Math.max(lasttime, order.getDeliveryTime());
        }
        int HH = lasttime / 60;
        int MM = lasttime % 60;
        return String.format("%02d:%02d", HH, MM);
    }
}
