package Class;
/*
 * shelby, ji, and alexis worked on the code together in a group 
 * DISCLOSURE: we did use ai to make the servers connect
 */
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class TIM_Server_Runnable implements Runnable {//runnable allows a separate thread for each client

    private Socket socket;//the socket is the client connection
    private PrintStream output;//the output is the messages sent to the client
    private Scanner input;//the input is the messages read from the client
    private String currentUser;   //this will keep track of which user is connected on this thread

    public TIM_Server_Runnable(Socket socket) {//this stores the client socket for communication
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
//initialize the output and input from the socket using get
            output = new PrintStream(socket.getOutputStream());
            input = new Scanner(socket.getInputStream());

            while (input.hasNextLine()) {//reads client messages infinitely
                String line = input.nextLine();
                handleCommand(line);//this will parse and executes the commands from the client
            }

        } catch (IOException e) {//throws error
            e.printStackTrace();
        } finally {
            disconnect();   //disconnect ensures the server is disconnected despite errors
        }
    }

    private void disconnect() {   
        try {
            if (currentUser != null) {
                TIM_Server.connectedUsers.remove(currentUser);
        //removes users from connectedUsers if logged in
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();//makes sure socket is closed
            }

        } catch (IOException e) {//throws error
            e.printStackTrace();
        }
    }
/*
 * the following handle commands were made with the help of AI
 */
    private void handleCommand(String line) {//command handler

        String[] parts = line.split(" ", 3);//this splits the input into parts
        String command = parts[0];

        switch (command) {//the switch command will determine which method to call
        //can only use 4 letter commands
            case "CRTE"://create command
                handleCreate(parts);
                break;

            case "AUTH"://authorize user command
                handleAuth(parts);
                break;

            case "SEND"://send a message command
                handleSend(parts);   // ✅ fixed
                break;

            case "QUIT"://quit command
                handleQuit();
                break;

            default:
                output.println("205 No command exists \"" + command + "\".");//this is the message if the command is unknown
        }
    }

    //CRTE 
    private void handleCreate(String[] parts) {

        if (parts.length < 3) {
            output.println("205 No such command \"CRTE\".");
            return;
        }

        String username = parts[1];
        String password = parts[2];

        synchronized (TIM_Server.users) {//Synchronized modify's shared users map

            if (TIM_Server.users.containsKey(username)) {  
                output.println("203 User " + username + " already exists.");
                return;
            }

            TIM_Server.users.put(username, password);
            TIM_Server.offlineMessages.put(username,
                    new java.util.ArrayList<>());//an empty list for offline messages

            if (TIM_Server.users.size() == 1) {
                output.println("105 User " + username +
                        " created as superuser.");//first user is superUser
            } else {
                output.println("104 User " + username + " created.");
            }
        }
    }

    // AUTH 
    private void handleAuth(String[] parts) {

        if (parts.length < 3) {
            output.println("204 Invalid user name or password.");//makes sure username and password are valid
            return;
        }

        String username = parts[1];
        String password = parts[2];

        if (currentUser != null) {
            output.println("202 Already connected as " +
                    currentUser + ".");
            return;
        }

        if (!TIM_Server.users.containsKey(username) ||
                !TIM_Server.users.get(username).equals(password)) {
            output.println("204 Invalid user name or password.");
            return;
        }

        if (TIM_Server.connectedUsers.containsKey(username)) {
            output.println("201 User " + username +
                    " already connected.");//makes sure there isn't a copy of users
            return;
        }

        currentUser = username;
        TIM_Server.connectedUsers.put(username, output);

        output.println("102 Connected as " + username + ".");

        // Delivers the offline messages
        List<String> messages =
        		TIM_Server.offlineMessages.get(username);

        for (String msg : messages) {
            output.println(msg);
        }

        messages.clear();
    }

    // SEND 
    private void handleSend(String[] parts) {

    	//ensures the sender is logges in

        if (currentUser == null) {
            output.println("206 Not connected as a user.");//the error messages are from the instructions paper
            return;
        }

        if (parts.length < 3) {
            output.println("205 No such command \"SEND\".");
            return;
        }

        String receiver = parts[1];
        String message = parts[2];
//checks if receiver exists
        if (!TIM_Server.users.containsKey(receiver)) {
            output.println("200 User " + receiver +
                    " doesn’t exist.");
            return;
        }

        String formatted =
                "100 Message from " + currentUser +
                " follows: \"" + message + "\"";

        if (TIM_Server.connectedUsers.containsKey(receiver)) {
        	TIM_Server.connectedUsers.get(receiver)
                    .println(formatted);
        } else {
        	TIM_Server.offlineMessages//if the receiver is offline store in offline messages
                    .get(receiver)
                    .add(formatted);
        }

        output.println("101 Message sent.");
    }

    // QUIT
    private void handleQuit() {
        output.println("103 Bye.");//bye message
        disconnect();//calls disconnect to remove the user and closes the socket
    }
}
