package com.example.osmzhttpserver;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ConnectionHandler implements Runnable {
    private final Context context;
    private final Socket socket;
    private final Semaphore semaphore;
    private final Thread thread;
    private final Handler handler;
    HttpResponseHandler httpResponseHandler = new HttpResponseHandler();
    Logger logger;
    TelemetryStreamer telemetryStreamer;

    public ConnectionHandler(Context context, Socket socket, Semaphore semaphore, Thread thread, Handler handler, TelemetryStreamer telemetryStreamer) {
        this.context = context;
        this.socket = socket;
        this.semaphore = semaphore;
        this.thread = thread;
        this.handler = handler;
        this.logger = new Logger(context);
        this.telemetryStreamer = telemetryStreamer;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                lines.add(line);
                Log.d("SERVER", line);
            }

            if (lines.size() == 0) {
                return;
            }
            logger.writeAccess(handler, socket.getRemoteSocketAddress().toString(), lines.get(0));

            String method = Arrays.asList(lines.get(0).split(" ")).get(0);
            Log.d("APP", method);

            String requestedUri = Arrays.asList(lines.get(0).split(" ")).get(1);
            Log.d("APP", requestedUri);

            if (requestedUri.equals("/streams/telemetry")) {
                JSONObject telemetryData = telemetryStreamer.getTelemetryData();
                httpResponseHandler.sendJSON(socket, telemetryData);
            }
            else if (requestedUri.contains("/cmd/")) {
                String command = requestedUri.replace("/cmd/", "");
                CommandExecutor ce = new CommandExecutor(command);
                String response = ce.execute();
                httpResponseHandler.sendText(socket, response);
            }
            else if (requestedUri.contains("/camera/stream")) {
                httpResponseHandler.sendMJPEGStream(socket, MainActivity.getCameraInstance());
            }
            else if (requestedUri.contains("/camera")) {
                httpResponseHandler.sendImage(socket, MainActivity.pictureData);
            }
            else {
                httpResponseHandler.sendFile(socket, context, requestedUri);
            }

            socket.close();
            Log.d("SERVER", "Socket Closed");

        } catch (IOException e) {
            e.printStackTrace();
            logger.writeError(e.toString());
        } finally {
            Log.d("CLIENT THREAD", "Terminating #" + thread.getId() + " thread");
            semaphore.release();
            Log.d("CLIENT THREAD", "Semaphore released, remaining permits: " + semaphore.availablePermits());
        }
    }
}
