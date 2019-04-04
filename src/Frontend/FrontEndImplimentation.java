package Frontend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;

import org.omg.CORBA.ORB;

import ServerObjectInterfaceApp.ServerObjectInterfacePOA;

public class FrontEndImplimentation extends ServerObjectInterfacePOA{
	private ORB orb;
	private ArrayList<MessageInfo> responses = new ArrayList<MessageInfo>();
	public void setORB(ORB orb_val) {
		orb = orb_val;
	}
	public String addItem(String managerId, String itemID, String itemName, int quantity){
		sendMessage("addItem", managerId, itemName, itemID, null ,quantity);

		waitForResponse();
		String response =findMessages("addItem", managerId, itemName, itemID, null ,quantity);

		//boolean finalMessages = Boolean.parseBoolean(response);	
		return response;
	}

	public String removeItem(String managerID, String itemID, int quantity){
		sendMessage("removeItem", managerID, null, itemID, null ,quantity);

		waitForResponse();
		String response =findMessages("removeItem", managerID, null, itemID, null ,quantity);

		//boolean finalMessages = Boolean.parseBoolean(response);
		return response;
	}

	public String listItemAvailability(String managerID) {
		sendMessage("listItemAvailability", managerID, null, null, null ,0);

		waitForResponse();
		String response =findMessages("listItemAvailability", managerID, null, null, null ,0);

		//boolean finalMessages = Boolean.parseBoolean(response);
		return response;
	}

	public boolean borrowItem(String userID,String itemID,int numberOfDay) {
		sendMessage("borrowItem", userID, null, itemID, null ,numberOfDay);


		waitForResponse();
		String response =findMessages("borrowItem", userID, null, itemID, null ,numberOfDay);

		boolean finalMessages = Boolean.parseBoolean(response);
		return finalMessages;
	}

	public String findItem(String userID, String itemName) {
		sendMessage("findItem", userID, itemName, null, null ,0);


		waitForResponse();
		String response =findMessages("findItem", userID, itemName, null, null ,0);

		//boolean finalMessages = Boolean.parseBoolean(response);
		return response;
	}

	public boolean returnItem(String userID, String itemID) {
		sendMessage("returnItem", userID, null, itemID, null ,0);


		waitForResponse();
		String response =findMessages("returnItem", userID, null, itemID, null ,0);

		boolean finalMessages = Boolean.parseBoolean(response);
		return finalMessages;
	}
	public boolean waitInQueue(String userID,String itemID) {
		sendMessage("waitInQueue", userID, null, itemID, null ,0);


		waitForResponse();
		String response =findMessages("waitInQueue", userID, null, itemID, null ,0);

		boolean finalMessages = Boolean.parseBoolean(response);
		return finalMessages;
	}

	public boolean exchangeItem(String userID,String newItemID,String oldItemID){
		sendMessage("exchangeItem", userID, null, oldItemID, newItemID ,0);


		waitForResponse();
		String response =findMessages("exchangeItem", userID, null, oldItemID, newItemID ,0);

		boolean finalMessages = Boolean.parseBoolean(response);
		return finalMessages;
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

	public void waitForResponse(){

		try {
            System.out.println("waiting for result...");
            Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addMessage(MessageInfo messageinfo) {
		responses.add(messageinfo);
	}

	public ArrayList<MessageInfo> getResponses() {
		return responses;
	}
	public String findMessages(String function,String userID,String itemName, String itemId, String newItem, int number) {
		ArrayList<MessageInfo> finalMessages = new ArrayList<MessageInfo>();
		String message = "false";
		Iterator<MessageInfo> itr = responses.iterator();
        System.out.println("check total response number ");
        System.out.println("response size "+responses.size());
		while (itr.hasNext()) {
            System.out.println("has next ");
			MessageInfo request = itr.next();
			if(request.getFunction().equals(function) && request.getUserID().equals(userID) && request.getSequenceId()!= 0) {
				finalMessages.add(request);
			}
		}
		if(finalMessages.size()==3) {
            System.out.println("packet = 3");
			if(finalMessages.get(0).getResponse().equals(finalMessages.get(1).getResponse()) && finalMessages.get(0).getResponse().equals(finalMessages.get(2).getResponse()) && finalMessages.get(1).getResponse().equals(finalMessages.get(2).getResponse())) {
				message = finalMessages.get(0).getResponse();
				sendFaultMessage(finalMessages.get(0).getRMNo()+";rfault");
				sendFaultMessage(finalMessages.get(1).getRMNo()+";rfault");
				sendFaultMessage(finalMessages.get(2).getRMNo()+";rfault");
			}else if(finalMessages.get(0).getResponse().equals(finalMessages.get(1).getResponse())) {
				message = finalMessages.get(0).getResponse();
				sendFaultMessage(finalMessages.get(0).getRMNo()+";rfault");
				sendFaultMessage(finalMessages.get(1).getRMNo()+";rfault");
				sendFaultMessage(finalMessages.get(2).getRMNo()+";fault");
			}else if(finalMessages.get(0).getResponse().equals(finalMessages.get(2).getResponse())){
				message = finalMessages.get(0).getResponse();
				sendFaultMessage(finalMessages.get(0).getRMNo()+";rfault");
				sendFaultMessage(finalMessages.get(2).getRMNo()+";rfault");
				sendFaultMessage(finalMessages.get(1).getRMNo()+";fault");
			}else {
				message = finalMessages.get(1).getResponse();
				sendFaultMessage(finalMessages.get(0).getRMNo()+";fault");
				sendFaultMessage(finalMessages.get(1).getRMNo()+";rfault");
				sendFaultMessage(finalMessages.get(2).getRMNo()+";rfault");
			}
			
		}else if(finalMessages.size()==2){
            System.out.println("packer = 2");
			if(finalMessages.get(0).getResponse().equals(finalMessages.get(1).getResponse())){
				String campus = finalMessages.get(0).getUserID().substring(0, Math.min(userID.length(), 3)).toLowerCase();
				message = finalMessages.get(1).getResponse();
				if(campus.equals("con")) {
                    System.out.println("found conServer crash in fe");
					if((finalMessages.get(0).getRMNo()==11 || finalMessages.get(0).getRMNo()==21) && (finalMessages.get(1).getRMNo()==11 || finalMessages.get(1).getRMNo()==21)) {
						sendFaultMessage(31+";crush");
					}else if((finalMessages.get(0).getRMNo()==11 || finalMessages.get(0).getRMNo()==31) && (finalMessages.get(1).getRMNo()==11 || finalMessages.get(1).getRMNo()==31)) {
						sendFaultMessage(21+";crush");
					}else if((finalMessages.get(0).getRMNo()==21 || finalMessages.get(0).getRMNo()==31) && (finalMessages.get(1).getRMNo()==21 || finalMessages.get(1).getRMNo()==31)) {
						sendFaultMessage(11+";crush");
					}
				}else if(campus.equals("mcg")) {
					if((finalMessages.get(0).getRMNo()==12 || finalMessages.get(0).getRMNo()==22) && (finalMessages.get(1).getRMNo()==12 || finalMessages.get(1).getRMNo()==22)) {
						sendFaultMessage(32+";crush");
					}else if((finalMessages.get(0).getRMNo()==12 || finalMessages.get(0).getRMNo()==32) && (finalMessages.get(1).getRMNo()==12 || finalMessages.get(1).getRMNo()==32)) {
						sendFaultMessage(22+";crush");
					}else if((finalMessages.get(0).getRMNo()==22 || finalMessages.get(0).getRMNo()==32) && (finalMessages.get(1).getRMNo()==22 || finalMessages.get(1).getRMNo()==32)) {
						sendFaultMessage(12+";crush");
					}
					
				}else if(campus.equals("mon")) {
					if((finalMessages.get(0).getRMNo()==13 || finalMessages.get(0).getRMNo()==23) && (finalMessages.get(1).getRMNo()==13 || finalMessages.get(1).getRMNo()==23)) {
						sendFaultMessage(33+";crush");
					}else if((finalMessages.get(0).getRMNo()==13 || finalMessages.get(0).getRMNo()==33) && (finalMessages.get(1).getRMNo()==13 || finalMessages.get(1).getRMNo()==33)) {
						sendFaultMessage(23+";crush");
					}else if((finalMessages.get(0).getRMNo()==23 || finalMessages.get(0).getRMNo()==33) && (finalMessages.get(1).getRMNo()==13 || finalMessages.get(1).getRMNo()==33)) {
						sendFaultMessage(13+";crush");
					}
				}
			}
		}

		responses.removeAll(finalMessages);
		return message;
	}
	
	public static void sendFaultMessage(String message) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] messages = message.getBytes();
			InetAddress aHost = InetAddress.getByName("230.1.1.10");

			DatagramPacket request = new DatagramPacket(messages, messages.length, aHost, 1412);
			aSocket.send(request);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// implement shutdown() method
	public void shutdown() {
		orb.shutdown(false);
	}
}