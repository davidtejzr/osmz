package com.example.osmzhttpserver;

import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

public class HttpResponseHandler {
    public void send503(Socket socket, Logger logger) {
        try {
            OutputStream o = socket.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(o);

            String html = "<html><body><h1>Server too busy. Please try again later.</h1></body></html>";
            String header = "HTTP/1.1 503 Server too busy\n" +
                    "Date: " + new Date() + "\n" +
                    "Content-Type: text/html\n" +
                    "Content-Length: " + html.length() + "\n\n";

            out.write((header + html).getBytes());
            out.flush();
            out.close();
            Log.d("SERVER", "Server too busy");
            logger.write503(socket.getRemoteSocketAddress().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send404(Socket socket, String filePath) {
        try {
            OutputStream o = socket.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(o);

            String html = "<html><h1>File <i>" + filePath + "</i> not found </h1></html>";
            String header = "HTTP/1.0 404 Not Found\n" +
                    "Date: " + new Date() + "\n" +
                    "Content-Type: text/html\n" +
                    "Content-Length: " + html.length() + "\n\n";

            out.write((header + html).getBytes());
            out.flush();
            out.close();
            Log.d("SERVER", "File not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(Socket socket, Context context, String filePath) {
        if (filePath.equals("/")) {
            filePath = "index.html";
        }

        try {
            OutputStream o = socket.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(o);
            File file = new File(context.getExternalFilesDir(null), filePath);
            if (!file.exists() || file.isDirectory()) {
                this.send404(socket, filePath);
            } else {
                FileInputStream fin;
                try {
                    fin = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                BufferedInputStream bis = new BufferedInputStream(fin);
                byte[] buffer = new byte[1024];
                int bytesRead;

                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                String fileExtension = file.getName().split("\\.")[1];

                String header = "HTTP/1.0 200 OK\n" +
                        "Date: " + new Date() + "\n" +
                        "Content-Type: " + mimeTypeMap.getMimeTypeFromExtension(fileExtension) + "\n" +
                        "Content-Length: " + file.length() + "\n\n";


                out.write((header).getBytes());

                while ((bytesRead = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                bis.close();
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendJSON(Socket socket, JSONObject jsonData) {
        try {
            OutputStream o = socket.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(o);

            String json = jsonData.toString();
            String header = "HTTP/1.0 200 OK\n" +
                    "Date: " + new Date() + "\n" +
                    "Content-Type: application/json\n" +
                    "Access-Control-Allow-Origin: *\n" +
                    "Content-Length: " + json.length() + "\n\n";

            out.write((header + json).getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
