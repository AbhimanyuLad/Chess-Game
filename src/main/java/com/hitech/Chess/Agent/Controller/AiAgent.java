package com.hitech.Chess.Agent.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hitech.Chess.Agent.Bean.DifficultyRequest;
import com.hitech.Chess.Agent.Entity.InvalidMoveException;
import com.hitech.Chess.Agent.Service.DifficultyLevel;
import com.hitech.Chess.Agent.Service.GeminiService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;



@Slf4j
@RestController
@RequestMapping("/chess")
public class AiAgent {

    @Autowired
    private GeminiService geminiService;


    @PostMapping("/home")
    public ResponseEntity<?> difficultyLevel(@RequestBody DifficultyRequest request, HttpSession session) {
        try {
            log.info("Session ID: {}", session.getId());
            log.info("Received difficulty: {}", request.getDifficulty());
            log.info("Received Timer: {}", request.getGameTimer());
            log.info("Received game moves: {}", request.getGameMoves());
            
            // Setting the difficulty into session
            DifficultyLevel level = DifficultyLevel.valueOf(request.getDifficulty().toUpperCase());
            session.setAttribute("difficulty", level);
            session.setAttribute("gameTimer", request.getGameTimer());
    
            log.info("Difficulty level set to: {}", session.getAttribute("difficulty"));
            return new ResponseEntity<>("Difficulty level set to " + request.getDifficulty(), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid difficulty level", HttpStatus.BAD_REQUEST);
        }
    }
    

    @GetMapping("/moves")
    public ResponseEntity<?> getMoves(@RequestBody List<String> move, HttpSession session) {
        try {
            log.info("Received move: {}", move);
            log.info("Session ID: {}", session.getId());
            
            DifficultyLevel difficulty = (DifficultyLevel) session.getAttribute("difficulty");

            //  Step 2: If not set, return error
            if (difficulty == null) {
                log.error("Difficulty level not set");
                return new ResponseEntity<>("Difficulty level not set. Please start a new game.", HttpStatus.BAD_REQUEST);
            }
            String result = geminiService.checkInputAsPerUCIStandards(move, difficulty);
            
             // Parse only a cleaned copy of result to extract move
            String cleanedResult = result.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(cleanedResult);

            String from = node.path("from").asText();
            String to = node.path("to").asText();

            if (!from.isEmpty() && !to.isEmpty()) {
                String combinedMove = from + to;
                move.add(combinedMove);
                List<String> updatedMoveList = geminiService.updateMoveHistory(session, move);
                log.info("Added combined move to history: {}", combinedMove);
                return new ResponseEntity<>(updatedMoveList, HttpStatus.OK);
            } else {
                log.warn("From/To not found in result. No move added.");
                return new ResponseEntity<>("Invalid move format", HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            log.error("Error processing move: {}", "Wrong move format");
            return new ResponseEntity<>("Wrong move format", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/end")
    public ResponseEntity<String> endGame(HttpSession session) {
        try { 
            log.info("Ending session with ID: {}", session.getId());
            session.invalidate(); // This will terminate the session
            log.info("Session invalidated successfully.");
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Session ended successfully.");
        } catch (Exception e) {
            log.error("Error ending session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error ending session");
        }
    }

    @RequestMapping("/**")
    public ResponseEntity<String> handleUnknownRoutes() {
        return new ResponseEntity<>("Unknown route", HttpStatus.NOT_FOUND);
    }

}

    

