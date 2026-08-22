package com.tarsem.BookMyStay.Strategy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final List<PricingStrategy> pricingStrategyList;

    /**
     * Applies all dynamic pricing strategies to a base amount.
     *
     * Example:
     *
     * Base amount = 1000
     *
     * Weekend   = 20%
     * Festival  = 50%
     * Occupancy = 10%
     *
     * Total adjustment = 80%
     *
     * Final = 1000 * 1.80 = 1800
     */
    public BigDecimal calculatePrice(
            BigDecimal baseAmount,
            InventoryEntity inventory
    ) {

        BigDecimal totalAdjustment = BigDecimal.ZERO;

        for (PricingStrategy strategy : pricingStrategyList) {

            BigDecimal adjustment =
                    strategy.calculateAdjustment(inventory);

            totalAdjustment =
                    totalAdjustment.add(adjustment);

        }

        /*
         * Maximum dynamic increase = 100%.
         *
         * Therefore final price cannot exceed
         * 2 × base amount.
         */
        totalAdjustment = totalAdjustment.min(
                BigDecimal.ONE
        );

        BigDecimal multiplier =
                BigDecimal.ONE.add(totalAdjustment);

        return baseAmount
                .multiply(multiplier)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }


    /**
     * Calculates daily booking price.
     *
     * Each inventory date gets its own
     * dynamic pricing.
     */
    public BigDecimal calculateDailyTotal(
            BigDecimal dailyBasePrice,
            List<InventoryEntity> inventories
    ) {

        BigDecimal total = BigDecimal.ZERO;

        for (InventoryEntity inventory : inventories) {

            BigDecimal price =
                    calculatePrice(
                            dailyBasePrice,
                            inventory
                    );

            total = total.add(price);
        }

        return total.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
}