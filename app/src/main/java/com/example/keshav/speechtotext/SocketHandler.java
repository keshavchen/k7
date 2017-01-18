package com.example.keshav.speechtotext;

/**
 * Created by root on 18/1/17.
 */


import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;


public class SocketHandler {
    private static Socket socket;

    public static synchronized Socket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(){
        try {
            SocketHandler.socket = IO.socket("http://192.168.1.2:9000");
            //SocketHandler.socket = IO.socket("http://104.199.174.3:9000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
}
