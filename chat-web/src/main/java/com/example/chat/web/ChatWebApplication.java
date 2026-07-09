package com.example.chat.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.chat")
public class ChatWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatWebApplication.class, args);
	}
}
