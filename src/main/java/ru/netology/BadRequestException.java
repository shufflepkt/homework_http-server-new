package ru.netology;

public class BadRequestException extends Exception {
    public BadRequestException(String msg) {
        super("Bad request exception. The request does not contain method, path and/or protocol version. \n" +
                "The request is the following: " + msg);
    }
}
