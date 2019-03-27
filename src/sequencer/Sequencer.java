package sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Sequencer {
	private static int con_sequencerId = 1;
	private static int mon_sequencerId = 1;
	private static int mcg_sequencerId = 1;
	public static void main(String[] args) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(1333);
			byte[] buffer = new byte[1000];
			System.out.println("Sequencer UDP Server 1313 Started............");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String sentence = new String( request.getData(), 0,
						request.getLength() );
				
				String[] parts = sentence.split(";");
				String userID = parts[1]; 
				System.out.println(sentence);
				sendMessage(userID,sentence);
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
	
	public static void sendMessage(String userID , String message) {
		String libraryPrefix = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();
		int port=1412;
		if(libraryPrefix.equals("con")) {
			message = message+con_sequencerId;
			port = 1412;
			con_sequencerId++;
		}else if(libraryPrefix.equals("mcg")) {
			message = message+mcg_sequencerId;
			port = 1410;
			mcg_sequencerId++;
		}else if(libraryPrefix.equals("mon")) {
			message = message+mon_sequencerId;
			port = 1411;
			mon_sequencerId++;
		}
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] messages = message.getBytes();
			InetAddress aHost = InetAddress.getByName("230.1.1.10");

			DatagramPacket request = new DatagramPacket(messages, messages.length, aHost, port);
			aSocket.send(request);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}