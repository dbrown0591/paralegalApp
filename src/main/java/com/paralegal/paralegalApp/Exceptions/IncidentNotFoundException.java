package com.paralegal.paralegalApp.Exceptions;

public class IncidentNotFoundException extends RuntimeException{

    public IncidentNotFoundException(String message){
        super("Incident not found with ID: ");
    }


}
