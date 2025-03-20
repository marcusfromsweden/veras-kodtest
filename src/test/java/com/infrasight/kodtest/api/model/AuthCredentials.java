package com.infrasight.kodtest.api.model;

public class AuthCredentials {
    public String user;
    public String password;

    public AuthCredentials(String user, String password) {
        this.user = user;
        this.password = password;
    }
}
