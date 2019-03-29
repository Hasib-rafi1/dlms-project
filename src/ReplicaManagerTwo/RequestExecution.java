package ReplicaManagerTwo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RequestExecution extends Thread{

    public Thread t;
    public ServerImp server;
    public DatagramSocket serversocket;
    public InetAddress address;
    public int clientport;
    String message;

    public RequestExecution(ServerImp server, DatagramSocket serversocket, InetAddress address, int clientport, String message) {
        super();
        this.address = address;
        this.message = message;
        this.server = server;
        this.clientport = clientport;
        this.serversocket = serversocket;
    }

    public void start(){
        if(t == null){
            t = new Thread(this);
            t.start();
        }
    }

    public void run(){
        String result = "";
        if(message.startsWith("findItem")){
            result = UDPfindItem(message);
        }
        else if(message.startsWith("borrowItem")){
            result = String.valueOf(UDPborrowItem(message));
        }
        else if(message.startsWith("waitInQueue")){
            result = String.valueOf(UDPwaitInQueue(message));
        }
        else if(message.startsWith("returnItem")){
            result = String.valueOf(UDPreturnItem(message));
        }
//        else if(message.startsWith("listBorrowedItem")){
//            result = UDPlistBorrowedItem(message);
//        }
        else if (message.startsWith("exchangeItem")){
            result = String.valueOf(UDPexchangeItem(message));
        }
        byte []buffer = result.getBytes();
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length,address,clientport);
        try {
            serversocket.send(reply);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean UDPborrowItem(String message){
        String arguments = message.substring(message.indexOf("(") + 1,message.length() - 1);
        String arg[] = arguments.split(",");
        String userID = arg[0];
        String itemID = arg[1];
        boolean result = server.borrowLocal(userID, itemID);
        return result;
    }

    public boolean UDPwaitInQueue(String message){
        String arguments = message.substring(message.indexOf("(") + 1,message.length() - 1);
        String arg[] = arguments.split(",");
        String userID = arg[0];
        String itemID = arg[1];
        boolean result = server.waitInLocal(userID, itemID);
        return result;
    }

    public String UDPfindItem(String message){
        String data = message.substring(message.indexOf("(") + 1,message.length() - 1);
        String result = server.findItemLocal(data);
        return result;

    }

    public boolean UDPreturnItem(String message) {
        String arguments = message.substring(message.indexOf("(") + 1,message.length() - 1);
        String arg[] = arguments.split(",");
        String itemID = arg[0];
        String userID = arg[1];
        boolean result = server.returnLocal(itemID, userID);
        return result;
    }

//    private String UDPlistBorrowedItem(String message) {
//        String userID = message.substring(message.indexOf("(") + 1,message.length() - 1);
//        String result = server.listBorrowedLocal(userID);
//        return result;
//    }

    private boolean UDPexchangeItem(String message) {
        String arguments = message.substring(message.indexOf("(") + 1,message.length() - 1);
        String arg[] = arguments.split(",");
        String studentID = arg[0];
        String newItemID = arg[1];
        String oldItemID = arg[2];
        boolean result = server.exchangeLocal(studentID,newItemID,oldItemID);
        return result;
    }
}
