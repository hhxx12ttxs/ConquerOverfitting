package aflevering4;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by IntelliJ IDEA.
 * User: Mads Fisk
 * Date: 08-02-12
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class MultiChat {

    private static MulticastQueueFifoOnly<ArrayList> group;

    public static void main(String[] args) {

        group = new MulticastQueueFifoOnly<ArrayList>();
        if(args.length <1){
            try{
                InetAddress localhost = InetAddress.getLocalHost();
                String localhostAddress = localhost.getHostAddress();
                System.out.println("Creating a group...");
                group.createGroup(40202,MulticastQueue.DeliveryGuarantee.FIFO);
                String input = "";
                boolean running = true;
                Scanner in = new Scanner(System.in);
                MessageReader msgReader = new MessageReader(group);
                msgReader.start();
                while (running) {
                    input = in.nextLine();
                    if(!input.equals(("exit"))) {
                    	ArrayList<String> msg = new ArrayList<String>();
                    	msg.add(localhostAddress + " said: " + input);
                        group.put(msg);
                    } else {
                    	ArrayList<String> msg = new ArrayList<String>();
                    	msg.add(localhostAddress + " exited");
                        group.put(msg);
                        msgReader.interrupt();
                        running = false;
                    }
                }
                System.out.println("Existing host...");
                group.leaveGroup();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else{
            try {
                InetAddress localhost = InetAddress.getLocalHost();
                String localhostAddress = localhost.getHostAddress();
                System.out.println("Joining a group...");
                group.joinGroup(40202, new InetSocketAddress(args[0],40202), MulticastQueue.DeliveryGuarantee.FIFO);
                String input = "";
                boolean running = true;
                Scanner in = new Scanner(System.in);
                MessageReader msgReader = new MessageReader(group);
                msgReader.start();
                while (running) {
                    input = in.nextLine();
                    if(!input.equals("exit")) {
                    	ArrayList<String> msg = new ArrayList<String>();
                    	msg.add(localhostAddress + " said: " + input);
                        group.put(msg);
                    } else {
                    	ArrayList<String> msg = new ArrayList<String>();
                    	msg.add(localhostAddress + " exited");
                        group.put(msg);
                        msgReader.interrupt();
                        running = false;
                    }
                }
                System.out.println("Existing client...");
                group.leaveGroup();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

class MessageReader extends Thread {

    private MulticastQueue<ArrayList> group;
	private ArrayList<String> _messageCache;

    MessageReader(MulticastQueueFifoOnly<ArrayList> group) {
        this.group = group;
    }

    public void run() {
        setName("msgReaderThread");
        System.out.println("Starting " + getName());
        System.out.flush();
        _messageCache = new ArrayList<String>();

        try {
            MulticastMessage msg;
            // Lytter efter nye beskeder
            while ((msg = group.get()) != null) {
            	if(msg instanceof MulticastMessageJoin){
            		System.out.println(msg);
            		group.put(_messageCache);
            	} else if (msg instanceof MulticastMessagePayload<?>) {
                	ArrayList<String> payload = (ArrayList<String>)((MulticastMessagePayload) msg).getPayload();
                	if (payload.size() > 1) {
                		if(_messageCache.size() < 2){
                			System.out.println("[Receiving cached messages...]");
                			_messageCache = payload;
                			System.out.println("[Printing cached messages...]");
                			for(String s : _messageCache){
                				System.out.println(s);
                			}
                			System.out.println("[Finished printing cached messages]");                		
                		} else {
                			System.out.println("[Received a message cache but this client already has a cache]");	
                		}
                	} else if(payload.size() == 1){
                		System.out.println(payload.get(0));
                		_messageCache.add(payload.get(0));
                	}
                }
            }
        } catch (Exception e) {
            System.err.println("Thread error: " + e);
        }
    }
}

