package com.example.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class StartMessage extends SendMessage{
    public StartMessage(String chatId){
        this.setChatId(chatId);
        this.setText("Welcome to the reminder bot! This bot is created to spam you whenever you forget to study and make you feel guilty for" + 
            " any sort of inaction. Also one time in a week it can randomly send you dickpeek. Enjoy)");
    }
}
