import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

import com.mes.Message;


public class ClientHandler extends Thread{
    private BufferedReader reader;
    private Socket sock;
    private PrintWriter writer;

    public ClientHandler(Socket clientSocket) {
        try {
            sock = clientSocket;
            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            writer = new PrintWriter(sock.getOutputStream());
            start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendReject(String text){
        Message mes = new Message("3");
        mes.addFixStrPart(58, text);

        writer.write(mes.getFixStrFromParts() + "\n");
        writer.flush();
    }

    private void sendMessageFromAnotherMessage(String type, Map<String, Object> FixStrParts){
        Message mes = new Message(type);
        mes.getBodyFromIdenticalFixStrParts(FixStrParts);

        writer.write(mes.getFixStrFromParts() + "\n");
        writer.flush();
    }

    @Override
    public void run(){
        String message;
        try{

            //while(!(message=in.readLine()).equals("stop"))
            while ((message = reader.readLine()) != null){

                if(message.equals("stop"))
                {
                    System.out.println("client passed out");
                    break;
                }

                Message m = new Message();
                m.parseStrToMap(message);

                if(!m.getResult().equals("ok")) {
                    sendReject(m.getResult());
                }
                else {
                    switch (m.getType()) {
                        case ("D"):
                            sendMessageFromAnotherMessage("8", m.getFixStrParts());
                            break;
                        case("1"):
                            sendMessageFromAnotherMessage("0", m.getFixStrParts());
                            break;
                        default:
                            sendReject("unknown type");
                            break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            try {
                if (!sock.isClosed()) {
                    writer.close();
                    reader.close();
                    sock.close();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}

