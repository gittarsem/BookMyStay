package com.tarsem.BookMyStay.Strategy;

import com.tarsem.BookMyStay.Entity.InventoryEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLOutput;
import java.util.List;

@Service
@AllArgsConstructor
public class PricingService {

    private List<PricingStrategy> pricingStrategyList;

    public BigDecimal calculatePrice(InventoryEntity inventory) {

        BigDecimal price = inventory.getPrice();
        for (PricingStrategy strategy : pricingStrategyList) {
            price = strategy.calculatePrice(price, inventory);
            System.out.println(strategy.name()+": "+price);
        }
        return price;
    }

    public BigDecimal calculateTotalPrice(List<InventoryEntity> list) {
        BigDecimal price = BigDecimal.ZERO;

        for (InventoryEntity inventory : list) {
            price = price.add(calculatePrice(inventory));
        }

        return price;
    }
}
