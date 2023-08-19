package com.example.reminders.weekReminder.keyboards;

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
            task = new InlineKeyboardButton("Task"),
            time = new InlineKeyboardButton("Time"),
            weekDays = new InlineKeyboardButton("Weed days included"),
            finish = new InlineKeyboardButton("Finish"),
            cancel = new InlineKeyboardButton("Cancel");
        task.setCallbackData("/change_weekreminder_task");
        time.setCallbackData("/change_weekreminder_time");
        weekDays.setCallbackData("/week_reminder_toggle");
        finish.setCallbackData("/finish");
        cancel.setCallbackData("/cancel");

        row1.add(task);
        row1.add(time);
        row2.add(weekDays);
        row3.add(finish);
        row3.add(cancel);

        keyboardTable.add(row1);
        keyboardTable.add(row2);
        keyboardTable.add(row3);

        this.setKeyboard(keyboardTable);

    }

    

}
