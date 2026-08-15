package com.smartlogistics;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class SmartLogisticsApplication {

    public static void main(String[] args) {
        loadEnv();
        SpringApplication.run(SmartLogisticsApplication.class, args);
    }

    private static void loadEnv() {
        try {
            Dotenv dotenv;
            if (new File(".env").exists()) {
                dotenv = Dotenv.configure().ignoreIfMissing().load();
            } else if (new File("../.env").exists()) {
                dotenv = Dotenv.configure().directory("../").ignoreIfMissing().load();
            } else if (new File("smart-logistics-backend/.env").exists()) {
                dotenv = Dotenv.configure().directory("smart-logistics-backend").ignoreIfMissing().load();
            } else {
                dotenv = Dotenv.configure().ignoreIfMissing().load();
            }

            if (dotenv != null) {
                dotenv.entries().forEach(entry -> {
                    if (System.getProperty(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load .env file directly (" + e.getMessage() + "). Using environment variables.");
        }
    }
}
