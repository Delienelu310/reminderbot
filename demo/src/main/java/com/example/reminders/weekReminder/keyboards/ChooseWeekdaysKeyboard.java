package com.example.reminders.weekReminder.keyboards;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class ChooseWeekdaysKeyboard extends InlineKeyboardMarkup{
    public ChooseWeekdaysKeyboard(){
        List<List<InlineKeyboardButton>> keyboardTable = new ArrayList<>();

        InlineKeyboardButton
            monday = new InlineKeyboardButton("Monday"),
            tuesday = new InlineKeyboardButton("Tuesday"),
            wednesday = new InlineKeyboardButton("Wednesday"),
            thirsday = new InlineKeyboardButton("Thirsday"),
            friday = new InlineKeyboardButton("Friday"),
            saturday = new InlineKeyboardButton("Saturday"),
            sunday = new InlineKeyboardButton("Sunday");
        monday.setCallbackData("/week_reminder_toggle?day=" + 1);
        tuesday.setCallbackData("/week_reminder_toggle?day=" + 2);
        wednesday.setCallbackData("/week_reminder_toggle?day=" + 3);
        thirsday.setCallbackData("/week_reminder_toggle?day=" + 4);
        friday.setCallbackData("/week_reminder_toggle?day=" + 5);
        saturday.setCallbackData("/week_reminder_toggle?day=" + 6);
        sunday.setCallbackData("/week_reminder_toggle?day=" + 7);

        List<InlineKeyboardButton>
            row1 = new ArrayList<>(),
            row2 = new ArrayList<>(),
            row3 = new ArrayList<>(),
            row4 = new ArrayList<>(),
            row5 = new ArrayList<>(),
            row6 = new ArrayList<>(),
            row7 = new ArrayList<>();
        row1.add(monday);
        row2.add(tuesday);
        row3.add(wednesday);
        row4.add(thirsday);
        row5.add(friday);
        row6.add(saturday);
        row7.add(sunday);



        keyboardTable.add(row1);
        keyboardTable.add(row2);
        keyboardTable.add(row3);
        keyboardTable.add(row4);
        keyboardTable.add(row5);
        keyboardTable.add(row6);
        keyboardTable.add(row7);

        InlineKeyboardButton   
            finish = new InlineKeyboardButton("Finish"),
            cancel = new InlineKeyboardButton("Cancel");
        finish.setCallbackData("/finish");
        cancel.setCallbackData("/cancel");

        List<InlineKeyboardButton>
            finalRow = new ArrayList<>();
        finalRow.add(finish);
        finalRow.add(cancel);
        keyboardTable.add(finalRow);

        this.setKeyboard(keyboardTable);
    }
}
