package ReplicaManagerTwo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

import ReplicaManagerOne.Map.Message;
import ReplicaManagerOne.Map.MessageComparator;


public class RmTwo {
	public static int nextSequence = 1;
	public static PriorityQueue<Message> pq = new PriorityQueue<Message>(20, new MessageComparator());
	public static ArrayList<Message> pq_list = new ArrayList<Message>(); 
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
				pq_list.add(request);
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
			Runnable task = () -> {
				try {
					// McgillServer.shutDown();
					String[] args = new String[1];
					args[0] = "CON";
					Server.main(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			Thread handleThread = new Thread(task);
			handleThread.start();
			System.out.println("handle MCG server crash!");
			redeploy("CON");
		}else if (message.equals("22")) {
			Runnable task = () -> {
				try {
					// McgillServer.shutDown();
					String[] args = new String[1];
					args[0] = "MCG";
					Server.main(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			Thread handleThread = new Thread(task);
			handleThread.start();
			System.out.println("handle MCG server crash!");
			redeploy("MCG");
		}else if (message.equals("23")) {
			Runnable task = () -> {
				try {
					// MontrealServer.shutDown();
					String[] args = new String[1];
					args[0] = "MON";
					Server.main(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
			Thread handleThread = new Thread(task);
			handleThread.start();
			System.out.println("handle Mon server crash!");
			redeploy("MON");
		}
	}
	public static void redeploy(String system) {
		try {
            Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		system = system.toLowerCase();
		Iterator<Message> itr = pq_list.iterator();
		while (itr.hasNext()) {
			Message request = itr.next();
			String message = request.getMessage();
			String[] parts = message.split(";");
			String function =parts[0];
			String userID = parts[1]; 
			String itemName = parts[2];
			String itemId =parts[3];
			String newItem = parts[4]; 
			String number = parts[5];
			String sequencer = parts[6];
			String preUser = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();

			if(!function.equals("listItemAvailability")||!function.equals("findItem")) {
				String preItem = itemId.substring(0, Math.min(userID.length(), 3)).toLowerCase();
				if(function.equals("addItem") && preUser.equals(system)) {
					String messageSend = function+";"+userID+";"+itemName+";"+itemId+";"+newItem+";"+number+";"+0;
					sendMessage(system,messageSend);
				}else if(function.equals("removeItem") && preUser.equals(system)) {
					String messageSend = function+";"+userID+";"+itemName+";"+itemId+";"+newItem+";"+number+";"+0;
					sendMessage(system,messageSend);
				}else if(function.equals("borrowItem") && preItem.equals(system)) {
					String messageSend = function+";"+userID+";"+itemName+";"+itemId+";"+newItem+";"+number+";"+0;
					sendMessage(system,messageSend);
				}else if(function.equals("returnItem") && preItem.equals(system)) {
					String messageSend = function+";"+userID+";"+itemName+";"+itemId+";"+newItem+";"+number+";"+0;
					sendMessage(system,messageSend);
				}else if(function.equals("waitInQueue") && preItem.equals(system)) {
					String messageSend = function+";"+userID+";"+itemName+";"+itemId+";"+newItem+";"+number+";"+0;
					sendMessage(system,messageSend);
				}else if(function.equals("exchangeItem")) {
					String preNewItem = newItem.substring(0, Math.min(userID.length(), 3)).toLowerCase();
					if(preItem.equals(system) && preNewItem.equals(system)) {
						String messageSend = function+";"+userID+";"+itemName+";"+itemId+";"+newItem+";"+number+";"+0;
						sendMessage(system,messageSend);	
					}else if(!preItem.equals(system) && preNewItem.equals(system)) {
						String messageSend = "borrowItem"+";"+userID+";"+itemName+";"+newItem+";"+newItem+";"+number+";"+0;
						sendMessage(system,messageSend);
					}else if(preItem.equals(system) && !preNewItem.equals(system)) {
						String messageSend = "returnItem"+";"+userID+";"+itemName+";"+itemId+";"+newItem+";"+number+";"+0;
						sendMessage(system,messageSend);
					}

				}
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