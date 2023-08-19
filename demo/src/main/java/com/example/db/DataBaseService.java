package com.example.db;

import java.util.ArrayList;
import java.util.Map;

import com.example.User;
import com.example.reminders.Reminder;

public interface DataBaseService {

    

    public User getUser(String userId);
    public boolean deleteUser(String userId);
    public boolean addUser(User user);       //?

    public boolean addReminder(Reminder reminder);//?

    public boolean deleteReminder(Reminder reminder);
    public boolean deleteReminder(String userId, Integer reminderId);

    public boolean changeReminder(String userId, Integer reminderId, Reminder newReminder);

    public Reminder getReminder(String userId, Integer reminderId);
    public Map<String, Map<Integer, Reminder>> getAllReminders();

    public ArrayList<Reminder> getReminderList(String userId);
    public ArrayList<Reminder> getReminderList(String userId, int from, int to);

}
