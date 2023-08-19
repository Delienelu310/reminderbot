package com.example.reminders.weekReminder;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.TelegramTestBot;
import com.example.User;
import com.example.help.CommandLineParser;
import com.example.keyboards.PrintKeyboard;
import com.example.reminders.Reminder;
import com.example.reminders.weekReminder.messages.ChangeMessage;
import com.example.reminders.weekReminder.messages.ChooseWeekdaysMessage;
import com.example.threads.ChangingThread;
import com.example.threads.CreationThread;

public class WeekReminder extends Reminder{

    public boolean[] weekDaysIncluded = new boolean[7];
    private LocalTime time;

    public LocalTime getTime(){
        return this.time;
    }

    public void setTime(LocalTime time){
        this.time = time;
    }

    public boolean setTime(String timeString){
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH-mm");
            this.time = LocalTime.parse(timeString, formatter);

        }catch(Exception e){
            return false;
        }
        return true;
    }



    @Override
    public ArrayList<LocalDateTime> getDatesBefore(LocalDateTime finalDate) {
        //we only make form today`s date
        ArrayList<LocalDateTime> result = new ArrayList<>();

        LocalDateTime date = LocalDateTime.now()
            .minusNanos(LocalTime.now().toNanoOfDay())
            .plusNanos(time.toNanoOfDay());
        
        while(date.isBefore(finalDate)){
            if(weekDaysIncluded[date.getDayOfWeek().getValue()-1])
                result.add(date);
            date = date.plusDays(1);
        }

        return result;
    }

    @Override
    public int executeMySqlAddStatement(Connection connection) {
        int result;
        
        try{
            PreparedStatement statement = connection.prepareStatement("INSERT INTO weekreminder VALUES(" + userId + ", ?,?,?," + 
                "?,?,?,?,?, ?, ?);");
            statement.setInt(1, reminderId);
            statement.setString(2, task);
            statement.setTime(3, Time.valueOf(time));
            for(int i = 0; i < 7; i++){
                statement.setBoolean(i + 4, weekDaysIncluded[i]);
            }

            result = statement.executeUpdate();
            statement.close();

            return result;
        }catch(Exception e){
            System.out.println("Problem while adding week reminder into mysql db");
            System.out.println(e.getMessage());
            return -1;
        }

    }

    @Override
    public int executeMySqlChangeStatement(Connection connection) {
        int result;
        try{

            String updateStatementString = "UPDATE weekreminder SET TASK=?, TIME=?";
            for(int i = 0; i < 7; i++){
                updateStatementString += ", " + DayOfWeek.of(i + 1).toString() + "=?";
            }
            updateStatementString += " WHERE USER_ID=" + userId + " AND REMINDER_ID=" + reminderId + ";";

            PreparedStatement statement = connection.prepareStatement(updateStatementString);

            statement.setString(1, task);
            statement.setTime(2, Time.valueOf(time));
            for(int i = 0; i < 7; i++){
                statement.setBoolean(i + 3, weekDaysIncluded[i]);
            }

            result = statement.executeUpdate();
            statement.close();

            System.out.println(result);

            return result;

        }catch(Exception e){
            System.out.println("Error while updating week reminder in mysql db");
            System.out.println(e.getMessage());
            return -1;
        }
    }

    @Override
    public int executeMySqlDeleteStatement(Connection connection, String userId, int reminderId) {
        int result;

        try{
            PreparedStatement statement = connection.prepareStatement("DELETE FROM weekreminder WHERE USER_ID=" + userId + 
                " AND REMINDER_ID=" + reminderId + ";");

            result = statement.executeUpdate();
            statement.close();

            return result;
        }catch(Exception e){
            System.out.println("Erorr while deleting week reminder from mysql db");
            System.out.println(e.getMessage());

            return -1;
        }
    }

    @Override
    public Reminder executeMySqlGetStatement(Connection connection, String userId, int reminderId) {
        
        try{

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM weekreminder WHERE USER_ID=" + userId 
                + " AND REMINDER_ID=" + reminderId + ";");
            
            if(!resultSet.next()) return null;

            WeekReminder result = new WeekReminder();

            result.userId = userId;
            result.reminderId = reminderId;
            result.task = resultSet.getString("task");
            result.setTime(resultSet.getTime("time").toLocalTime());

            for(int i = 0; i < 7; i++){
                result.weekDaysIncluded[i] = resultSet.getBoolean(i + 5);
            }

            statement.close();

            return result;  

        }catch(Exception e){
            
            System.out.println("Error while selecting week reminder in mysql db");
            System.out.println(e.getMessage());
            
            return null;
        }
    }

    @Override
    public Map<String, Map<Integer, Reminder>> executeMySqlGetAllReminderStatement(Connection connection) {
        Map<String, Map<Integer, Reminder>> result = new HashMap<>();

        try{

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM weekreminder");

            while(resultSet.next()){
                String userId = Integer.toString(resultSet.getInt("user_id"));
                if(!result.containsKey(userId)) result.put(userId, new HashMap<Integer, Reminder>());

                WeekReminder reminder = new WeekReminder();
                reminder.userId = userId;
                reminder.reminderId = resultSet.getInt("reminder_id");
                reminder.task = resultSet.getString("task");
                reminder.setTime(resultSet.getTime("time").toLocalTime());

                for(int i = 0; i < 7; i++){
                    reminder.weekDaysIncluded[i] = resultSet.getBoolean(i + 5);
                }

                result.get(userId).put(reminder.reminderId, reminder);

            }

        }catch(Exception e){
            System.out.println("Error while getting all week reminders form mysql db");
            System.out.println(e.getMessage());
        }

        return result;
    }

    @Override
    public ArrayList<Reminder> executeMySqlGetReminderListStatement(Connection connection, String userId) {
        
        ArrayList<Reminder> result = new ArrayList<>();
        try{

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM weekreminder WHERE USER_ID=" + userId + ";");

            while(resultSet.next()){
                WeekReminder reminder = new WeekReminder();
                reminder.userId = userId;
                reminder.reminderId = resultSet.getInt("reminder_id");
                reminder.task = resultSet.getString("task");
                reminder.setTime(resultSet.getTime("time").toLocalTime());

                for(int i = 0; i < 7; i++){
                    reminder.weekDaysIncluded[i] = resultSet.getBoolean(i + 5);
                }

                result.add(reminder);
            }


        }catch(Exception e){
            System.out.println("Error while getting week reminder list from mysql db");
            System.out.println(e.getMessage());

        }

        return result;
    }

    @Override
    public CreationThread generateCreationThread(TelegramTestBot bot, User user) {
        
        return new CreationThread(bot, user) {
            @Override
            public void run(){
                try{
                    synchronized(this){
                        bot.sendMsg(new SendMessage(user.id, "Type in the task"));
                        wait();
                        while(user.inputData.equals("")){
                            bot.sendMsg(new SendMessage(user.id, "Task can`t be empty, try again"));
                        }
                        user.currentReminder.task = user.inputData;

                        bot.sendMsg(new SendMessage(user.id, "Which time of the day you want to get a reminder? Use format hh-mm"));
                        wait();
                        while(!((WeekReminder)user.currentReminder).setTime(user.inputData)){
                            bot.sendMsg(new SendMessage(user.id,"Invalid input, try format hh-mm"));
                            wait();
                        }

                        bot.sendMsg(new ChooseWeekdaysMessage(user.id));
                        while(true){

                            wait();

                            if(user.inputData.equals("/finish")){                
                                break;
                            }else if(!user.inputData.startsWith("/week_reminder_toggle?")){
                                continue;
                            }

                            System.out.println(11);
                            Map<String, String> params = CommandLineParser.parseCommandString(user.inputData);
                            if(!params.containsKey("day")){
                                continue;
                            }

                            int day;
                            try{
                                day = Integer.parseInt(params.get("day"));
                            }catch(Exception e){
                                System.out.println("invalid parameter");
                                System.out.println(e.getMessage());
                                continue;
                            }

                            boolean oldValue = ((WeekReminder)user.currentReminder).weekDaysIncluded[day - 1];
                            ((WeekReminder)user.currentReminder).weekDaysIncluded[day - 1] = !oldValue;
                            
                            bot.sendMsg(new SendMessage(user.id, oldValue ? 
                                "Reminder does not work at " + DayOfWeek.of(day).toString() :
                                "Now reminder works at " + DayOfWeek.of(day).toString())
                            );
                        }
                        System.out.println(10);
                        bot.db.addReminder(user.currentReminder);
                        bot.reminderControlThread.tryAddReminderThread(user.currentReminder);
                        bot.sendMsg(new SendMessage(this.user.id, "The creation process was finished successfully"));
                        
                    }    

                }catch(Exception e){
                    System.out.println("Error in creationg thread");
                    System.out.println(e.getMessage());
                    bot.sendMsg(new SendMessage(this.user.id, "The creation process was cancelled"));

                    this.user.remindersCount--;

                }finally{
                    this.user.currentReminder = null;
                    this.user.inputData = null;
                    this.user.status = User.UserStatus.DEFAULT;
                }
            }
        };
        
        
    }

    @Override
    public ChangingThread generateChangingThread(TelegramTestBot bot, User user) {
        return new ChangingThread(bot, user){
            @Override
            public void run(){
                try{
                    synchronized(this){
                        bot.sendMsg(new ChangeMessage(user.id));
                        wait();
                        while(true){
                            
                            if(user.inputData.equals("/finish")){
                                break;
                            }else if(user.inputData.equals("/change_weekreminder_task")){
                                bot.sendMsg(new SendMessage(user.id, "Type your new task description"));
                                wait();
                                while(user.inputData.equals("")){
                                    bot.sendMsg(new SendMessage(user.id,"Task description can`t be empty"));
                                    wait();
                                }
                                user.currentReminder.task = user.inputData;
                                bot.sendMsg(new SendMessage(user.id, "The new task description was set"));
                            }else if(user.inputData.equals("/change_weekreminder_time")){
                                bot.sendMsg(new SendMessage(user.id, "Which time do you want to get the message at? Type in hh-mm"));
                                wait();
                                while(!((WeekReminder)user.currentReminder).setTime(user.inputData)){
                                    bot.sendMsg(new SendMessage(user.id,"Invalid format, try again in format hh-mm"));
                                    wait();
                                }
                                bot.sendMsg(new SendMessage(user.id, "The new time was set"));
                    
                            }else if(user.inputData.equals("/week_reminder_toggle")){
                                bot.sendMsg(new ChooseWeekdaysMessage(user.id));
                            }else if(user.inputData.startsWith("/week_reminder_toggle?")){ 
                                Map<String, String> params = CommandLineParser.parseCommandString(user.inputData);
                                if(!params.containsKey("day")) continue;
                                int day;
                                try{
                                    day = Integer.parseInt(params.get("day"));
                                }catch(Exception e){
                                    System.out.println("invalid parameter");
                                    System.out.println(e.getMessage());
                                    continue;
                                }   

                                boolean oldValue = ((WeekReminder)user.currentReminder).weekDaysIncluded[day-1];
                                ((WeekReminder)user.currentReminder).weekDaysIncluded[day-1] = !oldValue;

                                bot.sendMsg(new SendMessage(user.id, oldValue ? 
                                    "The reminder does not work at " + DayOfWeek.of(day).toString() + "anymore" :
                                    "Now the reminder works at " + DayOfWeek.of(day).toString()
                                ));

                            }
                            wait();
                        }

                        bot.db.changeReminder(user.id, user.currentReminder.reminderId, user.currentReminder);
                        bot.reminderControlThread.tryChangeReminderThreads(user.currentReminder);
                        bot.sendMsg(new SendMessage(user.id, "The changing process was finished successfully"));
                    }
                }catch(Exception e){
                    System.out.println("Problem in changing thread of week reminder");
                    System.out.println(e.getMessage());

                    bot.sendMsg(new SendMessage(user.id, "The changing process was cancelled"));
                }finally{
                    this.user.currentReminder = null;
                    this.user.inputData = null;
                    this.user.status = User.UserStatus.DEFAULT;
                }
            }
        };
    }

    @Override
    public boolean stopReminderThread(TelegramTestBot bot, User user, int reminderId, int threadId) {

        if(!bot.reminderControlThread.removeReminderThread(user.id, reminderId, threadId)) return false;
        return true;
    }

    @Override
    public String toShortString() {

        String result = "To do: " + (this.task.length() < 10 ? this.task : this.task.substring(0, 10)) + " ";
        
        int weekDaysCount = 0;
        for(boolean a : weekDaysIncluded){
            if(a) weekDaysCount++;
        }
        result += weekDaysCount + " days a week at " + this.time;

        return result;
    }

    @Override
    public void printLongly(TelegramTestBot bot) {
        String messageText = "Here is your week reminder:\n "
            + "Id: " + reminderId + " \n"
            + "Task: " + this.task + "\n"
            + "Time: " + this.time + "\n"
            + "Days: \n";


        for(int i = 0; i < 7; i++){
            if(weekDaysIncluded[i]){
                messageText += DayOfWeek.of(i+1).toString() + "\n";
            }
        }

        SendMessage message = new SendMessage(this.userId, messageText);
        message.setReplyMarkup(new PrintKeyboard(this.reminderId));
        bot.sendMsg(message);
    }
    


}
