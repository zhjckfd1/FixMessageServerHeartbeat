import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mes.Message;

public class SimpleChatClient {
    private static final int WAITTIME = 10;
    private PrintWriter writer;
    private BufferedReader reader;
    private Socket sock;
    private final static String SEPARATOR = "/001";
    private BufferedReader fileReader;
    private String fixPath;
    private List<Message> hearts = new ArrayList<>();
    private ClientHeartbeat heart;

    public SimpleChatClient(String fixPath) {
        this.fixPath = fixPath;
    }


    private void setUpNetworking() {
        try {
            sock = new Socket("127.0.0.1", 5000);
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            writer = new PrintWriter(sock.getOutputStream());
            fileReader = new BufferedReader(new FileReader(fixPath));

            System.out.println("Networking established");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFixStrForFields(Message m, String fixStr) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getMsgName());
        sb.append(", ");

        Map<String, Object> fixStrParts = m.getFixStrParts();
        for (Map.Entry<String, Object> entry : fixStrParts.entrySet()) {
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue());
            sb.append(", ");
        }

        sb.append("fixStr: ");
        sb.append(fixStr);

        return sb.toString();
    }

    private void waitReader(){
        try {
            while (!reader.ready())
                Thread.sleep(WAITTIME);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void go() {
        setUpNetworking();

        try {
            String message;
            heart = new ClientHeartbeat(hearts, writer);

            while ((message = fileReader.readLine()) != null) {

                if (message.equals("stop")) {           //wait stop?
                    synchronized (writer) {
                        writer.write(message + "\n");
                        writer.flush();
                    }
                    break;
                }

                try {
                    Message mes = new Message("D");           //(char?)
                    String fix = mes.getFixStrFromBody(message);
                    synchronized (writer) {
                        writer.write(fix + "\n");
                        writer.flush();
                    }

                    while (true) {
                        waitReader();
                        String answer = reader.readLine();
                        Message m = new Message();
                        m.parseStrToMap(answer);
                        if (m.getType().equals("0")) {
                            hearts.add(m);
                        }
                        else {
                            System.out.println(getFixStrForFields(m, answer));
                            break;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            synchronized (writer) {
                if (!sock.isClosed()) {
                    writer.write("stop" + "\n");
                    writer.flush();
                    System.out.println("stop");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (!sock.isClosed()) {
                    writer.close();
                    reader.close();
                    fileReader.close();
                    heart.deactivate();
                    sock.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        SimpleChatClient client = new SimpleChatClient(args[0]);
        client.go();
    }
}
