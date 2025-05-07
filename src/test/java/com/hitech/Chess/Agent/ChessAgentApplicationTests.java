package com.hitech.Chess.Agent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.hitech.Chess.Agent.Service.DifficultyLevel;
import com.hitech.Chess.Agent.Service.GeminiService;

import jakarta.servlet.http.HttpSession;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ChessAgentApplicationTests {

	@Test
	void contextLoads() {
		List<String> validMoves = List.of("e2e4", "g1f3", "e7e8q");
        DifficultyLevel level = DifficultyLevel.EASY;
        // This should pass without exceptions
        assertDoesNotThrow(() -> GeminiService.checkInputAsPerUCIStandards(validMoves, level));
	}

	@Test
    public void testInvalidMoves() {
        // Invalid UCI moves
        List<String> invalidMoves = List.of("e9e4","hello", "e2-e4");
        DifficultyLevel level = DifficultyLevel.EASY; // Replace with an appropriate value or mock
        
        // This should throw an exception
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            GeminiService.checkInputAsPerUCIStandards(invalidMoves,level);
        });
        
        assertEquals("Invalid UCI move: e9e4", exception.getMessage());
    }

    @Test
    public void testEmptyMoves() {
        // Empty list should also pass as there's nothing to validate
        List<String> emptyList = List.of();
        DifficultyLevel level = DifficultyLevel.EASY;
        assertDoesNotThrow(() -> GeminiService.checkInputAsPerUCIStandards(emptyList, level));
    }

}
