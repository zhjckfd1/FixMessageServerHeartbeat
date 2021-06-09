import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SimpleChatServer {
    private static List<ClientHandler> clientsList = new ArrayList<>();
    protected final static String SEPARATOR = "/001";

    public void go() {
        try (ServerSocket serverSock = new ServerSocket(5000)){
            System.out.println("server ready for work");

            while (true) {
                Socket clientSocket = serverSock.accept();
                try {
                    clientsList.add(new ClientHandler(clientSocket));

                } catch (Exception e) {
                    clientSocket.close();
                }
                System.out.println("got a connection");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new SimpleChatServer().go();
    }
}
