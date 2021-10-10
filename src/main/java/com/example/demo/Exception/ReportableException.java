package com.example.demo.Exception;


public class ReportableException extends RuntimeException{
    public ReportableException(){
        super();
    }
    public ReportableException(String message){
        super(message);
    }
}
