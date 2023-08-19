package com.example.keyboards;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class PrintKeyboard extends InlineKeyboardMarkup{
    
    public PrintKeyboard(int reminderId){
        List<List<InlineKeyboardButton>> keyboardTable = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();

        //buttons
        InlineKeyboardButton
            changeButton = new InlineKeyboardButton("Change"),
            deleteButton = new InlineKeyboardButton("Delete");
        changeButton.setCallbackData("/change_reminder?reminder_id=" + reminderId);
        deleteButton.setCallbackData("/delete_reminder?reminder_id=" + reminderId);

        row1.add(changeButton);
        row1.add(deleteButton);

        //markup
        keyboardTable.add(row1);
        this.setKeyboard(keyboardTable);
    }
}
