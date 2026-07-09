package com.example.chat.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.chat")
public class ChatAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatAdminApplication.class, args);
	}
}
