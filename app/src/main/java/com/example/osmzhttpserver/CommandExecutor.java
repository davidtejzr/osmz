package com.example.osmzhttpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class CommandExecutor {
    private final String command;

    CommandExecutor(String command) {
        this.command = command;
    }

    public String execute() {
        List<String> parsedCommand = Arrays.asList(this.command.split("%20"));
        try {
            ProcessBuilder processBuilder= new ProcessBuilder(parsedCommand);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            return output.toString();
        }
         catch (Exception e) {
             return "Error executing command: " + parsedCommand;
            }
        }

}

