package com.zdc.luence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.zdc.luence.**")
public class App {

	public static void main(String[] args) {
		new SpringApplication(App.class).run(args);
	}
}
