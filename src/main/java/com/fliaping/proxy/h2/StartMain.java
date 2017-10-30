package com.fliaping.proxy.h2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author payne
 */
public class StartMain {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        Socket clientSocket = serverSocket.accept();
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        String request, response;
        while ((request = in.readLine()) != null) {
            if ("Done".equals(request)) {
                break;
            }
            response = request;
            out.print(response);
        }
    }
}
