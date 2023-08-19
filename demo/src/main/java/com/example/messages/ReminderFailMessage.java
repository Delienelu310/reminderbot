package com.example.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class ReminderFailMessage extends SendMessage{
    
    public ReminderFailMessage(String chatId){
        this.setChatId(chatId);
        this.setText("Have you played WOW all time again? 100 push-ups");
    }
}
