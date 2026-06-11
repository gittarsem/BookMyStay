package com.tarsem.BookMyStay.Strategy;


import com.tarsem.BookMyStay.Entity.InventoryEntity;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(BigDecimal price, InventoryEntity inventory);

    String name();
}
