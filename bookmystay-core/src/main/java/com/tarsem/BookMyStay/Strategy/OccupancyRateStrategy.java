package com.tarsem.BookMyStay.Strategy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;

import java.math.BigDecimal;

public class OccupancyRateStrategy implements PricingStrategy{
    @Override
    public BigDecimal calculatePrice(BigDecimal price, InventoryEntity inventory) {

        double ratio=(double)inventory.getBookCount()/ inventory.getTotalCount();
        BigDecimal factor=inventory.getSurgeFactor()==null?BigDecimal.ONE:inventory.getSurgeFactor();
        return ratio==0.8? price.multiply(factor):price;

    }

    @Override
    public String name() {
        return "OccupancyRateStrategy";
    }
}
