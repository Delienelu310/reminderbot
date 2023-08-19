package com.example.reminders.weekReminder.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.reminders.weekReminder.keyboards.ChangeKeyboard;

public class ChangeMessage extends SendMessage{
    public ChangeMessage(String chatId){
        this.setChatId(chatId);
        this.setText("Choose what you want to change");
        this.setReplyMarkup(new ChangeKeyboard());
    }
}
