package Class;
/*
 * shelby, ji, and alexis worked on the code together in a group 
 * DISCLOSURE: we did use ai to make the servers connect
 */

import java.net.ServerSocket; //ServerSocket creates a server that listens on a port
import java.net.Socket; //Socket represents a connection between two computers (client, server)
import java.io.PrintStream;//sends data to the client through the socket
import java.util.*;//imports all library utilities without having to list out each one for HashMap etc.


public class TIM_Server {

    // username - password
	/*
	 * this is the HashMap data structure to store all the registered user
	 * recommended to use in class
	 * the key is the username
	 * the value is the password
	 */
    public static Map<String, String> users = new HashMap<>();

    // username - active connection output
    /*
     * keeps a record of CURRENTLY connected users
     * stores the print stream of each user so we can look up the PrintStream
     * then send data through the socket and send messages to specific people(clients)
     */
    public static Map<String, PrintStream> connectedUsers = new HashMap<>();

    // username - stored offline messages
    /*
     * stores messages for users who are NOT connected 
     * when it reconnects you can then send the stored messages
     */
    public static Map<String, List<String>> offlineMessages = new HashMap<>();//use of AI helped use implement the HashMap

    public static void main(String[] args) {//main method for the server
//throws error if the port is already in use or any connection issues
        try {
            ServerSocket server = new ServerSocket(7777);//creates a server and binds it to port 7777 (IP Address)
            System.out.println("Server running on port 7777...");//test to check its working
//port is the number (7777) 
//ServerSocket is the connector
//Socket is the line created once the connection in complete

            while (true) {//infinite loop will stop once I terminate in the console, is always listening for new clients
                Socket socket = server.accept();//waits until a client connects
//a new Socket object is created to represent the private line channel between server and client
                TIM_Server_Runnable handler = new TIM_Server_Runnable(socket);
//this passes the client's Socket to the new object handler to manage the connection with client
                new Thread(handler).start();//this allows the server to handle multiple clients at once
//clients run in individual threads so multiple clients can chat at the same time
            }

        } catch (Exception e) {//catches any error e
            e.printStackTrace();//prints out the error
        }
    }
}
