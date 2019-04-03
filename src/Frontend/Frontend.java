package Frontend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import ServerObjectInterfaceApp.ServerObjectInterface;
import ServerObjectInterfaceApp.ServerObjectInterfaceHelper;



public class Frontend {

	public static void main(String args[])
	{
		try {
			FrontEndImplimentation obj = new FrontEndImplimentation();
			Runnable task = () -> {
				receive(obj);
			};
			Thread thread = new Thread(task);
			thread.start();
			// create and initialize the ORB //// get reference to rootpoa &amp; activate
			// the POAManager
			ORB orb = ORB.init(args, null);
			// -ORBInitialPort 1050 -ORBInitialHost localhost
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();

			// create servant and register it with the ORB
			obj.setORB(orb);

			// get object reference from the servant
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(obj);
			ServerObjectInterface href = ServerObjectInterfaceHelper.narrow(ref);

			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			NameComponent path[] = ncRef.to_name("frontend");
			ncRef.rebind(path, href);

			System.out.println("frontend Server ready and waiting ...");
			
			// wait for invocations from clients
			for (;;) {
				orb.run();
			}
		}

		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("frontend Exiting ...");

	}
	
	public static void receive(FrontEndImplimentation obj) {
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
				String[] parts = sentence.split(":");
				if(parts.length>2) {
					MessageInfo messageInfo = new MessageInfo();
					messageInfo.setResponse(parts[0]);
					messageInfo.setRMNo(Integer.parseInt(parts[1]));
					String[] partsTwo = parts[2].split(";");
					messageInfo.setMessage(parts[2]);
					messageInfo.setFunction(partsTwo[0]);
					messageInfo.setUserID(partsTwo[1]);
					if(partsTwo[2].equals("null")) {
						partsTwo[2] = null;
					}
					messageInfo.setItemName(partsTwo[2]);
					if(partsTwo[3].equals("null")) {
						partsTwo[3] = null;
					}
					messageInfo.setItemId(partsTwo[3]);
					if(partsTwo[4].equals("null")) {
						partsTwo[4] = null;
					}
					messageInfo.setNewItem(partsTwo[4]);
					
					messageInfo.setNumber(Integer.parseInt(partsTwo[5]));
					messageInfo.setSequenceId(Integer.parseInt(partsTwo[6]));
					obj.addMessage(messageInfo);
					System.out.println("add response ");
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
}
