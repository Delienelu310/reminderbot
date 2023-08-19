/**
 * Onetime package, that contains OneTimeReminder class and all small classes, that used only by it and created specially for him
 */
package com.example.reminders.OneTimeReminder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.TelegramTestBot;
import com.example.User;
import com.example.keyboards.PrintKeyboard;
import com.example.reminders.Reminder;
import com.example.reminders.OneTimeReminder.messages.ChooseChangeMessage;
import com.example.threads.ChangingThread;
import com.example.threads.CreationThread;

/**
 * 
 */
public class OneTimeReminder extends Reminder{
    
    /**
     * Point of time, when app should send user a reminder
     */
    public LocalDateTime date;

    /**
     * 
     * @param date
     * @return
     */
    public boolean setDate(String date){
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
            this.date = LocalDateTime.parse(date, formatter);

            //if before, then it cannot be like this
            if(this.date.isBefore(LocalDateTime.now())) return false;
        }catch(Exception e){
            //date string is not pasable into date
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<LocalDateTime> getDatesBefore(LocalDateTime finalDate){
        ArrayList<LocalDateTime> result = new ArrayList<LocalDateTime>();
        if(date.isBefore(finalDate)) result.add(date);

        return result;
    }

    @Override
    public String toShortString(){
        String result = "On ";
        result += this.date.toString();
        result += " do: ";
        result += this.task.length() > 10 ? this.task.substring(0, 10) + "..." : this.task;

        return result;
    }

    @Override
    public void printLongly(TelegramTestBot bot){

        String messageText = "Here is your onetime reminder:\n "
            + "Id: " + reminderId + " \n"
            + "Task: " + this.task + "\n"
            + "Time: " + this.date.toString();
        SendMessage message = new SendMessage(this.userId, messageText);
        message.setReplyMarkup(new PrintKeyboard(this.reminderId));
        bot.sendMsg(message);

    }

    @Override
    public int executeMySqlAddStatement(Connection connection){
        PreparedStatement statement = null;
        int result = -1;
        try{
            statement = connection.prepareStatement("INSERT INTO onetimereminder VALUES(?," + userId + ",?,?);");
            statement.setInt(1, reminderId);
            statement.setString(2, task);
            statement.setTimestamp(3, Timestamp.valueOf(date));

            result = statement.executeUpdate();
            statement.close();

        }catch(Exception e){
            System.out.println("Insert into onetimereminder failed");
            System.out.println(e.getMessage());    
        }
        return result;
        
    }

    @Override
    public int executeMySqlChangeStatement(Connection connection){
        String updateQuery = "UPDATE onetimereminder SET DATE=?, TASK=? WHERE user_id=" + userId + " AND REMINDER_ID=?";
        int result = -1;
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(date));
            preparedStatement.setString(2, task);
            preparedStatement.setInt(3, reminderId);

            result = preparedStatement.executeUpdate();
            preparedStatement.close();

        }catch(Exception e){
            System.out.println("Problem with updating reminder");
            System.out.println(e.getMessage());
        }
        return result;
    }

    @Override
    public int executeMySqlDeleteStatement(Connection connection, String userId, int reminderId){
        int result = -1;
        
        try{
            PreparedStatement statement = connection.prepareStatement("DELETE FROM onetimereminder WHERE REMINDER_ID=" + reminderId + " AND USER_ID=" + userId + ";");
            result = statement.executeUpdate();
            statement.close();
            

        }catch(Exception e){
            System.out.println("Problem while deleting onetime reminder from mysql");
            System.out.println(e.getMessage());
        }
        
        return result;
    }

    @Override
    public Reminder executeMySqlGetStatement(Connection connection, String userId, int reminderId){

        try{
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM onetimereminder WHERE REMINDER_ID=" + reminderId + " AND USER_ID=" + userId + ";");

            if(result.next()){

                OneTimeReminder reminder = new OneTimeReminder();
                reminder.userId = userId;
                reminder.reminderId = reminderId;
                reminder.task = result.getString("task");
                reminder.date = result.getTimestamp("date").toLocalDateTime();

                statement.close();
                return reminder;
            }

        }catch(Exception e){
            System.out.println("Problem with get onetime reminder sql statements");
            System.out.println(e.getMessage());
        }

        return null;
    };

    @Override
    public Map<String, Map<Integer, Reminder>> executeMySqlGetAllReminderStatement(Connection connection){
        Map<String, Map<Integer, Reminder>> result = new HashMap<>();

        try{
            //for onetimereminder:
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM onetimereminder;");
            
            while(resultSet.next()){
                OneTimeReminder reminder = new OneTimeReminder();
                reminder.reminderId = resultSet.getInt("reminder_id");
                reminder.userId = resultSet.getBigDecimal("user_id").toString();
                reminder.task = resultSet.getString("task");
                reminder.date = resultSet.getTimestamp("date").toLocalDateTime();

                if(!result.containsKey(reminder.userId) || result.get(reminder.userId) == null)
                    result.put(reminder.userId, new HashMap<Integer, Reminder>());
                result.get(reminder.userId).put(reminder.reminderId, reminder);

            }
            
            statement.close();
            
        }catch(Exception e){
            System.out.println("Problem while getting reminder list");
            System.out.println(e.getMessage());
        }

        return result;
        
    }

    @Override
    public  ArrayList<Reminder> executeMySqlGetReminderListStatement(Connection connection, String userId){
        ArrayList<Reminder> result = new ArrayList<>();

        try{
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM onetimereminder WHERE USER_ID=" + userId + ";");

            while(resultSet.next()){
                OneTimeReminder reminder = new OneTimeReminder();
                reminder.userId = userId;
                reminder.reminderId = resultSet.getInt("reminder_id");
                reminder.task = resultSet.getString("task");
                reminder.date = resultSet.getTimestamp("date").toLocalDateTime();

                result.add(reminder);
            }

            statement.close();

        }catch(Exception e){
            System.out.println("Problem while getting reminder list of onetime reminders");
            System.out.println(e.getMessage());
        }

        return result;
    }


    @Override
    public CreationThread generateCreationThread(TelegramTestBot bot, User user){
        return new CreationThread(bot, user){
            @Override
            public void run(){
                try{

                    //getting the task
                    bot.sendMsg(new SendMessage(this.user.id, "Type in the task you want to do"));
                    synchronized(this){
                        wait();
                        this.user.currentReminder.task = this.user.inputData;
                    }

                    //getting the time
                    bot.sendMsg(new SendMessage(this.user.id, "Type in the date"));
                    synchronized(this){
                        wait();
                    }
                    while(!((OneTimeReminder)this.user.currentReminder).setDate(this.user.inputData)){
                        bot.sendMsg(new SendMessage(this.user.id, "Invalid input for data. Try format \"yyyy-MM-dd HH-mm\""));
                        synchronized(this){
                            wait();
                        }
                    }
                    

                    //in case of success
                    this.bot.db.addReminder(this.user.currentReminder);
                    this.bot.reminderControlThread.tryAddReminderThread(this.user.currentReminder);

                    bot.sendMsg(new SendMessage(this.user.id, "The creation process is finished, you can look for the reminder in reminder list!"));
                    
                    //clean-up
                    this.user.currentReminder = null;
                    this.user.inputData = null;
                    this.user.status = User.UserStatus.DEFAULT;

                }catch(InterruptedException e){
                    bot.sendMsg(new SendMessage(this.user.id, "The creation process was cancelled"));
                    this.user.remindersCount--;
                    this.user.currentReminder = null;
                    this.user.inputData = null;
                    this.user.status = User.UserStatus.DEFAULT;
                }
            }
        };
    }

    @Override
    public ChangingThread generateChangingThread(TelegramTestBot bot, User user){
        return new ChangingThread(bot, user) {
            @Override
            public void run(){

                //use reflection on fields in order to simplify the process for all different reminders
                String initialTask = this.user.currentReminder.task;
                LocalDateTime initialDate = ((OneTimeReminder)this.user.currentReminder).date;

                try{
                    while(true){
                        SendMessage message = new ChooseChangeMessage(this.user.id);
                        bot.sendMsg(message);

                        synchronized(this){
                            wait();
                            String input = this.user.inputData;

                            if(input.equals("/finish")){
                                bot.db.changeReminder(this.user.id, reminderId, this.user.currentReminder);
                                bot.sendMsg(new SendMessage(this.user.id, "The changing thread is closed"));
                                break;
                            }

                            switch(input){
                                case "/change_task":
                                    bot.sendMsg(new SendMessage(this.user.id, "Type in new task description"));
                                    wait();
                                    this.user.currentReminder.task = this.user.inputData;
                                    break;
                                case "/change_date":
                                    bot.sendMsg(new SendMessage(this.user.id, "Type in the new date"));
                                    wait();
                                    input = this.user.inputData;
                                    
                                    while(!((OneTimeReminder)this.user.currentReminder).setDate(input)){
                                        bot.sendMsg(new SendMessage(this.user.id, "Invalid input for data. Try format \"yyyy-MM-dd HH-mm\""));
                                        synchronized(this){
                                            wait();
                                        }
                                        input = this.user.inputData;
                                    }
                                    
                                    this.bot.reminderControlThread.tryChangeReminderThreads(this.user.currentReminder);

                                    break;
                            }
                        }
                    }
                    

                    this.user.currentReminder = null;
                    this.user.inputData = null;
                    this.user.status = User.UserStatus.DEFAULT;

                }catch(InterruptedException e){
                    //when cancelled
                    bot.sendMsg(new SendMessage(this.user.id, "The changing process was cancelled, initial data is returned"));

                    this.user.currentReminder.task = initialTask;
                    ((OneTimeReminder) this.user.currentReminder).date = initialDate;

                    this.user.currentReminder = null;
                    this.user.inputData = null;
                    this.user.status = User.UserStatus.DEFAULT;
                }
            }
        };
    }

    @Override
    public boolean stopReminderThread(TelegramTestBot bot, User user, int reminderId, int threadId){
        
        if(!bot.reminderControlThread.removeReminderThread(user.id, reminderId, threadId)) return false;
        if(!bot.db.deleteReminder(userId, reminderId)) return false;
        
        bot.reminderControlThread.removeReminderThreads(userId, reminderId);
        
        return true;
    }
}
