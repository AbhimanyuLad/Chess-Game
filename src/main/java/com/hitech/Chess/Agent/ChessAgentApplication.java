package com.hitech.Chess.Agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;



@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ChessAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChessAgentApplication.class, args);
	}

}
