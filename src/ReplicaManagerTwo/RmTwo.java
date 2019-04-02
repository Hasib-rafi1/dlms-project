package ReplicaManagerTwo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.PriorityQueue;

import ReplicaManagerOne.Map.Message;
import ReplicaManagerOne.Map.MessageComparator;

public class RmTwo {
	public static int nextSequence = 1;
	public static PriorityQueue<Message> pq = new PriorityQueue<Message>(20, new MessageComparator()); 
	public static int con_fault = 0; 
	public static int mcg_fault = 0;
	public static int mon_fault = 0;
	public static void main(String[] args) {
		
		Runnable task = () -> {
			receive();
		};
		Thread thread = new Thread(task);
		thread.start();
		sendMessage("CONM1111", "Test");
		sendMessage("MCGM1111", "Test");
		sendMessage("MONM1111", "Test");
	}
	
	private static void receive() {
		MulticastSocket aSocket = null;
		try {

			aSocket = new MulticastSocket(1412);

			aSocket.joinGroup(InetAddress.getByName("230.1.1.10"));

			byte[] buffer = new byte[1000];
			System.out.println("Concordia UDP Server 1412 Started............");

			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);

				String sentence = new String( request.getData(), 0,
						request.getLength() );
				String[] parts = sentence.split(";");
				if(parts[1].equals("crush")||parts[1].equals("fault")||parts[1].equals("rfault")) {
					if(parts[1].equals("crush")) {
						crushhandle(parts[0]);
					}else if(parts[1].equals("rfault")) {
						rfaultHandle(parts[0]);
					}
					else {
						faultHandle(parts[0]);
					}
				}else {
					int sequencerId = Integer.parseInt(parts[6]);
					Message message = new Message(sentence,sequencerId);
					pq.add(message);
					findNextMessage();
				}
				DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(),
						request.getPort());
				aSocket.send(reply);
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
	
	public static void findNextMessage() {
		Iterator<Message> itr = pq.iterator(); 
		while (itr.hasNext()) {
			Message request = itr.next();
			if(request.getsequenceId()==nextSequence) {
				nextSequence = nextSequence+1;
				String message = request.getMessage();
				String[] parts = message.split(";");
				String userID = parts[1]; 
				
				System.out.println(message);

				sendMessage(userID,message);
				
			}
		} 			 
	}
	
	public static void rfaultHandle(String message) {
		if(message.equals("21")) {
			con_fault=0;
		}else if (message.equals("22")) {
			mcg_fault=0;
		}else if (message.equals("23")) {
			mon_fault=0;
		}
	}
	
	public static void faultHandle(String message) {
		if(message.equals("21")) {
			con_fault++;
		}else if (message.equals("22")) {
			mcg_fault++;
		}else if (message.equals("23")) {
			mon_fault++;
		}
		if(con_fault>2) {
			sendMessage("CONM1111" , "fault");
		}else if(mcg_fault>2) {
			sendMessage("MCGM1111" , "fault");
		}else if(mon_fault>2) {
			sendMessage("MONM1111" , "fault");
		}
	}
	
	public static void crushhandle(String message){
		if(message.equals("21")) {
			try {
				Server.main(new String[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (message.equals("22")) {
			try {
				Server.main(new String[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (message.equals("23")) {
			try {
				Server.main(new String[0]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void sendMessage(String userID , String message) {
		String libraryPrefix = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();
		int port=8887;
		if(libraryPrefix.equals("con")) {
			port = 8887;
		}else if(libraryPrefix.equals("mcg")) {
			port = 7776;
		}else if(libraryPrefix.equals("mon")) {
			port = 6667;
		}

		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] messageByte = message.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(messageByte, messageByte.length, aHost, port);
			aSocket.send(request);
			System.out.println(message);
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}

	}
}