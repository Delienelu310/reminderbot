package com.example.threads;

import java.time.Duration;
import java.time.LocalDateTime;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.TelegramTestBot;
import com.example.messages.AdditionalReminderMessage;
import com.example.reminders.Reminder;

public class ReminderThread extends Thread{
    
    public int threadId;
    TelegramTestBot bot;
    public Reminder reminder;
    LocalDateTime finalDate;

    public LocalDateTime date;
    
    public ReminderThread(TelegramTestBot bot, Reminder reminder, LocalDateTime date){
        this.reminder = reminder;
        this.bot = bot;

        this.date = date;

        this.threadId = ++reminder.threadCounter;

    }

    public void run(){
        try{
            LocalDateTime currentTime = LocalDateTime.now();
            long duration = Duration.between(currentTime, date).toMillis();
            sleep(duration > 0 ? duration : 0);

            bot.sendMsg(new SendMessage(reminder.userId, "Reminder: " + reminder.task));

            //removing old reminder and creating new
            bot.reminderControlThread.removeReminderThread(reminder, threadId);
            System.out.println(1);
            ReminderThread newReminderThread = new ReminderThread(bot, reminder, LocalDateTime.now().plusMinutes(5));
            bot.reminderControlThread.addReminderThread(newReminderThread);
            
            bot.sendMsg(new AdditionalReminderMessage(reminder.userId, newReminderThread));

        }catch(InterruptedException e){
            System.out.println("The thread of reminder with id " + reminder.reminderId + " of user " + reminder.userId + " was interrupted");
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("smth went wrong in reminder thread of reminder with id " + reminder.reminderId + " of user with id" +
                reminder.userId);
        }
    }
}
