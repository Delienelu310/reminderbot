package com.example.keyboards;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class MainKeyboard extends ReplyKeyboardMarkup{
    public MainKeyboard(){
        this.setIsPersistent(true);

        List<KeyboardRow>  keyboardTable = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();

        keyboardTable.add(keyboardRow1);

        KeyboardButton 
            addReminderButton = new KeyboardButton("Add Reminder"),
            reminderListButton = new KeyboardButton("Reminder List"),
            helpButton = new KeyboardButton("Help");

        
        keyboardRow1.add(addReminderButton);
        keyboardRow1.add(reminderListButton);
        keyboardRow1.add(helpButton);

        this.setKeyboard(keyboardTable);
    }
}
