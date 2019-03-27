package Frontend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.ArrayList;

import org.omg.CORBA.ORB;

import ServerObjectInterfaceApp.ServerObjectInterfacePOA;

public class FrontEndImplimentation extends ServerObjectInterfacePOA{
	private ORB orb;

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}
	public String addItem(String managerId, String itemID, String itemName, int quantity){
		ArrayList<String> message = new ArrayList();
		sendMessage("addItem", managerId, itemName, itemID, null ,quantity);
		
//		while(true) {
//			if(message.size()==3) {
//				break;
//			}
//		}
		return "Message";
	}

	public String removeItem(String managerID, String itemID, int quantity){
		ArrayList<String> message = new ArrayList();
		sendMessage("removeItem", managerID, null, itemID, null ,quantity);
		
//		while(true) {
//			if(message.size()==3) {
//				break;
//			}
//		}
		return "Book is removed";
	}

	public String listItemAvailability(String managerID) {
		ArrayList<String> message = new ArrayList();
		sendMessage("listItemAvailability", managerID, null, null, null ,0);
		
//		while(true) {
//			if(message.size()==3) {
//				break;
//			}
//		}
		return "";
	}

	public boolean borrowItem(String userID,String itemID,int numberOfDay) {
		ArrayList<String> message = new ArrayList();
		sendMessage("borrowItem", userID, null, itemID, null ,numberOfDay);
		
//		while(true) {
//			if(message.size()==3) {
//				break;
//			}
//		}
		return true;
	}

	public String findItem(String userID, String itemName) {
		ArrayList<String> message = new ArrayList();
		sendMessage("findItem", userID, itemName, null, null ,0);
		
//		while(true) {
//			if(message.size()==3) {
//				break;
//			}
//		}
		return "";
	}

	public boolean returnItem(String userID, String itemID) {
		ArrayList<String> message = new ArrayList();
		sendMessage("returnItem", userID, null, itemID, null ,0);
		
//		while(true) {
//			if(message.size()==3) {
//				break;
//			}
//		}
		return true;
	}
	public boolean waitInQueue(String userID,String itemID) {
		ArrayList<String> message = new ArrayList();
		sendMessage("waitInQueue", userID, null, itemID, null ,0);
		
//		while(true) {
//			if(message.size()==3) {
//				break;
//			}
//		}
		return true;
	}

	public boolean exchangeItem(String userID,String newItemID,String oldItemID){
		ArrayList<String> message = new ArrayList();
		sendMessage("exchangeItem", userID, null, oldItemID, newItemID ,0);
		
//		while(true) {
//			if(message.size()==3) {
//				break;
//			}
//		}
		return true;
	}

	public void sendMessage(String function,String userID,String itemName, String itemId, String newItem, int number) {
		DatagramSocket aSocket = null;
		String dataFromClient = function+";"+userID+";"+itemName+";"+itemId+";"+newItem+";"+number+";";
		try {
			aSocket = new DatagramSocket();
			byte[] message = dataFromClient.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, dataFromClient.length(), aHost, 1333);
			aSocket.send(request);
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
	

	public static void receive() {
		MulticastSocket aSocket = null;
		try {

			aSocket = new MulticastSocket(1413);

			aSocket.joinGroup(InetAddress.getByName("230.1.1.5"));

			byte[] buffer = new byte[1000];
			System.out.println("Server Started............");

			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String sentence = new String( request.getData(), 0,
						request.getLength() );
				System.out.println(sentence);
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
	
	// implement shutdown() method
	public void shutdown() {
		orb.shutdown(false);
	}
}