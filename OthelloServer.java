/*
 * Server program that accepts a board as input and returns a move.
 * @author Matt Hastings
 *
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class OthelloServer {

    public static void main(String[] args) {
        ServerSocket server;
        
        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress("0.0.0.0", 12321));
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            OthelloServerWorker worker;
        
            try {
                worker = new OthelloServerWorker(server.accept());
                Thread thread = new Thread(worker);
                thread.start();
            }
            catch (IOException e) {
                e.printStackTrace(); // A number of different errors can go wrong. We just want to know what happened.
            }
        }
    }
}
