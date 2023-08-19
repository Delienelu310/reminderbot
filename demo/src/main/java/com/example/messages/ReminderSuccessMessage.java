package com.example.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class ReminderSuccessMessage extends SendMessage{
    
    public ReminderSuccessMessage(String chatId){
        this.setChatId(chatId);
        this.setText("You are just like RYAN GOSLING, all chicks belong to you");
    }
}
