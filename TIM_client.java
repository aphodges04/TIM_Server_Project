package Class;
/*
 * shelby, ji, and alexis worked on the code together in a group 
 * DISCLOSURE: we did use ai to make the servers connect
 */

import java.net.Socket;
import java.io.PrintStream;
import java.util.Scanner;

public class TIM_client {

    public static void main(String[] args) {

        try {//try and catch exceptions
            Socket socket = new Socket("localhost", 7777);//connect to server local host with given digits in class
            System.out.println("Connected to server.");//for testing

            PrintStream output = new PrintStream(socket.getOutputStream());//output to send messages to server
            Scanner serverInput = new Scanner(socket.getInputStream());//creates scanner to read messages from server
            Scanner keyboard = new Scanner(System.in);//uses scanner to read user input from keyboard

            // Thread to listen to server this next portion is helped with AI
            new Thread(() -> {//new thread so the client can listen to server messages simultaneously 
                 while (serverInput.hasNextLine()) {//while loop as server sends inputs
                    System.out.println(serverInput.nextLine());//print the input
                }
            }).start();//starts listening thread

            // Main loop to send user input to server
            while (keyboard.hasNextLine()) {//while has next line
                String line = keyboard.nextLine();//read line
                output.println(line);//send to server
            }

        } catch (Exception e) {
            e.printStackTrace();//prints exception from stack trace
        }
    }
}