package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LaterApplication {
    public static void main(String[] args) {
        // Эта строчка запускает всё: контекст, Tomcat и сканирование компонентов
        SpringApplication.run(LaterApplication.class, args);
    }
}
