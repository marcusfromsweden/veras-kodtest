package com.infrasight.kodtest.dto;

public class AuthCredentials {
    public String user;
    public String password;

    public AuthCredentials(String user, String password) {
        this.user = user;
        this.password = password;
    }
}
