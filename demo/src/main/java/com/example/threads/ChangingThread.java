package com.example.threads;

import com.example.TelegramTestBot;
import com.example.User;

public abstract class ChangingThread extends Thread{
    
    public TelegramTestBot bot;
    public User user;

    public ChangingThread(TelegramTestBot bot, User user){
        this.bot = bot;
        this.user = user;
    }

    public abstract void run();
}
