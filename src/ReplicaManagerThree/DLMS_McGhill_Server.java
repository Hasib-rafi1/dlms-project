package ReplicaManagerThree;

import java.io.IOException;
import java.rmi.server.ExportException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.PriorityQueue;


import java.net.*;

public class DLMS_McGhill_Server
{

	public static DLMS_McGhill_Implementation mcgObjecct;
	public static int RMNo = 32;
	public static void main(String args[]) throws ExportException
	{
		try 
		{
			
			DLMS_McGhill_Implementation obj = new DLMS_McGhill_Implementation();
			mcgObjecct = obj;

			System.out.println("McGhill Server ready and waiting ...");
			
			Runnable task= () ->{
				try {
					connect_MCG_UDP_Server(obj);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			
			Thread thread = new Thread(task);
			thread.start();
			
			Runnable task2 = () -> {
				receiveFromSequencer();
			};
			Thread thread2 = new Thread(task2);
			thread2.start();

			sendMessageBackToFrontend("Listen from RM3 McGhill");
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
		
	}

    private static void connect_MCG_UDP_Server(DLMS_McGhill_Implementation mcgimplPublic) throws IOException, ParseException, Exception
    {
        DatagramSocket serverSocket = new DatagramSocket(9877);
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        String returnvalue = null;

        while(true)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String sentence = new String(receivePacket.getData());

            String[] parts =sentence.split(":");
            String part1 = parts[0];
            String part2 = parts[1];
            String part3 = parts[2];
            System.out.println("RECEIVED:"+part1+":"+part2+":"+part3);

            if(part1.equals("0"))
            {
                returnvalue = Boolean.toString(mcgimplPublic.borrowItem(part2,part3,0));

            }
            if(part1.equals("1"))
            {
                returnvalue = Boolean.toString(mcgimplPublic.returnItem(part2,part3));

            }
            if(part1.equals("2"))
            {
                returnvalue = mcgimplPublic.findBook(part2,part3);

            }
            if(part1.equals("3"))
            {
                returnvalue = mcgimplPublic.exchangeCheck1(part2,part3);
            }
            if(part1.equals("4"))
            {
                returnvalue = mcgimplPublic.exchangeCheck2(part2,part3);
            }

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            returnvalue = returnvalue+":";
            sendData = returnvalue.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, returnvalue.length(), IPAddress, port);
            serverSocket.send(sendPacket);
        }
    }

    private static void receiveFromSequencer() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(7775);
			byte[] buffer = new byte[1000];
			System.out.println("Sequencer UDP Server 7775 Started............");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String sentence = new String( request.getData(), 0,
						request.getLength() );
				if(!sentence.equals("Test")&&!sentence.equals("fault")) {
					findNextMessage(sentence);
				}else if(sentence.equals("fault")) {
					mcgObjecct.fault=false;
				}
			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	public static void findNextMessage(String sentence) {
				String message = sentence;
				String[] parts = message.split(";");
				String function = parts[0]; 
				String userID = parts[1]; 
				String itemName = parts[2]; 
				String itemId = parts[3]; 
				String newItemId = parts[4];
				int number = Integer.parseInt(parts[5]);
				System.out.println(message);
				String sendingResult ="";
				if(function.equals("addItem")) {
					sendingResult = mcgObjecct.addItem(userID,itemId, itemName,number);
				}else if(function.equals("removeItem")) {
					String result = mcgObjecct.removeItem(userID, itemId,number);
					sendingResult = result;
				}else if(function.equals("listItemAvailability")) {
					String result = mcgObjecct.listItemAvailability(userID);
					sendingResult = result;
				}else if(function.equals("borrowItem")) {
					boolean result = mcgObjecct.borrowItem(userID, itemId,number);
					sendingResult = Boolean.toString(result);
				}else if(function.equals("findItem")) {
					sendingResult = mcgObjecct.findItem(userID,itemName);
				}else if(function.equals("returnItem")) {
					boolean result = mcgObjecct.returnItem(userID,itemId);
					sendingResult = Boolean.toString(result);
				}else if(function.equals("waitInQueue")) {
					boolean result = mcgObjecct.waitInQueue(userID,itemId);
					sendingResult = Boolean.toString(result);
				}else if(function.equals("exchangeItem")) {
					boolean result = mcgObjecct.exchangeItem(userID,newItemId,itemId);
					sendingResult = Boolean.toString(result);
				}

				sendingResult= sendingResult+":"+RMNo+":"+message+":";
				sendMessageBackToFrontend(sendingResult);			 
	}
	
	public static void sendMessageBackToFrontend(String message) {
		System.out.println(message);
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] m = message.getBytes();
			InetAddress aHost = InetAddress.getByName("230.1.1.5");

			DatagramPacket request = new DatagramPacket(m, m.length, aHost, 1413);
			aSocket.send(request);
			aSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
