package com.example.reminders.weekReminder.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.reminders.weekReminder.keyboards.ChooseWeekdaysKeyboard;

public class ChooseWeekdaysMessage extends SendMessage{
    public ChooseWeekdaysMessage(String chatId){
        this.setChatId(chatId);
        this.setText("Choose the days, you want to get your reminder");
        this.setReplyMarkup(new ChooseWeekdaysKeyboard());

    }
}
