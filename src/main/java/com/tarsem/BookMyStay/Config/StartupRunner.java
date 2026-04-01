package com.tarsem.BookMyStay.Config;

import com.tarsem.BookMyStay.Service.InventoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    @Autowired
    private InventoryServiceImpl inventoryService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(args.getOptionNames());
        System.out.println(args.getOptionValues("name"));
        inventoryService.scheduledInventoryJob();
    }
}
