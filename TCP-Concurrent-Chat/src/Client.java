import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-threaded tcp chat client
 */
public class Client {
    private Socket clientSocket;

    public static void main(String[] args) {
        try {
            Client client = new Client();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Client() throws IOException {

        //Connect to server
        clientSocket = new Socket("localhost",8081);
        System.out.println("Connected: " + clientSocket);
        start();
    }

    public void start() {
        // Create an executor for a single thread
        ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
        singleExecutor.submit(new Handler());

        try {

            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter socketOutput = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            while (!clientSocket.isClosed()) {

                String msg;

                try {
                    // Blocks waiting for user input
                    msg = consoleInput.readLine();

                } catch (IOException e) {
                    System.out.println("Error reading message: " + e.getMessage());
                    break;
                }

                if (msg == null || msg.equals("/quit")) {
                    break;
                }

                socketOutput.write(msg);
                socketOutput.newLine();
                socketOutput.flush();
            }

            try {

                consoleInput.close();
                socketOutput.close();
                clientSocket.close();

            } catch (IOException e) {

                System.out.println("Error closing connection: " + e.getMessage());
            }

        } catch (IOException e) {

            System.out.println("Error sending message to server: " + e.getMessage());
        }
    }


    private class Handler implements Runnable {

        @Override
        public void run() {

            try {

                BufferedReader socketInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                while (!clientSocket.isClosed()) {

                    String msg = socketInput.readLine();

                    if (msg != null) {

                        System.out.println(msg);

                    } else {

                        try {

                            System.out.println("Connection closed, exiting...");
                            socketInput.close();
                            clientSocket.close();

                        } catch (IOException e) {

                            System.out.println("Error closing connection: " + e.getMessage());
                        }
                    }
                }

            } catch (IOException e) {

                System.out.println("Error reading from server: " + e.getMessage());
            }

            System.exit(0);
        }
    }
}