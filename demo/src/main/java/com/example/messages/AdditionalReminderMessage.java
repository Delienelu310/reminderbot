package com.example.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.keyboards.AdditionalReminderKeyboard;
import com.example.threads.ReminderThread;

public class AdditionalReminderMessage extends SendMessage{
    public AdditionalReminderMessage(String chatId, ReminderThread thread){
        this.setChatId(chatId);
        this.setText("The next remind will appear in 5 minutes");

        this.setReplyMarkup(new AdditionalReminderKeyboard(thread));
    }
}
