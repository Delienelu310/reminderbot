package com.example.keyboards;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.example.reminders.Reminder;

public class ReminderKeyboard extends InlineKeyboardMarkup{
    public ReminderKeyboard(String chatId, Reminder reminder){
        List<List<InlineKeyboardButton>> keyboardTable = new ArrayList<>();

        

        this.setKeyboard(keyboardTable);
    }
}
