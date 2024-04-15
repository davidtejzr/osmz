package com.example.osmzhttpserver;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class SocketServer extends Thread {
    public static final int PORT = 12345;
    public static final int MAX_SEMAPHORE_PERMITS = 2;
    ServerSocket serverSocket;
    boolean bRunning;
    Context context;
    Semaphore semaphore;
    Handler handler;
    HttpResponseHandler httpResponseHandler = new HttpResponseHandler();
    Logger logger;
    TelemetryStreamer telemetryStreamer;

    public SocketServer(Context context, Handler handler) {
        this.context = context;
        this.semaphore = new Semaphore(MAX_SEMAPHORE_PERMITS);
        this.handler = handler;
        this.logger = new Logger(context);
        this.telemetryStreamer = new TelemetryStreamer(context);
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        bRunning = false;
    }

    public void run() {
        try {
            Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(PORT);
            bRunning = true;

            while (bRunning) {
                Log.d("SERVER", "Socket Waiting for connection");
                Socket socket = serverSocket.accept();
                Log.d("SERVER", "Socket Accepted");

                if (semaphore.tryAcquire()) {
                    Thread thread = new Thread(new ConnectionHandler(context, socket, semaphore, this, handler, telemetryStreamer));
                    Log.d("CLIENT THREAD", "Starting #" + String.valueOf(thread.getId()) + " thread");
                    Log.d("CLIENT THREAD", "Semaphore remaining permits: " + semaphore.availablePermits());

                    thread.start();
                } else {
                    httpResponseHandler.send503(socket, logger);
                }
            }
        }
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
            }
        } finally {
            serverSocket = null;
            bRunning = false;
        }
    }

}

