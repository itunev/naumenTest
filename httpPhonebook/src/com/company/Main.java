package com.company;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeSet;

public class Main {
    public static void main(String[] args) {
        Phonebook pb = new Phonebook("phonebook");

        try {
            ServerSocket ss = new ServerSocket(8080);
            while (true) {
                Socket s = ss.accept();
                System.err.println("Client accepted");
                new Thread(new Server(s, pb)).start();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
