package com.example.reminders.multipleReminder.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.reminders.multipleReminder.keyboards.AddNewDateKeyboard;

public class AddNewDateMessage extends SendMessage{
    
    public AddNewDateMessage(String chatId){
        this.setChatId(chatId);
        this.setText("Do you want to add new date?");
        this.setReplyMarkup(new AddNewDateKeyboard());
    }
}
