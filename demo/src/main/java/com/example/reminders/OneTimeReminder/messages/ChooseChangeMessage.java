package com.example.reminders.OneTimeReminder.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.reminders.OneTimeReminder.keyboards.ChangeKeyboard;

public class ChooseChangeMessage extends SendMessage{
    public ChooseChangeMessage(String chatId){
        this.setChatId(chatId);
        this.setText("What do you want to change?");

        this.setReplyMarkup(new ChangeKeyboard());
    }
}
