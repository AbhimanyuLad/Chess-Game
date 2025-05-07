package com.hitech.Chess.Agent.Entity;

public class InvalidMoveException  extends RuntimeException {
    // Constructor accepting a custom message
    public InvalidMoveException(String message) {
        super(message);
    } 

}
