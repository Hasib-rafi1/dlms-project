package ReplicaManagerTwo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Listening extends Thread {

    public Thread t;
    public String threadName;
    public DatagramSocket serversocket;
    public ServerImp server;

    public Listening(String threadName, DatagramSocket serversocket, ServerImp server){
        this.serversocket = serversocket;
        this.server = server;
        this.threadName = threadName;
    }

    public void run(){
        byte []buffer = new byte[1000];
        try {
            while(true){
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                serversocket.receive(request);
                byte []newbuffer = new byte[request.getLength()];
                System.arraycopy(buffer, 0, newbuffer, 0, request.getLength());
                String message = new String(newbuffer).trim();
                InetAddress address = request.getAddress();
                int clientport = request.getPort();
                RequestExecution exe = new RequestExecution(server, serversocket, address, clientport, message);
                exe.start();
            }
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally{
            if(serversocket != null)
                serversocket.close();
        }

    }
    public void start () {
        if (t == null) {
            t = new Thread (this,threadName);
            t.start ();
        }
    }

}
