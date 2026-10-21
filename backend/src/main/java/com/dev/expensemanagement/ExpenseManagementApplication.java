package com.dev.expensemanagement;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExpenseManagementApplication {

    public static void main(String[] args) {
        // Load .env file
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
        
        // Set system properties for Spring Boot to use
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        // Some IDEs and developer tools export a generic DEBUG variable. Spring
        // Boot also recognizes it and may print the full condition report,
        // including environment values. Use a dedicated opt-in variable instead.
        if (System.getProperty("debug") == null) {
            System.setProperty("debug", dotenv.get("SPRING_BOOT_DEBUG", "false"));
        }

        SpringApplication.run(ExpenseManagementApplication.class, args);
    }

}
