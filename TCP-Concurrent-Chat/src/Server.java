import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-threaded tcp chat server
 */
public class Server {

    public final static int DEFAULT_PORT = 8081;
    private final Queue<ServerHandler> handlers = new LinkedList<>();


    public static void main(String[] args) {

        int port = DEFAULT_PORT;

        try {

            //Optional port number as command line argument
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }

            Server chatServer = new Server();
            chatServer.start(port);

        } catch (NumberFormatException ex) {

            System.out.println("Usage: java ChatServer [port_number]");
            System.exit(1);

        }

    }

    private void start(int port) {

        int connectionCount = 0;

        try {

            // Bind to local port
            System.out.println("Binding to port " + port + ", please wait  ...");
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started: " + serverSocket);

            while (true) {

                //Waiting loop for client connections
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client accepted: " + clientSocket);

                // Create a new Server Handler
                connectionCount++;
                String name = "Client-" + connectionCount; //Clients name
                ServerHandler client = new ServerHandler(name, clientSocket);
                handlers.add(client);

                // Serve the client connection with a Thread Pool
                ExecutorService cachedPool = Executors.newCachedThreadPool();
                cachedPool.submit(client);

            }

        } catch (IOException e) {
            System.out.println("Unable to start server on port " + port);
        }
    }

    private void writeAll(String clientName, String message) {
        for (ServerHandler client : handlers) {
            client.write(clientName, message);
        }
    }

    private class ServerHandler implements Runnable {

        final private String name;
        final private Socket clientSocket;
        BufferedReader in;
        PrintWriter out;
        String msg;

        private ServerHandler(String name, Socket clientSocket) throws IOException {
            this.name = name;
            this.clientSocket = clientSocket;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream());
        }

        @Override
        public void run() {

            //While not closed
            while (!clientSocket.isClosed()) {

                try {

                    //Blocks waiting for client messages
                    msg = in.readLine();

                    if (msg == null) {

                        System.out.println("Client " + name + " closed, exiting...");

                        in.close();
                        clientSocket.close();
                        continue;

                    } else if (msg == "/quit") {

                        //closes streams and sockets
                        System.out.println("Client is closing connection");
                        out.close();
                        in.close();
                        clientSocket.close();

                    } else {

                        writeAll(name, msg);

                    }

                    handlers.remove(this);

                } catch (IOException e) {
                    System.out.println("Receiving error on " + name + " : " + e.getMessage());
                }
            }
        }


        private void write(String clientName, String message) {

            out.write(clientName + ": " + message);
            out.flush();

        }
    }
}
