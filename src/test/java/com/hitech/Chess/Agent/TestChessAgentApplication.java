package com.hitech.Chess.Agent;

import org.springframework.boot.SpringApplication;

public class TestChessAgentApplication {

	public static void main(String[] args) {
		SpringApplication.from(ChessAgentApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
