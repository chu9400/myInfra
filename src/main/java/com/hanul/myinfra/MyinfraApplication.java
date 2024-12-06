package com.hanul.myinfra;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyinfraApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyinfraApplication.class, args);
	}

	// SIGTERM이나 SIGINT가 발생하면 실행되는 코드
	@PreDestroy
	public void onShutdown() {
		System.out.println("애플리케이션 종료 중...");
	}

}
