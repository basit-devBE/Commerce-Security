package com.example.commerce.events;

import lombok.Getter;

@Getter
public class UserRegisterationEvent {
    private final  String email;

    public UserRegisterationEvent(String email) {
        this.email = email;
    }
}
