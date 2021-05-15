package com.example.TouchApp;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ConnectApp {
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://vannfalt.se:3000");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
