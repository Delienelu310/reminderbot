package com.example.reminders.multipleReminder.keyboards;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class ChooseDateKeyboard extends InlineKeyboardMarkup{
    public ChooseDateKeyboard(Map<Integer, LocalDateTime> dateMap){

        List<List<InlineKeyboardButton>> keyboardTable = new ArrayList<>();

        for(int i : dateMap.keySet()){
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton dateButton = new InlineKeyboardButton(dateMap.get(i).toString());
            dateButton.setCallbackData("/change_dateremove?dateindex=" + i);

            row.add(dateButton);
            keyboardTable.add(row);
        }

        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton cancelButton = new InlineKeyboardButton("Cancel");
        cancelButton.setCallbackData("/cancel");

        row.add(cancelButton);
        keyboardTable.add(row);

        this.setKeyboard(keyboardTable);
    }
}
