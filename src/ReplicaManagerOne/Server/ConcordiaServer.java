package ReplicaManagerOne.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.PriorityQueue;

import ReplicaManagerOne.ImplementRemoteInterface.ConcordiaClass;
import ReplicaManagerOne.Map.Message;
import ReplicaManagerOne.Map.MessageComparator;



public class ConcordiaServer {
	public static PriorityQueue<Message> pq = new PriorityQueue<Message>(20, new MessageComparator()); 
	public static ConcordiaClass concordiaObjecct;
	public static int nextSequence = 1;
	public static void main(String args[]) throws Exception
	{

		ConcordiaClass obj = new ConcordiaClass();
		concordiaObjecct =obj;

		System.out.println("Concordia Server ready and waiting ...");
		Runnable task = () -> {
			receive(obj);
		};
		Thread thread = new Thread(task);
		thread.start();

		Runnable task2 = () -> {
			receiveFromSequencer(pq);
		};
		Thread thread2 = new Thread(task2);
		thread2.start();

		System.out.println("ConcordiaServer Exiting ...");

	}





	private static void receive(ConcordiaClass obj) {
		DatagramSocket aSocket = null;
		String sendingResult = "";
		try {
			aSocket = new DatagramSocket(8888);
			byte[] buffer = new byte[1000];
			System.out.println("Concordia UDP Server 8888 Started............");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String sentence = new String( request.getData(), 0,
						request.getLength() );
				String[] parts = sentence.split(";");
				String function = parts[0]; 
				String userID = parts[1]; 
				String itemName = parts[2]; 
				String itemId = parts[3]; 
				int numberOfDays = Integer.parseInt(parts[4]); 
				if(function.equals("borrow")) {
					boolean result = obj.borrowItem(userID, itemId, numberOfDays);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("find")) {
					String result = obj.findItem(userID, itemName);
					sendingResult = result;
					sendingResult= sendingResult+";";
				}else if(function.equals("return")) {
					boolean result = obj.returnItem(userID, itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("wait")) {
					boolean result = obj.waitInQueue(userID, itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("isAvailableInLibrary")) {
					boolean result = obj.isAvailableInLibrary(itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("isBorrowed")) {
					boolean result = obj.isBorrowed(userID,itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("isAlreadyBorrowed")) {
					boolean result = obj.isAlreadyBorrowed(userID);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}
				byte[] sendData = sendingResult.getBytes();
				DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
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


	private static void receiveFromSequencer(PriorityQueue<Message> pq) {
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
				//				String function = parts[0]; 
				//				String userID = parts[1]; 
				//				String itemName = parts[2]; 
				//				String itemId = parts[3]; 
				//				String newItemId = parts[4];
				//				int number = Integer.parseInt(parts[6]);
				int sequencerId = Integer.parseInt(parts[6]);
				Message message = new Message(sentence,sequencerId);
				pq.add(message);
				findNextMessage();
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
				nextSequence = pq.poll().getsequenceId()+1;
				String message = request.getMessage();
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
					sendingResult = concordiaObjecct.addItem(userID,itemId, itemName,number);
					sendingResult= sendingResult+";";
				}else if(function.equals("removeItem")) {
					String result = concordiaObjecct.removeItem(userID, itemId,number);
					sendingResult = result;
					sendingResult= sendingResult+";";
				}else if(function.equals("listItemAvailability")) {
					String result = concordiaObjecct.listItemAvailability(userID);
					sendingResult = result;
					sendingResult= sendingResult+";";
				}else if(function.equals("borrowItem")) {
					boolean result = concordiaObjecct.borrowItem(userID, itemId,number);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("findItem")) {
					sendingResult = concordiaObjecct.findItem(userID,itemName);
					
					sendingResult= sendingResult+";";
				}else if(function.equals("returnItem")) {
					boolean result = concordiaObjecct.returnItem(userID,itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("waitInQueue")) {
					boolean result = concordiaObjecct.waitInQueue(userID,itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}else if(function.equals("exchangeItem")) {
					boolean result = concordiaObjecct.exchangeItem(userID,newItemId,itemId);
					sendingResult = Boolean.toString(result);
					sendingResult= sendingResult+";";
				}
				
				sendMessageBackToFrontend(sendingResult);
				
			}
		} 			 
	}
	
	public static void sendMessageBackToFrontend(String message) {
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
