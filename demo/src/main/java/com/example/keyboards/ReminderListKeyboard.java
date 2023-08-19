package com.example.keyboards;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.example.reminders.Reminder;

public class ReminderListKeyboard extends InlineKeyboardMarkup{
    
    public ReminderListKeyboard(ArrayList<Reminder> reminderList, int from, int to){

        //preparing inline keyboard with a list of reminders:
        List<List<InlineKeyboardButton>> keyboardTable = new ArrayList<>();

        
        for(Reminder reminder : reminderList){
            List<InlineKeyboardButton> row = new ArrayList<>();

            InlineKeyboardButton button = new InlineKeyboardButton(); 
            button.setText(reminder.toShortString());
            button.setCallbackData("/get_reminder?reminder_id=" + reminder.reminderId);

            row.add(button);
            keyboardTable.add(row);
        }

        //preparing navigation bar

        List<InlineKeyboardButton> navigationRow = new ArrayList<>();

        InlineKeyboardButton 
            back = new InlineKeyboardButton("Back"),
            next = new InlineKeyboardButton("Next");

        back.setCallbackData("/reminder_list?from=" +  ((from - 1) - (to-from)) + ";to=" + (from - 1));
        next.setCallbackData("/reminder_list?from=" + (to + 1) + ";to=" + (to + 1 + (to-from)));

        navigationRow.add(back);
        navigationRow.add(next);

        keyboardTable.add(navigationRow);

        //finishing

        this.setKeyboard(keyboardTable);

    }
}
