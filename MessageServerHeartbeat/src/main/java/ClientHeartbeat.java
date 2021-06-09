import com.mes.Message;
import java.io.PrintWriter;
import java.time.LocalTime;
import java.util.List;

public class ClientHeartbeat extends Thread{
    private PrintWriter writer;
    private List<Message> hearts;
    private boolean isAlive = true;

    public void deactivate() {
        isAlive = false;
    }


    public ClientHeartbeat(List<Message> l, PrintWriter wr) {
        try {
            writer = wr;
            hearts = l;
            start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkKonnect() {
        try {
            Message testRequest = new Message("1");
            String ch = Integer.toString((int) (Math.random() * 100));
            testRequest.addFixStrPart(112, ch);

            synchronized (writer) {
                writer.write(testRequest.getFixStrFromParts() + "\n");
                writer.flush();
            }

            LocalTime start = LocalTime.now();
            while (hearts.isEmpty()) {
                Thread.sleep(10);
                if (LocalTime.now().getSecond() - start.getSecond() > 3)
                    return false;
            }


            synchronized (hearts) {
                Message heart = hearts.get(0);
                hearts.remove(0);
                String checkResult = heart.getFieldValueAsStringForTag(112);

                if (ch.equals(checkResult)) return true;
                else return false;
            }

        }
        catch (Exception e){
            return false;
        }

    }

    @Override
    public void run() {
        try{

            while (isAlive){
                if (checkKonnect()) {
                    if (!isAlive) break;
                    //System.out.println("true");
                    Thread.sleep(5000);
                }
                else {
                    if (!isAlive){
                        synchronized (writer) {
                            System.out.println("invalid server response. Connection is broken");
                            writer.write("stop" + "\n");
                            writer.flush();
                        }
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
            writer.close();
        }
    }
}
