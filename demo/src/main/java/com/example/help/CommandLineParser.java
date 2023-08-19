package com.example.help;

import java.util.HashMap;

public class CommandLineParser {

    public static HashMap<String, String> parseCommandString(String command){
        
        HashMap<String, String> result = new HashMap<>();

        if(command.split("\\?").length != 2) return new HashMap<>(); //invalid string

        String argumentsString = command.split("\\?")[1];

        String[] arguments = argumentsString.split(";");
        String[][] argumentPairs = new String[arguments.length][];


        for(int i = 0; i < arguments.length; i++){
            argumentPairs[i] = arguments[i].split("=");
            if(argumentPairs[i].length != 2) return new HashMap<>();

            if(argumentPairs[i][0].equals("") ||
                argumentPairs[i][1].equals("")) return new HashMap<>();

            result.put(argumentPairs[i][0], argumentPairs[i][1]);
        }

        return result;
    }
    
}
