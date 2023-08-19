package com.example.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class HelpMessage extends SendMessage{
    public HelpMessage(String chatId){
        this.setText("Help information");
        this.setChatId(chatId);
    }
}
