package com.example.db;

import java.util.Map;

import com.example.User;
import com.example.reminders.Reminder;

import java.util.ArrayList;
import java.util.HashMap;

public class ImprovisedDB implements DataBaseService{
    
    private Map<String, User> users = new HashMap<>();
    private Map<String, Map<Integer, Reminder>> reminders = new HashMap<>();

    @Override
    public Map<String, Map<Integer, Reminder>> getAllReminders(){
        return this.reminders;
    }

    @Override
    public User getUser(String id){
        if( ! users.containsKey(id)) return null; 
        
        return users.get(id);
    };

    @Override
    public boolean addUser(User user){

        if(users.containsKey(user.id)) return false;

        users.put(user.id, user);
        return true;
    }

    @Override
    public boolean deleteUser(String id){
        if(! users.containsKey(id)) return false;
    
        users.remove(id);
        if(this.reminders.containsKey(id)) this.reminders.remove(id);

        return true;
    }

    @Override
    public boolean addReminder(Reminder reminder) {
        if( !this.reminders.containsKey(reminder.userId)) this.reminders.put(reminder.userId, new HashMap<Integer, Reminder>());
        if( this.reminders.get(reminder.userId).containsKey(reminder.reminderId) ) return false;

        this.reminders.get(reminder.userId).put(reminder.reminderId, reminder);
        
        return true;
    }

    @Override
    public boolean deleteReminder(Reminder reminder) {
        if( !this.reminders.containsKey(reminder.userId)) return false;
        if( !this.reminders.get(reminder.userId).containsKey(reminder.reminderId) ) return false;

        this.reminders.get(reminder.userId).remove(reminder.reminderId);
        if(this.reminders.get(reminder.userId).isEmpty()) this.reminders.remove(reminder.userId);

        return true;
    }

    @Override
    public boolean deleteReminder(String userId, Integer reminderId) {
        if( !this.reminders.containsKey(userId)) return false;
        if( !this.reminders.get(userId).containsKey(reminderId) ) return false;

        this.reminders.get(userId).remove(reminderId);
        if(this.reminders.get(userId).isEmpty()) this.reminders.remove(userId);

        return true;
    }

    @Override
    public boolean changeReminder(String userId, Integer reminderId, Reminder newReminder) {
        if( !this.reminders.containsKey(userId)) return false;
        if( !this.reminders.get(userId).containsKey(reminderId) ) return false;

        this.reminders.get(userId).remove(reminderId);
        this.reminders.get(userId).put(reminderId, newReminder);

        return true;
    }

    @Override
    public Reminder getReminder(String userId, Integer reminderId) {
        if( !this.reminders.containsKey(userId)) return null;
        if( !this.reminders.get(userId).containsKey(reminderId) ) return null;

        return this.reminders.get(userId).get(reminderId);
    }

    @Override
    public ArrayList<Reminder> getReminderList(String userId) {
        ArrayList<Reminder> list = new ArrayList<Reminder>();
        Map<Integer, Reminder> userMap = this.reminders.get(userId);
        if(userMap == null) return list;
        
        for(Reminder reminder : userMap.values()){
            list.add(reminder);
        }

        return list;
    }

    @Override
    public ArrayList<Reminder> getReminderList(String userId, int from, int to) {

        ArrayList<Reminder> list = new ArrayList<Reminder>();
        ArrayList<Reminder> returnList = new ArrayList<Reminder>();

        Map<Integer, Reminder> userMap = this.reminders.get(userId);
        if(userMap == null) return returnList;

        int size = userMap.size();
        if(from >= size) return null;

        
        for(Reminder reminder : userMap.values()){
            list.add(reminder);
        }


        for(int i = from; i < to && i < size; i++){
            returnList.add(list.get(i));
        }

        return returnList;
    }

}
