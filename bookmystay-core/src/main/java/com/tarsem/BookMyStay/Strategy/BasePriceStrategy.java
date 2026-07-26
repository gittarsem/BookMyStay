package com.tarsem.BookMyStay.Strategy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;

import java.math.BigDecimal;

public class BasePriceStrategy implements PricingStrategy{
    @Override
    public BigDecimal calculatePrice(BigDecimal price, InventoryEntity inventory) {
        return price.add(inventory.getPrice());
    }

    @Override
    public String name() {
        return "Base Price";
    }
}
