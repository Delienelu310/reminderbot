package com.example.threads;

import java.time.LocalDateTime;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.TelegramTestBot;
import com.example.User;

public class PostponeReminderThread extends Thread{
    
    TelegramTestBot bot = null;
    User user = null;
    int threadId = -1;

    public PostponeReminderThread(TelegramTestBot bot, User user, int threadId){
        this.bot = bot;
        this.user = user;
        this.threadId = threadId;
    }

    @Override 
    public void run(){
        
        synchronized (this){ 
            try{
                bot.sendMsg(new SendMessage(user.id, "Type in time in minutes, for which you want to postpone"));
                wait();

                int minutes = 0;
                while(true){
                    
                    String input = user.inputData;
                    try{
                        minutes = Integer.parseInt(input);
                        if(! (minutes >= 1 && minutes < 60 * 24)){
                            bot.sendMsg(new SendMessage(user.id, "Your must input the number between 1 and 24 * 60, try again"));
                            wait();
                            continue;
                        }
                    }catch(Exception e){
                        bot.sendMsg(new SendMessage(user.id, "Your input is invalid fot integer number, try again"));
                        wait();
                        continue;
                    }
                    break;
                }

                this.bot.reminderControlThread.removeReminderThread(user.id, user.currentReminder.reminderId, threadId);
                this.bot.reminderControlThread.addReminderThread(new ReminderThread(bot, user.currentReminder, LocalDateTime.now().plusMinutes(minutes)));

                bot.sendMsg(new SendMessage(user.id, "You have successfully posponed the reminder"));
                this.user.currentReminder = null;
                this.user.inputData = null;
                this.user.status = User.UserStatus.DEFAULT;
            
            }catch(Exception e){
                System.out.println(e.getMessage());

                bot.sendMsg(new SendMessage(this.user.id, "The postponning process was cancelled"));
                this.user.currentReminder = null;
                this.user.inputData = null;
                this.user.status = User.UserStatus.DEFAULT;
            }
        }
    }
}
