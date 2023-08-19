package com.example.reminders.multipleReminder.messages;

import java.time.LocalDateTime;
import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.reminders.multipleReminder.keyboards.ChooseDateKeyboard;

public class ChooseDateMessage extends SendMessage {
    public ChooseDateMessage(String chatId, Map<Integer, LocalDateTime> dateMap){

        this.setChatId(chatId);
        this.setText("Choose date you want to remove");
        this.setReplyMarkup(new ChooseDateKeyboard(dateMap));
    }
}
