package com.hitech.Chess.Agent.Service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;

@Service
@Slf4j
public class GeminiService {


    //AIzaSyAveGZgztm63hEz-bUH020kBJCv2s6jAXU
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=AIzaSyAveGZgztm63hEz-bUH020kBJCv2s6jAXU";

    public String jdbcUrl = "jdbc:sqlite:/D:\\Projects\\Chess-Agent\\src\\main\\resources\\mydatabase.db";

    public static String checkInputAsPerUCIStandards(List<String> positionHistory, DifficultyLevel difficulty) {
        
        for (String move : positionHistory) {
            if (!move.matches("^[a-h][1-8][a-h][1-8][qrbn]?$")) {
                throw new IllegalArgumentException("Invalid UCI move format: " + move);
            }
        }
        return buildPrompt(positionHistory, difficulty);
    }
    
    


    @Transactional
    private static String buildPrompt(List<String> positionHistory, DifficultyLevel difficulty) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a chess player.\n");
        prompt.append("Based on the history of UCI moves, give the next move using only \"from\" and \"to\" squares.\n\n");
        prompt.append("Please provide the move in UCI format for the chess piece for Example: from \"f3\" to \"g1\". The correct format is \"f3g1\".\n");
        switch (difficulty) {
            case EASY -> prompt.append("Play a random legal move.\n");
            case MODERATE -> prompt.append("Play a reasonable move based on strategy.\n");
            case HARD -> prompt.append("Analyze deeply and play the strongest move.\n");
        }
        prompt.append("Here is the move history:\n");
        for (int i = 0; i < positionHistory.size(); i++) {
            prompt.append(positionHistory.get(i));
            if (i < positionHistory.size() - 1) {
                prompt.append(", ");
            }
        }

        prompt.append("\n\nWhat is your next move? Reply strictly as JSON in this format:\n");
        prompt.append("{\n  \"from\": \"square\",\n  \"to\": \"square\"\n}");
        log.info("Prompt: {}", prompt.toString());
        return callAgent(prompt.toString()); 
    }

    // Method to call the agent and get the response
    @Transactional
     private static String callAgent(String prompt) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // JSON payload
            String jsonData = """
                {
                  "contents": [
                    {
                      "parts": [
                        {"text": "%s"}
                      ]
                    }
                  ]
                }
                """.formatted(prompt.replace("\"", "\\\""));  // escape quotes for JSON

            // Send request
            try (OutputStream os = connection.getOutputStream()) {      //connection.getOutputStream() → Opens a pipe (like a channel) to send data to the API.
                byte[] input = jsonData.getBytes("utf-8");   //jsonData.getBytes("utf-8") → Converts your prompt (in JSON format) into bytes so that it can be transmitted over the internet.
                os.write(input, 0, input.length);                    //os.write(...) → Sends the JSON data to Gemini.
            }

            // Read response
            StringBuilder response = new StringBuilder();  
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) { //connection.getInputStream() opens a stream to read the data Gemini sends back.
                String responseLine;                  //BufferedReader reads that response line by line.
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());                 //response.append(...) adds each line to the response string.
                }
            }

            // Parse only the text part of the first candidate
            ObjectMapper mapper = new ObjectMapper();           //ObjectMapper from Jackson library is used to parse the JSON response.
            JsonNode root = mapper.readTree(response.toString());       //.readTree(...) turns the raw response string into a navigable JSON tree.
            JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");  //root.path(...).get(...).path(...).path(...) → Walks down the tree to find the actual generated text from Gemini.
            log.info("Response: {}", textNode.asText()); 
            return textNode.asText();  //textNode.asText() → Gets the actual text value (e.g., the chess move or AI explanation).

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Failed to get response from Gemini API.\"}";
        }
            
    }

    @Transactional
    public List<String> updateMoveHistory(HttpSession session, List<String> moveHistory) {
        session.setAttribute("moveHistory", moveHistory);
        return moveHistory;
    }

}    
    
  



