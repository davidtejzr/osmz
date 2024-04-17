package com.example.osmzhttpserver;

import android.content.Context;
import android.hardware.Camera;
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
        try {
            File file = new File(context.getExternalFilesDir(null), filePath);
            if (filePath.equals("/")) {
                File indexFile = new File(context.getExternalFilesDir(null), "index.html");
                if (!indexFile.exists()) {
                    sendDirectoryStructure(socket, context.getExternalFilesDir(null));
                    return;
                } else {
                    filePath = "index.html";
                }
            }  else if (file.isDirectory()) {
                sendDirectoryStructure(socket, file);
                return;
            }

            OutputStream o = socket.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(o);
            if (!file.exists()) {
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

    public void sendText(Socket socket, String text) {
        try {
            OutputStream o = socket.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(o);

            String header = "HTTP/1.0 200 OK\n" +
                    "Date: " + new Date() + "\n" +
                    "Content-Type: text/plain\n" +
                    "Content-Length: " + text.length() + "\n\n";

            out.write((header + text).getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendHtml(Socket socket, String html) {
        try {
            OutputStream o = socket.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(o);

            String header = "HTTP/1.0 200 OK\n" +
                    "Date: " + new Date() + "\n" +
                    "Content-Type: text/html\n" +
                    "Content-Length: " + html.length() + "\n\n";

            out.write((header + html).getBytes());
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendDirectoryStructure(Socket socket, File rootDirectory) {
        try {
            String directoryStructure = getDirectoryStructure(rootDirectory, "/", false);
            sendHtml(socket, directoryStructure);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getDirectoryStructure(File directory, String path, Boolean submenu) throws IOException {
        StringBuilder directoryStructure = new StringBuilder();
        File[] files = directory.listFiles();

        if (path.equals("/")) {
            directoryStructure.append("<h1>").append(directory.getName()).append("</h1>");
        }

        if (files != null) {
            directoryStructure.append("<ul>");
            if (!submenu && !directory.getName().equals("files")) {
                directoryStructure.append("<li><a href=\"/\">..</a></li>");
                if (directory.getParentFile() != null) {
                    String parentPath = directory.getParentFile().getName();
                    if (parentPath.equals("files")) {
                        parentPath = "/";
                    }
                    directoryStructure.append("<li><a href=\"").append(parentPath).append("\">.</a></li>");
                }
            }
            for (File file : files) {
                String newPath = file.getName();
                if (file.isDirectory()) {
                    directoryStructure.append("<li><a href=\"").append(newPath).append("\">").append(file.getName()).append("</a>").append(getDirectoryStructure(file, newPath, true)).append("</li>");
                } else {
                    directoryStructure.append("<li><a href=\"").append(newPath).append("\">").append(file.getName()).append("</a></li>");
                }
            }
            directoryStructure.append("</ul>");
        }

        return directoryStructure.toString();
    }

    public void sendImage(Socket socket, byte[] imageData) {
        try {
            OutputStream o = socket.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(o);

            String header = "HTTP/1.0 200 OK\n" +
                    "Date: " + new Date() + "\n" +
                    "Content-Type: image/jpeg\n" +
                    "Content-Length: " + imageData.length + "\n\n";

            out.write((header).getBytes());
            out.write(imageData);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMJPEGStream(Socket socket, Camera camera) {
        try {
            OutputStream o = socket.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(o);

            String header = "HTTP/1.0 200 OK\n" +
                    "Date: " + new Date() + "\n" +
                    "Content-Type: multipart/x-mixed-replace; boundary=OSMZ_boundary\n\n";

            out.write((header).getBytes());

            while (true) {
                byte[] imageData = MainActivity.pictureData;
                if (imageData != null) {
                    out.write(("--OSMZ_boundary\n" +
                            "Content-Type: image/jpeg\n" +
                            "Content-Length: " + imageData.length + "\n\n").getBytes());
                    out.write(imageData);
                    out.write("\n\n".getBytes());
                    out.flush();
                }
                Thread.sleep(50);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
