package com.tarsem.BookMyStay.Strategy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OccupancyRateStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculateAdjustment(InventoryEntity inventory) {

        if (inventory.getTotalCount() == null ||
                inventory.getTotalCount() == 0) {

            return BigDecimal.ZERO;
        }

        BigDecimal booked = BigDecimal.valueOf(
                inventory.getBookCount()
        );

        BigDecimal total = BigDecimal.valueOf(
                inventory.getTotalCount()
        );

        BigDecimal occupancy = booked.divide(
                total,
                4,
                RoundingMode.HALF_UP
        );

        if (occupancy.compareTo(BigDecimal.valueOf(0.95)) >= 0) {
            return BigDecimal.valueOf(0.30);
        }

        if (occupancy.compareTo(BigDecimal.valueOf(0.85)) >= 0) {
            return BigDecimal.valueOf(0.20);
        }

        if (occupancy.compareTo(BigDecimal.valueOf(0.70)) >= 0) {
            return BigDecimal.valueOf(0.10);
        }

        if (occupancy.compareTo(BigDecimal.valueOf(0.50)) >= 0) {
            return BigDecimal.valueOf(0.05);
        }

        return BigDecimal.ZERO;
    }

    @Override
    public String name() {
        return "OccupancyRateStrategy";
    }
}