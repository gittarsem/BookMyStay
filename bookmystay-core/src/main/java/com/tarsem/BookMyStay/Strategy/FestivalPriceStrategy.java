package com.tarsem.BookMyStay.Strategy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class FestivalPriceStrategy implements PricingStrategy{

    private static final Set<LocalDate> HOLIDAYS = Set.of(

            // New Year
            LocalDate.of(2026, 1, 1),

            // Republic Day
            LocalDate.of(2026, 1, 26),

            // Holi
            LocalDate.of(2026, 3, 4),

            // Good Friday
            LocalDate.of(2026, 4, 3),

            // Ambedkar Jayanti
            LocalDate.of(2026, 4, 14),

            // Labour Day
            LocalDate.of(2026, 5, 1),

            // Eid-ul-Fitr (example)
            LocalDate.of(2026, 3, 20),

            // Independence Day
            LocalDate.of(2026, 8, 15),

            // Janmashtami
            LocalDate.of(2026, 9, 3),

            // Gandhi Jayanti
            LocalDate.of(2026, 10, 2),

            // Dussehra
            LocalDate.of(2026, 10, 20),

            // Diwali
            LocalDate.of(2026, 11, 8),

            // Guru Nanak Jayanti
            LocalDate.of(2026, 11, 24),

            // Christmas
            LocalDate.of(2026, 12, 25),

            // New Year's Eve
            LocalDate.of(2026, 12, 31)
    );
    @Override
    public BigDecimal calculatePrice(BigDecimal price, InventoryEntity inventory) {

        if(HOLIDAYS.contains(inventory.getDate())){
            return price.multiply(BigDecimal.valueOf(1.5));
        }
        return price;
    }

    @Override
    public String name() {
        return "Festival Strategy";
    }
}
