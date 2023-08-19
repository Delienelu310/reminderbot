package com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.example.db.DataBaseService;
import com.example.db.SQLDataBase;
import com.example.help.CommandLineParser;

import com.example.keyboards.MainKeyboard;
import com.example.keyboards.ReminderListKeyboard;
import com.example.messages.AddReminderMessage;
import com.example.messages.HelpMessage;
import com.example.messages.ReminderFailMessage;
import com.example.messages.ReminderSuccessMessage;
import com.example.messages.StartMessage;

import com.example.reminders.Reminder;
import com.example.reminders.OneTimeReminder.OneTimeReminder;
import com.example.reminders.multipleReminder.MultipleReminder;
import com.example.reminders.weekReminder.WeekReminder;
import com.example.threads.PostponeReminderThread;
import com.example.threads.ReminderThread;

public class TelegramTestBot extends TelegramLongPollingBot
{

    public DataBaseService db;
    public ReminderControlThread reminderControlThread = new ReminderControlThread(this);

    private String TOKEN; 
    private String BOT_USERNAME;

    private Map<String, User> usersCacheMap = new HashMap<String, User>();

    public TelegramTestBot(DataBaseService db){

        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            prop.load(input);
            this.TOKEN = prop.getProperty("bot.token");
            this.BOT_USERNAME = prop.getProperty("bot.username");

            System.out.println("API Key: " + this.TOKEN);
            System.out.println("Database URL: " + this.BOT_USERNAME);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        this.db = db;
        reminderControlThread.start();
    }

    public void sendMsg(SendMessage msg){
        try{
            execute(msg);
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.out.println("error while sending message");
        }
    };

    @Override
    public void onUpdateReceived(Update update) {

        if(!(update.hasMessage() || update.hasCallbackQuery())) return;
        //creating user`s session

        User user;
        Message inputMessage = update.getMessage();
        String chatId = inputMessage == null ? update.getCallbackQuery().getMessage().getChatId().toString() : inputMessage.getChatId().toString();

        System.out.println("Current chat id: " + chatId);
        //registering the user, if needed
        if(this.usersCacheMap.containsKey(chatId)){
            user = usersCacheMap.get(chatId);
        }else{
            user = this.db.getUser(chatId);

            if(user == null){
                String username = "";
                if(update.hasMessage()){
                    Message msg = update.getMessage();
                    username = msg.getFrom().getUserName();
                }else if(update.hasCallbackQuery()){
                    username = update.getCallbackQuery().getFrom().getUserName();
                }

                user = new User(chatId, username);
                this.db.addUser(user);

            }

            this.usersCacheMap.put(chatId, user);
        }

        //else we just move forward

        //here i want to add User into Arraylist "InitializedUsers" and create a thread, where this User will be removed certain time
        //to clear program memory
        //also, instead of creating new user we object, we take old user object, if it is possible (mb hashmap?)

        
        //getting uset`s input

        String requestText = inputMessage == null ? 
            update.getCallbackQuery().getData() : 
            inputMessage.getText();
        requestText = requestText.toLowerCase();
        requestText = requestText.trim();

        System.out.println(requestText);

        HashMap<String, String> params = CommandLineParser.parseCommandString(requestText);

        //If user status is "waiting_input", we just take what_ever he typed in

        if(user.status == User.UserStatus.WAITING_INPUT){

            if(requestText.equals("/cancel")){
                user.currentThread.interrupt();
                
                return; 
            }

            user.inputData = requestText;
            synchronized(user.currentThread){
                user.currentThread.notify();
            }
            
            return;
        }


        //otherwise we give prepared responces
        SendMessage message = null;
        MainKeyboard mainKeyboard = new MainKeyboard();

        //  response to "/start"
        if(requestText.equals("/start")){
            message = new StartMessage(chatId);
             message.setReplyMarkup(mainKeyboard);
        }else if(requestText.equals("/help")){  // response to "/help"
            message = new HelpMessage(chatId);
             message.setReplyMarkup(mainKeyboard);
        }else if(requestText.equals("/add_reminder") ||
            requestText.equals("add reminder")    
        ){
            //giving options to create different types of reminders
            //(options with buttons with text like "/add_reminder:OneTimeReminder")
            message = new AddReminderMessage(chatId);
        }else if(requestText.startsWith("/add_reminder?")){
            if(!params.containsKey("type")) return;

            String type = params.get("type");


            //with relfection i can significantly improve this code:
            switch(type){
                case "onetime":
                    user.currentReminder = new OneTimeReminder();
                    break;
                case "multiple":
                    user.currentReminder = new MultipleReminder();
                    break;
                case "weekly":
                    user.currentReminder = new WeekReminder();
                    break;
                default:
                    return;
            }

            user.currentThread = user.currentReminder.generateCreationThread(this, user);

            user.currentReminder.reminderId = ++user.remindersCount;
            user.currentReminder.userId = user.id;

            user.status = User.UserStatus.WAITING_INPUT;

            //create special message
            sendMsg(  new SendMessage(chatId, "Initialization of reminder creation process..."));
            user.currentThread.start();

            return;
        }else if(requestText.startsWith("/reminder_list?")){
            System.out.println("is it even here");

            //what to choose?

            //parsing the the strign

            if(
                !params.containsKey("from") ||
                !params.containsKey("to")
            ) {
                System.out.println("there s no parameters");
                return;                
            }
            System.out.println("is it even here 2");

            int from, to;
            try{
                from = Integer.parseInt(params.get("from"));
                to = Integer.parseInt(params.get("to"));    
            }catch(Exception e){
                return;
            }

            ArrayList<Reminder> list =  this.db.getReminderList(chatId, from, to);
            if(list.size() == 0) return;

            //preparing inline keyboard with a list of reminders:
            InlineKeyboardMarkup keyboardMarkup = new ReminderListKeyboard(list, from, to);
            System.out.println(from + " " + to);
            System.out.println(list);

            message = new SendMessage(chatId, "There are your reminders: ");
            message.setReplyMarkup(keyboardMarkup);

        }else if(requestText.equals("/reminder_list") ||
            requestText.equals("reminder list")
        ){
            ArrayList<Reminder> list = this.db.getReminderList(chatId, 0, 6);

            InlineKeyboardMarkup keyboardMarkup = new ReminderListKeyboard(list, 0, 6);

            message = new SendMessage(chatId, "There are your reminders: ");
            message.setReplyMarkup(keyboardMarkup);

        }else if(requestText.startsWith("/get_reminder?")){
            //validation
            if(!params.containsKey("reminder_id")) return;

            int reminderId;
            try{
                reminderId = Integer.parseInt(params.get("reminder_id"));
            }catch(Exception e){
                return;
            }

            //the process of retireving and valdation of the process
            Reminder reminder = this.db.getReminder(chatId, reminderId);
            System.out.println(reminder);
            //!!! validation is missing right now
            if(reminder == null){
                this.sendMsg(new SendMessage(chatId, "The reminder was not found"));
            }

            //output
            reminder.printLongly(this);

        
        }else if(requestText.startsWith("/delete_reminder?")){

            //validaiton 
            if(!params.containsKey("reminder_id")) return;

            int reminderId;
            try{
                reminderId = Integer.parseInt(params.get("reminder_id"));
            }catch(Exception e){
                return;
            }


            //the process of deleting and validation of the process
            boolean result = this.db.deleteReminder(user.id, reminderId);
            if(result){ 

                try {
                    this.reminderControlThread.removeReminderThreads(chatId, reminderId);
    
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }                
                this.sendMsg(new SendMessage(user.id, "The reminder with id " + reminderId + " was successfully deleted"));
            }else sendMsg(new SendMessage(user.id, "The reminder with id " + reminderId + " was not found"));

            this.reminderControlThread.removeReminderThreads(user.id, reminderId);

        }else if(requestText.startsWith("/change_reminder?")){

            //validation
            if(!params.containsKey("reminder_id")){
                return;
            }
            int reminderId;
            try{
                reminderId = Integer.parseInt(params.get("reminder_id"));
            }catch(Exception e){
                return;
            }

            //preparing launch of the changing thread
            user.currentReminder = this.db.getReminder(chatId, reminderId);
            user.currentThread = user.currentReminder.generateChangingThread(this, user);
            user.currentReminder.userId = user.id;
            user.status = User.UserStatus.WAITING_INPUT;

            //changing special message
            sendMsg(  new SendMessage(chatId, "Initialization of reminder changing process..."));
            user.currentThread.start();

            return;
        }else if(requestText.startsWith("/reminder_thread_fail?") ||
            requestText.startsWith("/reminder_thread_success?")
        ){
            if(!(
                params.containsKey("reminder_id") &&
                params.containsKey("thread_id")
            )) return;

            int reminderId, threadId;
            Reminder reminder = null;
            try{
                reminderId = Integer.parseInt(params.get("reminder_id"));
                threadId = Integer.parseInt(params.get("thread_id"));

                reminder = this.db.getReminder(chatId, reminderId);
                if(reminder == null) return;
                
                if(!reminder.stopReminderThread(this, user, reminderId, threadId)) return;
                
            }catch(Exception e){
                return;
            }

            if(requestText.startsWith("/reminder_thread_fail?")) message = new ReminderFailMessage(chatId);
            else if(requestText.startsWith("/reminder_thread_success?")) message = new ReminderSuccessMessage(chatId);
        
        }else if(requestText.startsWith("/reminder_thread_postpone?")){
            if(!(
                params.containsKey("reminder_id") &&
                params.containsKey("thread_id")
            )) return;

            int reminderId, threadId;
            Reminder reminder = null;

            try{

                //validation
                reminderId = Integer.parseInt(params.get("reminder_id"));
                threadId = Integer.parseInt(params.get("thread_id"));
            
                reminder = this.db.getReminder(chatId, reminderId);
                if(reminder == null) return;

                ReminderThread reminderThread = this.reminderControlThread.getReminderThread(chatId, reminderId, threadId);
                if(reminderThread == null) return;


                //process
                user.currentReminder = reminder;
                user.status = User.UserStatus.WAITING_INPUT;

                PostponeReminderThread postponeThread = new PostponeReminderThread(this, user, threadId);
                user.currentThread = postponeThread;

                postponeThread.start();

            }catch(Exception e){
                System.out.println(e.getMessage());
            }

        }else if(requestText.equals("/reminder_threads_all")){
            Map<Integer, ArrayList<ReminderThread>> threads = this.reminderControlThread.getAllReminderThreads(chatId);

            for(int i = 0; i <= user.remindersCount; i++){
                if(!threads.containsKey(i)) continue;
                

                this.sendMsg(new SendMessage(chatId, "Threads for reminder with id " + i));
                ArrayList<ReminderThread> reminderThread = threads.get(i);

                for(int j = 0; j < reminderThread.size(); j++){
                    ReminderThread thread = reminderThread.get(j);

                    this.sendMsg(new SendMessage(chatId, "Reminder thread " + thread.threadId + " : time = " + thread.date.toString()));
                }

            }
            return;
        }

        if(message == null) return;
        // message.setReplyMarkup(mainKeyboard);
        sendMsg(message);

    }

    @Override
    public String getBotUsername() {
        
        return this.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        
        return this.TOKEN;
    }

    public static void main( String[] args )
    {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramTestBot(new SQLDataBase()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
