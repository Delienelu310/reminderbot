/**
 * Reminder package contains reminder scheme and all types of reminders with used by them classes
 */
package com.example.reminders;

import java.util.ArrayList;
import java.util.Map;
import java.sql.Connection;
import java.time.LocalDateTime;

import com.example.ReminderControlThread;
import com.example.TelegramTestBot;
import com.example.User;
import com.example.keyboards.ReminderListKeyboard;
import com.example.threads.ChangingThread;
import com.example.threads.CreationThread;

/**
 * Reminder - abstract schema of all reminder classes
 * 
 * This classes implementing this schema are designed to hold intormation about when, what and to who remind
 * It has a wide variety of abstract methods, that are used in all parts of a program
 */
public abstract class Reminder {
    
    /**
     * The task, that the user wants to perform
     */
    public String task;

    /**
     * The user id, that is used to retirieve reminder data from the database and to difirintiate reminders of different users
     */
    public String userId;

    /**
     * The reminder id, that is used to retrieve reminder data from the database
     */
    public int reminderId;

    /**
     * The number of threads, that were created
     * Used in order to generate thread id for {@link ReminderThread}
     */
    public int threadCounter = 0;

    /**
     * Method that return all the dates, when reminder must perform its duty, that stay before certain date given as an argument
     * 
     * This method is user by ReminderControlThread {@link ReminderControlThread}. 
     * The thread takes points in time and sets a reminder thread, that send reminding message 
     * to the user using information from the reminder
     * 
     * @param finalDate upper bound of returned points in time
     * @return points in time before "final date", when reminder was planned to perform its duty
     * 
     * @see ReminderControlThread
     */
    abstract public ArrayList<LocalDateTime> getDatesBefore(LocalDateTime finalDate);


    /**
     * Method, that uses data from the reminder and adds a reminder entity into the MySql database
     * 
     * It is used by {@link CreationThread} and {@link SQLDataBase} when user adds the reminder
     * 
     * @param connection connection between the server and the MySql database
     * @return indicator, that tells, whether MySql operation was successfull
     * 
     * @see SQLDataBase
     */
    public abstract int executeMySqlAddStatement(Connection connection);

    /**
     * Method, that uses data from the Reminder to change reminder entity in the MySql database
     * 
     * It is used by {@link ChangingThread} and {@link SQLDataBase} when user changes the reminder
     * 
     * @param connection connection between the server and the MySql database
     * @return indicator, that tells, whether MySql operation was successful
     * 
     * @see SQLDataBase
     */
    public abstract int executeMySqlChangeStatement(Connection connection);

    /**
     * Method, that uses data from the Reminder to change reminder entity in the MySql database
     * it uses {@link Reminder.reminderId} and {@link Reminder.userId} to delete reminder from the database
     * 
     * It is used by {@link SQLDataBase} when user deletes the reminder
     * 
     * @param connection connection between the server and the MySql database
     * @return indicator, that tells, whether MySql operation was successful
     * 
     * @see SQLDataBase
     */
    public abstract int executeMySqlDeleteStatement(Connection connection, String userId, int reminderId);

    /**
     * Method, that uses data indexes from arguments to retrieve reminder data from the MySql database
     * 
     * it uses {@link Reminder.reminderId} and {@link Reminder.userId} for this
     * It is used by  {@link SQLDataBase} when user gets the reminder by tapping the button on reminder list
     * 
     * @param connection connection between the server and the MySql database
     * @param userId id of the user, the reminder belongs to
     * @param reminderId id of the reminder
     * @return {@link Reminder} object, that is used later to send information to the user and prepare reminderThreads in needed
     * 
     * @see SQLDataBase
     */
    public abstract Reminder executeMySqlGetStatement(Connection connection, String userId, int reminderId);

    /**
     * Method, that retrieves all the reminder of the current reminder type from the mysql database
     * 
     * It is used by {@link ReminderControlThread} in order to generate {@link ReminderThread} if needed
     * 
     * @param connection connection between application and mysql database
     * @return map of all reminders, that are sorted by userId and reminderId
     * 
     * @see SQLDataBase
     * @see ReminderControlThread
     * @see ReminderThread
     */
    public abstract Map<String, Map<Integer, Reminder>> executeMySqlGetAllReminderStatement(Connection connection);

    /**
     * Method, that retrieves all the reminders of the current reminder type of user with userid from argument list from the mysql database
     * 
     * Is it used by {@link SqlDataBase} when user asks for reminder list
     * 
     * @param connection connection between application and mysql database
     * @param userId id of the user, that we get reminder from
     * @return list of all reminder of current type of user with userId given in as argument
     * 
     * @see SQLDataBase
     */
    public abstract ArrayList<Reminder> executeMySqlGetReminderListStatement(Connection connection, String userId);

    /**
     * The method, that generates creation thread {@link CreationThread} for this exact reminder
     * 
     * It is implementing the run() method {@link CreationThread} and by this, controls how the user interacts with 
     * the bot
     * 
     * This creation thread is used by bot in order to get talk to the user and get its input, validate it,
     * process, write it down into the reminder, the method is called on 
     * 
     * @param bot telegram bot, with which we work
     * @param user user, which creates the reminder right now
     * @return the creation thread
     * 
     * @see CreationThread
     */
    public abstract CreationThread generateCreationThread(TelegramTestBot bot, User user);

    /**
     * The method, that generates thread extending changing thread {@link ChangingThread} for this exact reminder
     * 
     * It is implementing the run() method {@link ChangingThread.run} and by this, controls how the user interacts with 
     * the bot
     * 
     * This changing thread is used by bot in order to get talk to the user and get its input, validate it,
     * process, write it down into the reminder, the method is used on
     * 
     * @param bot telegram bot, with which we work
     * @param user user, which changes the reminder right now
     * @return the changing thread
     * 
     * @see ChangingThread
     */
    public abstract ChangingThread generateChangingThread(TelegramTestBot bot, User user);

    /**
     * Method, that stops reminder Thread {@link ReminderThread} when user deletes the reminder or it is out of date
     * Used by {@link ReminderControlThread} 
     * 
     * @param bot bot, which user interacts with
     * @param user user, interacts with the bot
     * @param reminderId id of the reminder, that is used to get reminderThread from the {@link ReminderControlThread}
     * @param threadId id of the reminder thread, that is used to get reminderThread from the {@link ReminderControlThread}
     * @return boolean, that tells, whether the thread was deleted or not
     */
    public abstract boolean stopReminderThread(TelegramTestBot bot, User user, int reminderId, int threadId);


    /**
     * Method that transforms the data, that is hold in the reminder, into a string record 
     * 
     * The method is used by the {@link ReminderListMessage} and {@link ReminderListKeyboard} in order to set text on
     * reminder list buttons
     * 
     * @param bot bot, the user interacts with
     * @return string record of the data from reminder
     * 
     * @see ReminderListMessage
     * @see ReminderListKeyboard
     */
    public abstract String toShortString();

    /**
     * Method that transform reminder data into a detailed string record and send it to the user
     * 
     * The method is used in {@link TelegramTestBot} by {@link TelegramTestBot.onUpdate} method when user chooses 
     * reminder from reminder list
     * 
     * @param bot the bot, which user interacts with
     * @return the detailed string recrod of the String
     * 
     *
     */
    public abstract void printLongly(TelegramTestBot bot);

    
}
