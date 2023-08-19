package com.example.reminders.multipleReminder.keyboards;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class AddNewDateKeyboard extends InlineKeyboardMarkup{
    public AddNewDateKeyboard(){
        List<List<InlineKeyboardButton>> keyboardTable = new ArrayList<>();
        List<InlineKeyboardButton> 
            row1 = new ArrayList<>(),
            row2 = new ArrayList<>();

        InlineKeyboardButton
            addAnotherDate = new InlineKeyboardButton("Add new"),
            finish = new InlineKeyboardButton("Finish"),
            cancel = new InlineKeyboardButton("Cancel");

        addAnotherDate.setCallbackData("/add_multiplereminder_adddate");
        finish.setCallbackData("/add_multiplereminder_finish");
        cancel.setCallbackData("/cancel");
        
        row1.add(addAnotherDate);
        row1.add(finish);
        row2.add(cancel);

        keyboardTable.add(row1);
        keyboardTable.add(row2);

        this.setKeyboard(keyboardTable);
    }
}
