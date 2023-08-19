package com.example.reminders.multipleReminder.keyboards;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class ChangeKeyboard extends InlineKeyboardMarkup{
    public ChangeKeyboard(){
        List<List<InlineKeyboardButton>> keyboardTable = new ArrayList<>();
        List<InlineKeyboardButton> 
            row1 = new ArrayList<>(),
            row2 = new ArrayList<>(),
            row3 = new ArrayList<>();
        InlineKeyboardButton
            taskButton = new InlineKeyboardButton("Task"),
            addDateButton = new InlineKeyboardButton("Add date"),
            removeDateButton = new InlineKeyboardButton("Remove date"),
            finishButton = new InlineKeyboardButton("Finish"),
            cancelButton = new InlineKeyboardButton("Cancel");
        taskButton.setCallbackData("/change_task");
        addDateButton.setCallbackData("/change_dateadd");
        removeDateButton.setCallbackData("/change_dateremove");
        finishButton.setCallbackData("/finish");
        cancelButton.setCallbackData("/cancel");

        row1.add(taskButton);
        row2.add(addDateButton);
        row2.add(removeDateButton);
        row3.add(finishButton);
        row3.add(cancelButton);

        keyboardTable.add(row1);
        keyboardTable.add(row2);
        keyboardTable.add(row3);

        this.setKeyboard(keyboardTable);
    }
}
