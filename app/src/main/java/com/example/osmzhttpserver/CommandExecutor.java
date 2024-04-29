package com.example.osmzhttpserver;

import android.os.Build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

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

            StringBuilder output = new StringBuilder();
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            outputThread.start();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            }

            outputThread.join();
            return output.toString();
        }
         catch (Exception e) {
             return "Error executing command: " + parsedCommand;
            }
        }

}

