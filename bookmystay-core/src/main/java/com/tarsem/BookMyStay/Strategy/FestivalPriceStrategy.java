package com.tarsem.BookMyStay.Strategy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class FestivalPriceStrategy implements PricingStrategy {

    private static final Set<LocalDate> HOLIDAYS = Set.of(

            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 3, 4),
            LocalDate.of(2026, 4, 3),
            LocalDate.of(2026, 4, 14),
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 3, 20),
            LocalDate.of(2026, 8, 15),
            LocalDate.of(2026, 9, 3),
            LocalDate.of(2026, 10, 2),
            LocalDate.of(2026, 10, 20),
            LocalDate.of(2026, 11, 8),
            LocalDate.of(2026, 11, 24),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 31)
    );

    @Override
    public BigDecimal calculateAdjustment(InventoryEntity inventory) {

        if (HOLIDAYS.contains(inventory.getDate())) {
            return BigDecimal.valueOf(0.50);
        }

        return BigDecimal.ZERO;
    }

    @Override
    public String name() {
        return "FestivalPriceStrategy";
    }
}