package ReplicaManagerOne.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import ReplicaManagerOne.ImplementRemoteInterface.McgillClass;


public class McgillServer {
	
	public static void main(String args[])
	{
		try {
			
			McgillClass obj = new McgillClass();
		
			System.out.println("MC Gill Server ready and waiting ...");
			Runnable task = () -> {
				receive(obj);
			};
			Thread thread = new Thread(task);
			thread.start();

		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("MC Gill Server Exiting ...");

	}

	

	private static void receive(McgillClass obj) {
		DatagramSocket aSocket = null;
		String sendingResult = "";
		try {
			aSocket = new DatagramSocket(7777);
			byte[] buffer = new byte[1000];
			System.out.println("MC Gill UDP Server 7777 Started............");
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
}
