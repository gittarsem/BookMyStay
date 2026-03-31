package com.tarsem.BookMyStay.Config;

import com.tarsem.BookMyStay.Service.InventoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private InventoryServiceImpl inventoryService;
    @Override
    public void run(String... args) throws Exception {
        inventoryService.scheduledInventoryJob();
    }
}
