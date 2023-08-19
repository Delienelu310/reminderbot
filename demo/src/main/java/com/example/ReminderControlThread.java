package com.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.example.reminders.Reminder;
import com.example.threads.ReminderThread;

public class ReminderControlThread extends Thread{
    
    TelegramTestBot bot;
    LocalDateTime currentMaxTime;

    private Map<String, Map<Integer, ArrayList<ReminderThread>>> runningReminderThreads = new HashMap<>();    

    public ReminderControlThread(TelegramTestBot bot){
        this.bot = bot;
    }

    public void run(){
        try{
            do{

                this.currentMaxTime = LocalDateTime.now().plusHours(1);
                Map<String, Map<Integer, Reminder>> reminders = bot.db.getAllReminders();
                System.out.println(reminders);

                for(String key : reminders.keySet()){
                    Map<Integer,Reminder> currentReminders = reminders.get(key);

                    for(Reminder reminder : currentReminders.values()){

                        if(!this.runningReminderThreads.containsKey(key)) this.runningReminderThreads.put(key, new HashMap<Integer, ArrayList<ReminderThread>>());
                        
                        ArrayList<LocalDateTime> dates = reminder.getDatesBefore(currentMaxTime);
                        System.out.println(dates);

                        if(dates.size() == 0) continue;
                        //sorting
                        Collections.sort(dates, new Comparator<LocalDateTime>() {
                            @Override
                            public int compare(LocalDateTime dateTime1, LocalDateTime dateTime2){
                                return dateTime1.compareTo(dateTime2);
                            }
                        }); 

                        if(!this.runningReminderThreads.get(key).containsKey(reminder.reminderId))
                            this.runningReminderThreads.get(key).put(reminder.reminderId, new ArrayList<ReminderThread>());
                        
                        //making the reminders
                        for(LocalDateTime date : dates){
                            ReminderThread reminderThread = new ReminderThread(this.bot, reminder, date);
                            
                            this.runningReminderThreads.get(key).get(reminder.reminderId).add(reminderThread);
                            
                            reminderThread.start();
                            
                        }
                        
                    }  
                }


                sleep(1000 * 3600);     //it launches the reminders each hour
            }while(true);
        }catch(Exception e){
            System.out.println("Problem while launching reminder threads");
            System.out.println(e.getMessage());
        }
        
    }

    public Map<Integer, ArrayList<ReminderThread>> getAllReminderThreads(String userId){
        if(!this.runningReminderThreads.containsKey(userId)) return null;

        return this.runningReminderThreads.get(userId);
    }

    public void addReminderThread(ReminderThread thread){
        Reminder reminder = thread.reminder;
        try{
            this.runningReminderThreads.get(reminder.userId).get(reminder.reminderId).add(thread);
            thread.start();
        }catch(Exception e){
            System.out.println("Strange behavior detected: how to add new reminder, if there werent any reminder threads in "
               + "the first time");
        }
    }

    public void tryAddReminderThread(Reminder reminder){
        
        ArrayList<LocalDateTime> dates = reminder.getDatesBefore(currentMaxTime);
        Collections.sort(dates, new Comparator<LocalDateTime>() {
            @Override
            public int compare(LocalDateTime dateTime1, LocalDateTime dateTime2){
                return dateTime1.compareTo(dateTime2);
            }
        }); 

        if(!this.runningReminderThreads.containsKey(reminder.userId)) 
            this.runningReminderThreads.put(reminder.userId, new HashMap<Integer, ArrayList<ReminderThread>>());
        if(!this.runningReminderThreads.get(reminder.userId).containsKey(reminder.reminderId)) 
            this.runningReminderThreads.get(reminder.userId).put(reminder.reminderId, new ArrayList<ReminderThread>());

        for(LocalDateTime date : dates){
            if(date.isBefore(currentMaxTime) && date.isAfter(LocalDateTime.now())){
                ReminderThread reminderThread = new ReminderThread(this.bot, reminder, date);
    
                this.runningReminderThreads.get(reminder.userId).get(reminder.reminderId).add(reminderThread);
                reminderThread.start();
            }
        }
    }

    public void tryChangeReminderThreads(Reminder reminder){
        ArrayList<LocalDateTime> dates = reminder.getDatesBefore(currentMaxTime);
        Collections.sort(dates, new Comparator<LocalDateTime>() {
            @Override
            public int compare(LocalDateTime dateTime1, LocalDateTime dateTime2){
                return dateTime1.compareTo(dateTime2);
            }
        }); 

        //setting the dates again
        if(!this.runningReminderThreads.containsKey(reminder.userId)) 
            this.runningReminderThreads.put(reminder.userId, new HashMap<Integer, ArrayList<ReminderThread>>());

        if(dates.size() == 0){
            this.runningReminderThreads.get(reminder.userId).remove(reminder.reminderId);
            return;
        }

        ArrayList<ReminderThread> threads = this.runningReminderThreads.get(reminder.userId).get(reminder.reminderId);
        if(threads != null)
            for(ReminderThread thread : threads) thread.interrupt();
        
        this.runningReminderThreads.get(reminder.userId).put(reminder.reminderId, new ArrayList<ReminderThread>());

        for(LocalDateTime date : dates){
            if(date.isBefore(currentMaxTime) && date.isAfter(LocalDateTime.now())){
                ReminderThread reminderThread = new ReminderThread(this.bot, reminder, date);
    
                this.runningReminderThreads.get(reminder.userId).get(reminder.reminderId).add(reminderThread);
                reminderThread.start();
            }
        }
    }

    public boolean removeReminderThread(Reminder reminder, int threadId){ 
        if(!this.runningReminderThreads.containsKey(reminder.userId)) return false;

        Map<Integer, ArrayList<ReminderThread>> userThreads = this.runningReminderThreads.get(reminder.userId);
        if(userThreads == null) return false;
        
        if(!userThreads.containsKey(reminder.reminderId)) return false;
        ArrayList<ReminderThread> reminderThreads = userThreads.get(reminder.reminderId);
        
        System.out.println(2);
        for(ReminderThread thread : reminderThreads){
            if(thread.threadId == threadId){
                thread.interrupt();
                reminderThreads.remove(thread);
                return true;
            }
        }

        return false;
    }

    public void removeReminderThreads(Reminder reminder){
        try{
            if(!this.runningReminderThreads.containsKey(reminder.userId)) return;
            if(!this.runningReminderThreads.get(reminder.userId).containsKey(reminder.reminderId)) return;

            ArrayList<ReminderThread> threads = this.runningReminderThreads.get(reminder.userId).get(reminder.reminderId);
            
            for(ReminderThread thread : threads){
                threads.remove(thread);
                thread.interrupt();
            }
            this.runningReminderThreads.get(reminder.userId).remove(reminder.reminderId);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void removeReminderThreads(String userId, Integer reminderId){

        if(!this.runningReminderThreads.containsKey(userId)) return;
        if(this.runningReminderThreads.get(userId) == null) return;
        if(!this.runningReminderThreads.get(userId).containsKey(reminderId)) return;

        ArrayList<ReminderThread> threads = this.runningReminderThreads.get(userId).get(reminderId);
        if(threads == null) return;

        for(ReminderThread thread : threads){
            threads.remove(thread);
            thread.interrupt();
        }

    }

    public void removeReminderThread(Reminder reminder, LocalDateTime date){
        try{
            ArrayList<ReminderThread> threads = this.runningReminderThreads.get(reminder.userId).get(reminder.reminderId);
            for(ReminderThread thread : threads){
                if(thread.date.isEqual(date)){
                    threads.remove(thread);
                    thread.interrupt();
                    
                    return;
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public ReminderThread getReminderThread(String userId, int reminderId, int threadId){
        try{
            ArrayList<ReminderThread> threads = this.runningReminderThreads.get(userId).get(reminderId);

            for(int i = 0; i < threads.size(); i++){
                if(threads.get(i).threadId == threadId) return threads.get(i);
            }

            return null;

        }catch(Exception exception){
            return null;
        }
    }

    public boolean removeReminderThread(String userId, int reminderId, int threadId){
        try{
            ArrayList<ReminderThread> threads = this.runningReminderThreads.get(userId).get(reminderId);
            ReminderThread thread = null;
            for(int i = 0; i < threads.size(); i++){
                if(threads.get(i).threadId == threadId) thread = threads.get(i);
            }

            threads.remove(thread);
            thread.interrupt();

            return true;

        }catch(Exception exception){
            return false;
        }
    }
}
