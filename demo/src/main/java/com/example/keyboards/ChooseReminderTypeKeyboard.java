package com.example.keyboards;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class ChooseReminderTypeKeyboard extends InlineKeyboardMarkup{
    
    public ChooseReminderTypeKeyboard(){
        List<List<InlineKeyboardButton>> keyboardTable = new ArrayList<>();


        //here i want to learn how to use reflect in order 
        //to make it automatically look into prepared file, 
        //where, when new type of reminder is added
        //i can type that in a comfortable way
        List<InlineKeyboardButton> 
            row1 = new ArrayList<>(),
            row2 = new ArrayList<>();
        
        InlineKeyboardButton
            oneTimeButton = new InlineKeyboardButton("One time", null, "/add_reminder?type=onetime", null, null, null, null, null, null),
            multipleTimesButton = new InlineKeyboardButton("Multiple times", null, "/add_reminder?type=multiple", null,null,null,null,null,null),
            weekReminderButton = new InlineKeyboardButton("Weekly");
        weekReminderButton.setCallbackData("/add_reminder?type=weekly");
        
        row1.add(oneTimeButton);
        row1.add(multipleTimesButton);
        row2.add(weekReminderButton);
        
        keyboardTable.add(row1);
        keyboardTable.add(row2);

        this.setKeyboard(keyboardTable);
    }
}
