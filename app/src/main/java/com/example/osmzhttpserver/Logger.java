package com.example.osmzhttpserver;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class Logger {
    enum LogLevel { ACCESS, ERROR }
    Context context;

    public Logger(Context context) {
        this.context = context;
    }

    public void writeAccess(Handler handler, String remoteSocketAddress, String req) {
        Bundle b = new Bundle();
        b.putString("address", remoteSocketAddress);
        b.putString("timestamp", new Date().toString());
        b.putString("req", req);


        Message msg = handler.obtainMessage();
        msg.setData(b);
        msg.sendToTarget();

        String message = msg.getData().getString("address") + " - - [" + msg.getData().getString("timestamp") + "] \"" + msg.getData().getString("req") + "\"\n";
        this.appendToFile(message, LogLevel.ACCESS);
    }

    public void writeError(String err) {
        String message = "[" + new Date() + "] - " + err + "\n";
        this.appendToFile(message, LogLevel.ERROR);
    }

    public void write503(String remoteSocketAddress) {
        String message = remoteSocketAddress + " - - [" + new Date() + "] - HTTP 503 Server too busy\n";
        this.appendToFile(message, LogLevel.ERROR);
    }

    private void appendToFile(String content, LogLevel logLevel) {
        String fileName = logLevel == LogLevel.ACCESS ? "access.log" : "error.log";
        File file = new File(context.getExternalFilesDir(null), fileName);

        try {
            if (!file.exists()) {
                return;
            }
            FileWriter writer = new FileWriter(file, true);
            writer.append(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
