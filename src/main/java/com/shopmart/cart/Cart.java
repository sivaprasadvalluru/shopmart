package com.shopmart.cart;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private String sessionId;
    private List<CartItem> items = new ArrayList<>();

    public Cart() {}

    public Cart(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotal() {
        return items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    }
}
