package com.example.reminders.OneTimeReminder.keyboards;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class ChangeKeyboard extends InlineKeyboardMarkup{
    public ChangeKeyboard(){
        List<List<InlineKeyboardButton>> keyboardTable = new ArrayList<>();
        List<InlineKeyboardButton> 
            row1 = new ArrayList<>(),
            row2 = new ArrayList<>();

        //preparing buttons

        InlineKeyboardButton
            taskButton = new InlineKeyboardButton("task"),
            dateButton = new InlineKeyboardButton("date"),
            finishButton =  new InlineKeyboardButton("finish"),
            cancelButton = new InlineKeyboardButton("cancel");

        taskButton.setCallbackData("/change_task");
        dateButton.setCallbackData("/change_date");
        finishButton.setCallbackData("/finish");
        cancelButton.setCallbackData("/cancel");

        row1.add(taskButton);
        row1.add(dateButton);
        row2.add(finishButton);
        row2.add(cancelButton);

        //rows

        keyboardTable.add(row1);
        keyboardTable.add(row2);

        //the markup
        this.setKeyboard(keyboardTable);

    }
}
