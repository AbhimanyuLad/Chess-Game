package com.hitech.Chess.Agent.Bean;

import java.beans.JavaBean;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;


@JavaBean
public class ChessRequest {
    
    private ArrayList<String> uciMoves;

    // Getters and Setters
    public ArrayList<String> getUciMoves() {
        return uciMoves;
    }

    public void setUciMoves(ArrayList<String> uciMoves) {
        this.uciMoves = uciMoves;
    }
}
