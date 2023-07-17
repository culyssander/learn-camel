package com.dxc.learncamelspring;

public class CustomException extends RuntimeException{

    public CustomException() {}

    public CustomException(Integer code, String message) {
        super(message);
    }

    public CustomException(String message) {
        super(message);
    }
}
