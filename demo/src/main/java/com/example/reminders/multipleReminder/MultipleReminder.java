package com.example.reminders.multipleReminder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;


import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.example.TelegramTestBot;
import com.example.User;
import com.example.help.CommandLineParser;
import com.example.keyboards.PrintKeyboard;
import com.example.reminders.Reminder;
import com.example.reminders.multipleReminder.messages.ChooseChangeMessage;
import com.example.reminders.multipleReminder.messages.ChooseDateMessage;
import com.example.reminders.multipleReminder.messages.AddNewDateMessage;
import com.example.threads.ChangingThread;
import com.example.threads.CreationThread;

public class MultipleReminder extends Reminder{
    public Map<Integer, LocalDateTime> dates = new HashMap<>();
    public int datesCount = 0;

    public boolean addDate(String date){
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
            this.dates.put(datesCount + 1, LocalDateTime.parse(date, formatter));
            datesCount++;
        }catch(Exception e){
            //if date is invalid
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public CreationThread generateCreationThread(TelegramTestBot bot, User user){
        return new CreationThread(bot, user){
            @Override
            public void run(){
                synchronized(this){
                    try{

                        //getting the task
                        bot.sendMsg(new SendMessage(this.user.id, "Type in the task you want to do"));
                        wait();
                        this.user.currentReminder.task = this.user.inputData;

                        //getting the time
                        
                        boolean exit = false;
                        do{
                            bot.sendMsg(new SendMessage(this.user.id, "Type in the time, when you want us to remind you to do the task"));
                            wait();
                            while(!((MultipleReminder)this.user.currentReminder).addDate(this.user.inputData)){
                                bot.sendMsg(new SendMessage(this.user.id, "Invalid input for data. Try format \"yyyy-MM-dd HH-mm\""));
                                wait();
                            }

                            bot.sendMsg(new AddNewDateMessage(this.user.id));
                            
                            do{
                                wait();
                                if(this.user.inputData.equals("/add_multiplereminder_finish")){
                                    exit = true;
                                    break;
                                }else if(this.user.inputData.equals("/add_multiplereminder_adddate")){
                                    break;
                                }

                            }while(true);
                        }while(!exit);

                        //in case of success
                        System.out.println("it reached db");
                        this.bot.db.addReminder(this.user.currentReminder);
                        this.bot.reminderControlThread.tryAddReminderThread(this.user.currentReminder);

                        bot.sendMsg(new SendMessage(this.user.id, "The creation process is finished, you can look for the reminder in reminder list!"));
                        
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
                
            }
        };
    }

    @Override
    public ChangingThread generateChangingThread(TelegramTestBot bot, User user) {
        return new ChangingThread(bot, user) {
            @Override
            public void run(){
                try{
                    
                    Map<Integer, LocalDateTime> dateMap = ((MultipleReminder)user.currentReminder).dates;
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

                            if(input.equals("/change_task")){
                                bot.sendMsg(new SendMessage(this.user.id, "Type in new task description"));
                                wait();
                                this.user.currentReminder.task = this.user.inputData;


                            }else if(input.equals("/change_dateadd")){
                                bot.sendMsg(new SendMessage(this.user.id, "Type in the new date"));
                                wait();
                                input = this.user.inputData;
                                
                                while(!((MultipleReminder)this.user.currentReminder).addDate(input)){
                                    bot.sendMsg(new SendMessage(this.user.id, "Invalid input for data. Try format \"yyyy-MM-dd HH-mm\""));
                                    wait();
                                    input = this.user.inputData;
                                }
                                System.out.println(((MultipleReminder)this.user.currentReminder).datesCount);
                                System.out.println(((MultipleReminder)this.user.currentReminder).dates.keySet());
                                
                                this.bot.reminderControlThread.tryChangeReminderThreads(this.user.currentReminder);
                                /**
                                 * there s bug - the cancel action does not return back previous dates, because i don`t have a proper function +
                                 * i dont return threads
                                 */

                            }else if(input.equals("/change_dateremove")){
                                //preparing map of dates
                                bot.sendMsg(new ChooseDateMessage(this.user.id, dateMap));
                            
                            }else if(input.startsWith("/change_dateremove?")){
                                HashMap<String, String> params = CommandLineParser.parseCommandString(input);
                                if(!params.containsKey("dateindex")){
                                    bot.sendMsg(new SendMessage(this.user.id, "Invalid command, try again"));
                                    continue;
                                }
                                int index;
                                try{
                                    index = Integer.parseInt(params.get("dateindex"));
                                }catch(Exception e){
                                    bot.sendMsg(new SendMessage(this.user.id, "Invalid command, try again"));
                                    continue;
                                }

                                dateMap.remove(index);

                                this.bot.reminderControlThread.tryChangeReminderThreads(this.user.currentReminder);

                                /**
                                 * there s bug - the cancel action does not return back previous dates, because i don`t have a proper function +
                                 * i dont return threads
                                 */
                                
                            }
                        }
                    }

                    bot.sendMsg(new SendMessage(this.user.id, "The changing process is finished, you can look for the reminder in reminder list!"));
                    this.user.currentReminder = null;
                    this.user.inputData = null;
                    this.user.status = User.UserStatus.DEFAULT;


                }catch(Exception e){
                    System.out.println("Problem in changing thread of multiple reminder");
                    System.out.println(e.getMessage());

                    bot.sendMsg(new SendMessage(this.user.id, "The changing process is failed or cancelled"));
                    this.user.currentReminder = null;
                    this.user.inputData = null;
                    this.user.status = User.UserStatus.DEFAULT;
                }
            }
        };
    }


    @Override
    public ArrayList<LocalDateTime> getDatesBefore(LocalDateTime finalDate) {
        
        ArrayList<LocalDateTime> result = new ArrayList<LocalDateTime>();
        
        for(LocalDateTime date : this.dates.values()){
            if(date.isBefore(finalDate)) result.add(date);
        }
        return result;
    }

    @Override
    public void printLongly(TelegramTestBot bot){

        String messageText = "Here is your multiple reminder:\n "
            + "Id: " + reminderId + " \n"
            + "Task: " + this.task + "\n"
            + "Times: \n";
        String datesString = "";
        for(int i : this.dates.keySet()){
            datesString += dates.get(i) + "\n";
        }
        messageText += datesString;

        SendMessage message = new SendMessage(this.userId, messageText);
        message.setReplyMarkup(new PrintKeyboard(this.reminderId));
        bot.sendMsg(message);

    }

    @Override
    public String toShortString() {
        String result = "On ";
        result += this.dates.size() == 0 ? "" : ( this.dates.values().toArray()[0].toString() + " and other ");
        result += (this.dates.size() == 0 ? 0 : this.dates.size() - 1) + " dates";
        result += " do: ";
        result += this.task.length() > 10 ? this.task.substring(0, 10) + "..." : this.task;

        return result;
    }

    @Override
    public boolean stopReminderThread(TelegramTestBot bot, User user, int reminderId, int threadId) {

        boolean isFinished = true;
        MultipleReminder reminder = (MultipleReminder)bot.db.getReminder(user.id, reminderId);

        LocalDateTime threadDate = bot.reminderControlThread.getReminderThread(user.id, reminderId, threadId).date;
        for(LocalDateTime date : reminder.dates.values()){
            System.out.println(threadDate.minusMinutes(5) + " is before " + date);
            if(threadDate.minusMinutes(5).isBefore(date)){
                isFinished = false;
                break;
            }
        }
        if(isFinished) bot.db.deleteReminder(user.id, reminderId);
        if(!bot.reminderControlThread.removeReminderThread(user.id, reminderId, threadId)) return false;

        return true;

    }

    @Override
    public int executeMySqlAddStatement(Connection connection) {
        PreparedStatement statement = null;
        int result = -1;
        int[] results = new int[dates.values().size()];
        try{
            statement = connection.prepareStatement("INSERT INTO multiplereminder VALUES(" + userId + ",?,?,?);");
            statement.setInt(1, reminderId);
            statement.setString(2, task);
            statement.setInt(3, datesCount);

            result = statement.executeUpdate();
            statement.close();

            PreparedStatement statementDates = connection.prepareStatement("INSERT INTO multiplereminder_date VALUES(" + userId + "," + reminderId + ", ?, ?);");
            
            for(int dateId : dates.keySet()){
                statementDates.setInt(1, dateId);
                statementDates.setTimestamp(2, Timestamp.valueOf(dates.get(dateId)));

                statementDates.addBatch();
            }

            results = statementDates.executeBatch();
            statementDates.close();


        }catch(Exception e){
            System.out.println("Insert into multiplereminder failed");
            System.out.println(e.getMessage());    
        }
        
        return Math.min(result, Arrays.stream(results).min().getAsInt());
    }

    @Override
    public int executeMySqlChangeStatement(Connection connection) {

        int result = -1;
        try{
            //changing task
            String reminderUpdateQuery = "UPDATE multiplereminder SET TASK=? WHERE user_id=" + userId + " AND REMINDER_ID=?;";
            PreparedStatement preparedStatement = connection.prepareStatement(reminderUpdateQuery);
            preparedStatement.setString(1, task);
            preparedStatement.setInt(2, reminderId);

            result = preparedStatement.executeUpdate();
            preparedStatement.close();

            //retrieving old dates
            Map<Integer, LocalDateTime> oldDates = new HashMap<>();

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM multiplereminder_date WHERE USER_ID=" + userId + 
                " AND REMINDER_iD=" + reminderId + ";");
            
            
            while(resultSet.next()){
                oldDates.put(resultSet.getInt("date_id"), resultSet.getTimestamp("date").toLocalDateTime());
            }

            statement.close();

            //deleting dates, that does not belong here anymore

            for(int dateId : oldDates.keySet()){
                if(!dates.containsKey(dateId)){
                    
                    PreparedStatement deleteStatement = 
                        connection.prepareStatement("DELETE FROM multiplereminder_date WHERE REMINDER_ID=" + reminderId + " AND USER_ID=" + userId 
                        + " AND DATE_ID=" + dateId + ";");
                    result = deleteStatement.executeUpdate();
                    if(result < 1) return result;
                    deleteStatement.close();
                }
            }

            //adding new dates, that aren`t in the database

            for(int dateId : dates.keySet()){
                if(!oldDates.containsKey(dateId)){

                    PreparedStatement insertStatement = 
                        connection.prepareStatement("INSERT INTO multiplereminder_date VALUES(" + userId + "," + reminderId + ", ?, ?);");
                    insertStatement.setInt(1, dateId);
                    insertStatement.setTimestamp(2, Timestamp.valueOf(dates.get(dateId)));

                    result = insertStatement.executeUpdate();
                    if(result < 1) return result;
                    insertStatement.close();

                }
            }


            //changing existing dates
            for(int dateId : dates.keySet()){
                if(!oldDates.containsKey(dateId)) continue;
                if(!dates.get(dateId).equals(oldDates.get(dateId))) continue;

                PreparedStatement updateStatement = connection.prepareStatement("UPDATE multiplereminder_date SET DATE=? WHERE USER_ID="
                    + this.userId + " AND REMINDER_ID=" + reminderId + " AND DATE_ID=?;");

                updateStatement.setInt(2, dateId);
                updateStatement.setTimestamp(1, Timestamp.valueOf(dates.get(dateId)));

                result = updateStatement.executeUpdate();
                if(result < 1) return result;
            }



        }catch(Exception e){
            System.out.println("Problem with updating multiple reminder");
            System.out.println(e.getMessage());
        }
        return result;
    }


    @Override 
    public int executeMySqlDeleteStatement(Connection connection, String userId, int reminderId){
        int result = -1;
        System.out.println("im here");
        try{
            PreparedStatement statement = connection.prepareStatement("DELETE FROM multiplereminder WHERE REMINDER_ID=" + reminderId + " AND USER_ID=" + userId + ";");
            result = statement.executeUpdate();
            statement.close();
            System.out.println("delte reminder result: " + result);
            
            //DELETING DATES

            Statement selectStatement = connection.createStatement();
            ResultSet resultSet = selectStatement.executeQuery("SELECT * FROM multiplereminder_date WHERE USER_ID=" + userId 
                + " AND REMINDER_ID=" + reminderId + ";");

            while(resultSet.next()){
                PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM multiplereminder_date WHERE USER_ID=" + userId + 
                    " AND REMINDER_ID=" + reminderId + " AND DATE_ID=" + resultSet.getInt("DATE_ID") + ";");
                result = Math.min(result, deleteStatement.executeUpdate());
                deleteStatement.close();
            }
            selectStatement.close();


        }catch(Exception e){
            System.out.println("Problem while deleting multiple reminder from mysql");
            System.out.println(e.getMessage());
        }
        
        return result;
    }

    @Override
    public Reminder executeMySqlGetStatement(Connection connection, String userId, int reminderId){
        
        try{
            //onetimereminder
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM multiplereminder WHERE REMINDER_ID=" + reminderId + " AND USER_ID=" + userId + ";");

            if(result.next()){

                MultipleReminder reminder = new MultipleReminder();
                reminder.userId = userId;
                reminder.reminderId = reminderId;
                reminder.datesCount = result.getInt("datescount");
                reminder.task = result.getString("task");

                statement.close();

                //getting dates

                Statement dateStatement = connection.createStatement();
                ResultSet datesResult = dateStatement.executeQuery("SELECT * FROM multiplereminder_date WHERE REMINDER_ID= "
                     + reminderId + " AND USER_ID=" + userId + ";");

                while(datesResult.next()){
                    reminder.dates.put(datesResult.getInt("DATE_ID"), datesResult.getTimestamp("DATE").toLocalDateTime());
                }

                dateStatement.close();


                return reminder;
            }

        }catch(Exception e){
            System.out.println("Problem with get multiple reminder sql statements");
            System.out.println(e.getMessage());
        }
        
        
        return null;
    }

    @Override
    public Map<String, Map<Integer, Reminder>> executeMySqlGetAllReminderStatement(Connection connection){
        Map<String, Map<Integer, Reminder>> result = new HashMap<>();

        try{
            //for onetimereminder:
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM multiplereminder;");
            
            while(resultSet.next()){
                MultipleReminder reminder = new MultipleReminder();
                reminder.reminderId = resultSet.getInt("reminder_id");
                reminder.userId = resultSet.getBigDecimal("user_id").toString();
                reminder.task = resultSet.getString("task");

                //adding dates
                Statement dateStatement = connection.createStatement();
                ResultSet dateResultSet = dateStatement.executeQuery("SELECT * FROM multiplereminder_date WHERE USER_ID=" + reminder.userId + 
                    " AND REMINDER_ID=" + reminder.reminderId + ";");

                while(dateResultSet.next()){
                    reminder.dates.put(dateResultSet.getInt("DATE_ID"), dateResultSet.getTimestamp("DATE").toLocalDateTime());
                }
                dateStatement.close();
   

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
            ResultSet resultSet = statement.executeQuery("SELECT * FROM multiplereminder WHERE USER_ID=" + userId + ";");

            while(resultSet.next()){
                MultipleReminder reminder = new MultipleReminder();
                reminder.userId = userId;
                reminder.reminderId = resultSet.getInt("reminder_id");
                reminder.task = resultSet.getString("task");
        
                //getting dates
                Statement datesStatement = connection.createStatement();
                ResultSet datesResutlSet = datesStatement.executeQuery("SELECT * FROM multiplereminder_date WHERE USER_ID=" + userId 
                    + " AND REMINDER_ID=" + reminder.reminderId + ";");

                while(datesResutlSet.next()){
                    reminder.dates.put(
                        datesResutlSet.getInt("date_id"),
                        datesResutlSet.getTimestamp("date").toLocalDateTime()
                    );
                }

                datesStatement.close();

                result.add(reminder);
            }

            statement.close();

        }catch(Exception e){
            System.out.println("Problem while getting reminder list of multiple reminders");
            System.out.println(e.getMessage());
        }

        return result;
    }

}
