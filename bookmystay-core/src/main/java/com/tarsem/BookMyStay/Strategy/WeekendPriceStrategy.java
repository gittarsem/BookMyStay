package com.tarsem.BookMyStay.Strategy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;

import java.math.BigDecimal;
import java.time.DayOfWeek;

public class WeekendPriceStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculateAdjustment(InventoryEntity inventory) {

        DayOfWeek day = inventory.getDate().getDayOfWeek();

        if (day == DayOfWeek.SATURDAY ||
                day == DayOfWeek.SUNDAY) {

            return BigDecimal.valueOf(0.20);
        }

        return BigDecimal.ZERO;
    }

    @Override
    public String name() {
        return "WeekendPriceStrategy";
    }
}