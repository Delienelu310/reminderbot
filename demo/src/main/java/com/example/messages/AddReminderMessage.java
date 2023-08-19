package com.example.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.keyboards.ChooseReminderTypeKeyboard;

public class AddReminderMessage extends SendMessage{
    
    public AddReminderMessage(String chatId){
        this.setChatId(chatId);
        this.setText("Choose the type of reminder, you want to create");

        this.setReplyMarkup(new ChooseReminderTypeKeyboard());
    }
}
