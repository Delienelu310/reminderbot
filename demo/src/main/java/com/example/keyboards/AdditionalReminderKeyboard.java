package com.example.keyboards;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import com.example.threads.ReminderThread;

public class AdditionalReminderKeyboard extends InlineKeyboardMarkup{
    public AdditionalReminderKeyboard(ReminderThread thread){
        List<List<InlineKeyboardButton>> keyboardTable = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        InlineKeyboardButton
            failButton = new InlineKeyboardButton("Fail"),
            successButton = new InlineKeyboardButton("Success"),
            postponeButton = new InlineKeyboardButton("Postpone");

        failButton.setCallbackData("/reminder_thread_fail?reminder_id=" + thread.reminder.reminderId + ";thread_id=" + thread.threadId);
        successButton.setCallbackData("/reminder_thread_success?reminder_id=" + thread.reminder.reminderId + ";thread_id=" + thread.threadId);
        postponeButton.setCallbackData("/reminder_thread_postpone?reminder_id=" + thread.reminder.reminderId + ";thread_id=" + thread.threadId);

        row1.add(successButton);
        row1.add(failButton);
        row2.add(postponeButton);

        keyboardTable.add(row1);
        keyboardTable.add(row2);

        this.setKeyboard(keyboardTable);
    }

}
