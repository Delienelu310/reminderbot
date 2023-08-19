package com.example;

import com.example.reminders.Reminder;

public class User {

    public static enum UserStatus{
        DEFAULT, WAITING_INPUT
    }

    public String id;
    public String username;

    public UserStatus status = UserStatus.DEFAULT;
    public Reminder currentReminder = null;
    
    public Thread currentThread = null;
    public String inputField = null;
    public String inputData = null;

    public int remindersCount = 0;

    public User(String id, String username){
        this.id = id;
        this.username = username;
    }

    public User(String id, String username, int remindersCount){
        this.id = id;
        this.username = username;
        this.remindersCount = remindersCount;
    }
}
