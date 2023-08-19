package com.example.db;

import java.sql.Statement;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.example.User;
import com.example.reminders.Reminder;
import com.example.reminders.OneTimeReminder.OneTimeReminder;
import com.example.reminders.multipleReminder.MultipleReminder;
import com.example.reminders.weekReminder.WeekReminder;

public class SQLDataBase implements DataBaseService {

    public Connection connection;
    
    private String databaseUrl;
    private String databasePassword;
    private String databaseUser;

    public SQLDataBase(){
        try{

            //getting config data
            Properties prop = new Properties();
            InputStream input = new FileInputStream("config.properties");
            prop.load(input);

            this.databaseUrl = prop.getProperty("db.mysql.url");
            this.databaseUser = prop.getProperty("db.mysql.user");
            this.databasePassword = prop.getProperty("db.mysql.password");
            

            System.out.println("Mysql url: " + this.databaseUrl);
            System.out.println("Mysql user: " + this.databaseUser);
            System.out.println("Mysql password: " + this.databasePassword);
        
            //making connection
            connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePassword);
        }catch(Exception e){
            System.out.println("Problem with connecting to the database");
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean addUser(User user){

        try{
            PreparedStatement statement = connection.prepareStatement("INSERT INTO USERS VALUES(" + user.id + ", ?, 0);");
            statement.setString(1, user.username);
            statement.executeUpdate();
            statement.close();
        
            return true;
        }catch(Exception e){
            System.out.println("Problem with addding user");
            System.out.println(e.getMessage());
        }
        
        return false;
    }

    @Override
    public User getUser(String userId){
        try{
            Statement statement = connection.createStatement();

            ResultSet result =  statement.executeQuery("SELECT * FROM USERS WHERE ID=" + userId + ";");
        
            if(!result.next()) return null;
            String username = result.getString("username");
            String id = result.getString("id");
            int remindersCount = result.getInt("reminders_count");

            return new User(id, username, remindersCount);
        
        }catch(Exception e){
            System.out.println("Problem with selecting user");
            System.out.println(e.getMessage());
        }
        
        return null;
    }



    @Override
    public boolean deleteUser(String userId){

        try{
            PreparedStatement statement = connection.prepareStatement("DELETE FROM USERS WHERE ID=" + userId + ";");
    
            int rowsAffected = statement.executeUpdate();

            statement.close();

            if(rowsAffected < 1) return false;
            return true;
        }catch(Exception e){
            System.out.println("Problem with deleting user");
            System.out.println(e.getMessage());
        }

        return false;
    }

    

    @Override
    public boolean addReminder(Reminder reminder){

        try{

            int result = reminder.executeMySqlAddStatement(connection);
            if(result < 1) return false;

            PreparedStatement statement2 = connection.prepareStatement("UPDATE USERS SET REMINDERS_COUNT=" + reminder.reminderId + " WHERE ID=" + reminder.userId);
            result = statement2.executeUpdate();
            statement2.close();

            if(result < 0) return false;

            return true;
        }catch(Exception e){
            System.out.println("Problem with inserting reminder");
            System.out.println(e.getMessage());
        }

        return false;
    }

    @Override
    public boolean deleteReminder(Reminder reminder){
        String userId = reminder.userId;
        int reminderId = reminder.reminderId;

        return deleteReminder(userId, reminderId);
    }

    @Override
    public boolean deleteReminder(String userId, Integer reminderId){

        int result = new OneTimeReminder().executeMySqlDeleteStatement(connection, userId, reminderId);
        if(result > 0) return true;

        result = new MultipleReminder().executeMySqlDeleteStatement(connection, userId, reminderId);
        if(result > 0) return true;

        result = new WeekReminder().executeMySqlDeleteStatement(connection, userId, reminderId);
        if(result > 0) return true;


        return result > 0;
    }

    @Override
    public boolean changeReminder(String userId, Integer reminderId, Reminder newReminder){

            int result = newReminder.executeMySqlChangeStatement(connection);
            return result > 0;

    }

    @Override
    public Reminder getReminder(String userId, Integer reminderId){

        Reminder reminder = null;

        reminder = new OneTimeReminder().executeMySqlGetStatement(connection, userId, reminderId);
        if(reminder != null) return reminder;

        reminder = new MultipleReminder().executeMySqlGetStatement(connection, userId, reminderId);
        if(reminder != null) return reminder;

        reminder = new WeekReminder().executeMySqlGetStatement(connection, userId, reminderId);
        if(reminder != null) return reminder;
        
        return null;
    }

    @Override
    public Map<String, Map<Integer, Reminder>> getAllReminders(){
        Map<String, Map<Integer, Reminder>> resultMap = new HashMap<>();


        //onetimereminder
        Map<String, Map<Integer, Reminder>> onetimeMap = new OneTimeReminder().executeMySqlGetAllReminderStatement(connection);
        for(String userId : onetimeMap.keySet()){
            for(int reminderId : onetimeMap.get(userId).keySet()){
                if(!resultMap.containsKey(userId)) resultMap.put(userId, new HashMap<Integer, Reminder>());
                resultMap.get(userId).put(reminderId, onetimeMap.get(userId).get(reminderId));
            }
        }

        //multiple reminder
        Map<String, Map<Integer, Reminder>> multipleMap = new MultipleReminder().executeMySqlGetAllReminderStatement(connection);
        for(String userId : multipleMap.keySet()){
            for(int reminderId : multipleMap.get(userId).keySet()){
                if(!resultMap.containsKey(userId)) resultMap.put(userId, new HashMap<Integer, Reminder>());
                resultMap.get(userId).put(reminderId, multipleMap.get(userId).get(reminderId));
            }
        }

        //weekly reminder
        Map<String, Map<Integer, Reminder>> weeklyMap = new WeekReminder().executeMySqlGetAllReminderStatement(connection);
        for(String userId : weeklyMap.keySet()){
            for(int reminderId : weeklyMap.get(userId).keySet()){
                if(!resultMap.containsKey(userId)) resultMap.put(userId, new HashMap<Integer, Reminder>());
                resultMap.get(userId).put(reminderId, weeklyMap.get(userId).get(reminderId));
            }
        }

        //other reminders


        return resultMap;
    }

    @Override
    public ArrayList<Reminder> getReminderList(String userId){
        ArrayList<Reminder> resultList = new ArrayList<>();
        
        resultList.addAll(new OneTimeReminder().executeMySqlGetReminderListStatement(connection, userId));
        resultList.addAll(new MultipleReminder().executeMySqlGetReminderListStatement(connection, userId));
        resultList.addAll(new WeekReminder().executeMySqlGetReminderListStatement(connection, userId));
        System.out.println(resultList);

        return resultList;
    }

    @Override
    public ArrayList<Reminder> getReminderList(String userId, int from, int to){
        ArrayList<Reminder> reminderList = getReminderList(userId);
        ArrayList<Reminder> resultList = new ArrayList<>();
        
        for(int i = from; i < to && i < reminderList.size(); i++){
            resultList.add(reminderList.get(i));
        }
        System.out.println(resultList);
        if(resultList.size() == 0) return null;
        return resultList;
    }

}
