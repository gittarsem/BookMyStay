package com.tarsem.BookMyStay.Strategy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class WeekendPriceStrategy implements PricingStrategy{
    @Override
    public BigDecimal calculatePrice(BigDecimal price, InventoryEntity inventory) {
        DayOfWeek day=inventory.getDate().getDayOfWeek();

        if(day==DayOfWeek.SATURDAY || day==DayOfWeek.SUNDAY){
            return price.multiply(BigDecimal.valueOf(1.20));
        }
        return price;
    }

    @Override
    public String name() {
        return "WeekendPriceStrategy";
    }
}
