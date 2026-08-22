package com.tarsem.BookMyStay.Strategy;


import com.tarsem.BookMyStay.Entity.InventoryEntity;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculateAdjustment(InventoryEntity inventory);
    String name();
}
