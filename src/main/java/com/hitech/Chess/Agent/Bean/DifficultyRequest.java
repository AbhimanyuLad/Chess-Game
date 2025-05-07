package com.hitech.Chess.Agent.Bean;

import java.beans.JavaBean;
import java.util.List;

import org.springframework.lang.NonNull;

import lombok.Data;

@JavaBean
@Data
public class DifficultyRequest {

    @NonNull
    private String difficulty;

    @NonNull
    private String gameTimer;

    private List<String> gameMoves;


}
