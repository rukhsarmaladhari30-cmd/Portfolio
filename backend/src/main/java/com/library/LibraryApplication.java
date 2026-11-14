package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LibraryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
        System.out.println("=================================================");
        System.out.println("Online Library Management System (Spring Boot)");
        System.out.println("API running at http://localhost:8080/api");
        System.out.println("Author: Rukhsar Maladhari | Java Full Stack");
        System.out.println("=================================================");
    }
}

