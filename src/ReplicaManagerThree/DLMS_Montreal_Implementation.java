package ReplicaManagerThree;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;


public class DLMS_Montreal_Implementation
{
    private ArrayList<String> arraylist = new ArrayList<>();
    private Map<String, Books> bookmapping = new HashMap<>();
    private Map<String,ArrayList<String>> bookmapping2 = new HashMap<>();
    private Map<String,ArrayList<String>> bookmapping3 = new HashMap<>();

    public DLMS_Montreal_Implementation() throws Exception
    {
        super();
        bookmapping.put("MON1010",new Books("MON1010", "Distributed System", 20));
        bookmapping.put("MON1011",new Books("MON1011", "Absoulate Java", 22));
        bookmapping.put("MON1012",new Books("MON1012", "Data Structure", 10));

    }


    public String addItem(String managerID, String itemID, String itemName, int itemQuantity) 
    {
        String message = null;
        if(bookmapping.containsKey(itemID))
        {
            if(itemName.equals(bookmapping.get(itemID).getItemName()))
            {
                int changeQty = bookmapping.get(itemID).getQuantity();
                changeQty = changeQty+itemQuantity;
                bookmapping.get(itemID).setQuantity(changeQty);
                message = "Books added Successfully.";
            }
            else
            {
                message = "Enter the Correct book name.";
            }
        }
        else
        {
            bookmapping.put(itemID,new Books(itemID, itemName, itemQuantity) );
            message = "Books added Successfully.";
        }
        issuewaiting(itemID);
        serverlog(managerID,itemID,"Adding Books",message);
        return message;
    }

    public String removeItem(String managerID, String itemID, int itemQuantity) 
    {
        String message = null;
        if(bookmapping.containsKey(itemID))
        {
        	if(itemQuantity==-1) {
        		bookmapping.remove(itemID);
                try
                {
                    for (Map.Entry<String, ArrayList<String>> entry : bookmapping2.entrySet())
                    {
                        String key = entry.getKey();
                        ArrayList<String> value = entry.getValue();
                        for(String aString : value)
                        {
                            if(aString.equals(itemID))
                            {
                                bookmapping2.get(key).remove(aString);
                            }
                        }
                    }
                }
                catch (Exception e)
                { }
                if(bookmapping3.containsKey(itemID))
                {
                    bookmapping3.remove(itemID);
                }
                message = "All Books Removed successfully.";
        	}else {
        		if(bookmapping.get(itemID).getQuantity() != 0 && bookmapping.get(itemID).getQuantity() >= itemQuantity)
                {
                    bookmapping.get(itemID).setQuantity(bookmapping.get(itemID).getQuantity() - itemQuantity);
                    message = " "+itemQuantity+" Books successfully Removed";
                }
                else
                {
                    message = "Only " +  bookmapping.get(itemID).getQuantity() + " Books available.";
                }
        	}

        }
        else
        {
            message = "No Book Found.";
        }
        serverlog(managerID,itemID,"Removing Books",message);
        return message;
    }

    public String listItemAvailability(String managerID) 
    {
        serverlog(managerID,"Listing Available Books.","Listing Available Books.","All Books Printed");
        return bookmapping.toString();
    }

    public boolean borrowItem(String userID, String itemID, int numberOfDays) 
    {
        boolean message = false;
        String itemidcon = itemID.substring(0,3);
        if(bookmapping.containsKey(itemID))
        {
            if(bookmapping.get(itemID).getQuantity() != 0)
            {
                String userid = userID.substring(0, 3);
                if (userid.equals("MON"))
                {
                    if (bookmapping2.containsKey(userID))
                    {
                        arraylist = bookmapping2.get(userID);
                        if (arraylist.contains(itemID))
                        {
                            message = false;

                        }
                        else
                        {
                            arraylist.add(itemID);
                            bookmapping.get(itemID).setQuantity(bookmapping.get(itemID).getQuantity() - 1);
                            message = true;
                        }
                    }
                    else
                    {
                        bookmapping.get(itemID).setQuantity(bookmapping.get(itemID).getQuantity() - 1);
                        userBorrow(userID, itemID);
                        message = true;
                    }
                }
                else
                {
                    if (bookmapping2.containsKey(userID))
                    {
                        message = false;
                    }
                    else
                    {
                        bookmapping.get(itemID).setQuantity(bookmapping.get(itemID).getQuantity() - 1);
                        userBorrow(userID, itemID);
                        message = true;
                    }
                }
            }
            else
            {
                message= false;
            }
        }
        else if(itemidcon.equals("CON"))
        {
            String sendmessage = 0+":"+userID+":"+itemID+":";
            try
            {
                DatagramSocket MontrealSocket = new DatagramSocket();

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress ServerAddress = InetAddress.getByName("localhost");
                sendData = sendmessage.getBytes();

                DatagramPacket ConSendPacket = new DatagramPacket(sendData, sendmessage.length(), ServerAddress, 9876);
                MontrealSocket.send(ConSendPacket);

                DatagramPacket ConReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                MontrealSocket.receive(ConReceivePacket);
                String messages = new String(ConReceivePacket.getData());

                String[] parts = messages.split(":");
                String part1 = parts[0];

                message = Boolean.parseBoolean(part1);

                MontrealSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error:" + e);
            }
        }
        else if(itemidcon.equals("MCG"))
        {
            String sendmessage = 0+":"+userID+":"+itemID+":";
            try
            {
                DatagramSocket MontrealSocket = new DatagramSocket();

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress ServerAddress = InetAddress.getByName("localhost");
                sendData = sendmessage.getBytes();

                DatagramPacket McgSendPacket = new DatagramPacket(sendData, sendmessage.length(), ServerAddress, 9877);
                MontrealSocket.send(McgSendPacket);

                DatagramPacket McgReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                MontrealSocket.receive(McgReceivePacket);
                String messages = new String(McgReceivePacket.getData());

                String[] parts = messages.split(":");
                String part1 = parts[0];
                message = Boolean.parseBoolean(part1);

                MontrealSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error:" + e);
            }
        }
        else
        {
            message = false;
        }
        serverlog(userID,itemID,"Borrowing Books",""+message);
        return message;
    }

    public boolean returnItem(String userID, String itemID)
    {

        boolean message = false;
        String itemidcon = itemID.substring(0,3);
        if(bookmapping.containsKey(itemID))
        {
            if(bookmapping2.containsKey(userID))
            {
                arraylist = bookmapping2.get(userID);
                if(arraylist.contains(itemID))
                {
                    arraylist.remove(itemID);
                    if(bookmapping2.get(userID).isEmpty())
                    {
                        bookmapping2.remove(userID);
                    }
                    bookmapping.get(itemID).setQuantity(bookmapping.get(itemID).getQuantity() + 1);
                    message = true;

                }
                else
                {
                    message = false;
                }
            }
            else
            {
                message = false;
            }

        }
        else if(itemidcon.equals("MCG"))
        {
            String sendmessage = 1+":"+userID+":"+itemID+":";
            try
            {
                DatagramSocket MontrealSocket = new DatagramSocket();

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress ServerAddress = InetAddress.getByName("localhost");
                sendData = sendmessage.getBytes();


                //McGhill Server
                DatagramPacket McgSendPacket = new DatagramPacket(sendData, sendmessage.length(), ServerAddress, 9877);
                MontrealSocket.send(McgSendPacket);

                DatagramPacket McgReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                MontrealSocket.receive(McgReceivePacket);
                String messages = new String(McgReceivePacket.getData());

				String[] parts = messages.split(":");
				String part1 = parts[0];

				message = Boolean.parseBoolean(part1);
                MontrealSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error:" + e);
            }
        }
        else if(itemidcon.equals("CON"))
        {
            String sendmessage = 1+":"+userID+":"+itemID+":";
            try
            {
                DatagramSocket MontrealSocket = new DatagramSocket();

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress ServerAddress = InetAddress.getByName("localhost");
                sendData = sendmessage.getBytes();

                DatagramPacket ConSendPacket = new DatagramPacket(sendData, sendmessage.length(), ServerAddress, 9876);
                MontrealSocket.send(ConSendPacket);

                DatagramPacket ConReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                MontrealSocket.receive(ConReceivePacket);
                String messages = new String(ConReceivePacket.getData());
                
				String[] parts = messages.split(":");
				String part1 = parts[0];

				message = Boolean.parseBoolean(part1);
                MontrealSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error:" + e);
            }
        }
        else
        {
            message = false;
        }
        issuewaiting(itemID);
        serverlog(userID,itemID,"Returning Books",""+message);
        return message;
    }
    public String findItem(String UserID, String itemName) 
    {
        String message = null;
        String con_message = null;
        String mcg_message = null;
        String mon_message = null;
        try
        {
            DatagramSocket MontrealSocket = new DatagramSocket();

            InetAddress ServerAddress = InetAddress.getByName("localhost");

            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];

            String mon_client_msg = (2+":"+UserID+":"+itemName+":");
            sendData = mon_client_msg.getBytes();

            //Concordia Server
            DatagramPacket ConSendPacket = new DatagramPacket(sendData, mon_client_msg.length(), ServerAddress, 9876);
            MontrealSocket.send(ConSendPacket);

            DatagramPacket ConReceivePacket = new DatagramPacket(receiveData, receiveData.length);
            MontrealSocket.receive(ConReceivePacket);
            con_message = new String(ConReceivePacket.getData());

            //McGhill Server
            DatagramPacket McgSendPacket = new DatagramPacket(sendData, mon_client_msg.length(), ServerAddress, 9877);
            MontrealSocket.send(McgSendPacket);

            DatagramPacket McgReceivePacket = new DatagramPacket(receiveData, receiveData.length);
            MontrealSocket.receive(McgReceivePacket);
            mcg_message = new String(McgReceivePacket.getData());

            //Montreal Server
            DatagramPacket MonSendPacket = new DatagramPacket(sendData, mon_client_msg.length(), ServerAddress, 9878);
            MontrealSocket.send(MonSendPacket);

            DatagramPacket MonReceivePacket = new DatagramPacket(receiveData, receiveData.length);
            MontrealSocket.receive(MonReceivePacket);
            mon_message = new String(MonReceivePacket.getData());

            message = con_message+mcg_message+mon_message;
            MontrealSocket.close();
        }
        catch (Exception e)
        {
            System.out.println("Error:" + e);
        }
        serverlog(UserID,itemName,"Borrowing Books",message);
        return message;
    }

	public String userBorrow (String userid, String bookid) 
	{
		String message = null;
		if(bookmapping2.containsKey(userid))
		{
			bookmapping2.get(userid).add(bookid);
		}
		else
		{
			bookmapping2.put(userid, new ArrayList<>());
			arraylist = bookmapping2.get(userid);
			arraylist.add(bookid);
		}
		return message;
	}

    public String findBook(String userid,String bookname) 
    {
        String message = ":Not Available.:"+0+":";
        for(Map.Entry<String, Books> entry: bookmapping.entrySet())
        {
            if(entry.getValue().getItemName().equals(bookname))
            {
                System.out.println("The Book "+bookname +" is available. Book ID: "+entry.getValue().getItemID()+" Quantity Available: "+entry.getValue().getQuantity());
                message = ":"+entry.getValue().getItemID()+":"+entry.getValue().getQuantity()+":";
            }
        }
        return message;
    }

    public boolean waitInQueue(String bookID, String userID )
    {
        boolean message = false;
        if(bookmapping3.containsKey(bookID))
        {
            String messages = "No.";
            for (Map.Entry<String, ArrayList<String>> entry : bookmapping3.entrySet())
            {
                String key = entry.getKey();
                ArrayList<String> value = entry.getValue();
                for(String aString : value)
                {
                    if(bookID.equals(key) && userID.equals(aString))
                    {
                        messages = "You are already in the Waiting List.";
                    }
                }
            }
            if(messages.equals("No."))
            {
                bookmapping3.get(bookID).add(userID);
                message = true;
            }
        }
        else
        {
            bookmapping3.put(bookID, new ArrayList<>());
            arraylist = bookmapping3.get(bookID);
            arraylist.add(userID);
            message = true;
        }
        return message;
    }

    public void issuewaiting (String bookid)
    {
        if(bookmapping.containsKey(bookid) && bookmapping3.containsKey(bookid))
        {
            try
            {
                if(bookmapping.get(bookid).getQuantity()!=0)
                {
                    while(bookmapping.get(bookid).getQuantity()!=0)
                    {
                        if(bookmapping3.containsKey(bookid))
                        {
                            arraylist = bookmapping3.get(bookid);
                            if(bookmapping2.containsKey(arraylist.get(0)))
                            {
                                bookmapping2.get(arraylist.get(0)).add(bookid);
                            }
                            else
                            {
                                bookmapping2.put(arraylist.get(0), new ArrayList<>());
                                arraylist = bookmapping2.get(arraylist.get(0));
                                arraylist.add(bookid);
                            }
                            arraylist.remove(0);
                            bookmapping.get(bookid).setQuantity(bookmapping.get(bookid).getQuantity()-1);

                        }
                    }
                }
            }
            catch (Exception e)
            {

            }
            if(bookmapping3.get(bookid).isEmpty())
            {
                bookmapping3.remove(bookid);
            }
        }
    }

    public boolean exchangeItem (String userid, String newItem, String oldItem) 
    {
        boolean message= false;
        String oldItemCon = oldItem.substring(0,3);
        String newItemCon = newItem.substring(0,3);

        boolean oldItemCheck = false;
        boolean newItemCheck = false;

        if(oldItemCon.equals("CON"))
        {
            String sendmessage = 4+":"+userid+":"+oldItem+":";
            try
            {
                DatagramSocket concordiaSocket = new DatagramSocket();

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress ServerAddress = InetAddress.getByName("localhost");
                sendData = sendmessage.getBytes();

                DatagramPacket ConSendPacket = new DatagramPacket(sendData, sendmessage.length(), ServerAddress, 9876);
                concordiaSocket.send(ConSendPacket);

                DatagramPacket ConReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                concordiaSocket.receive(ConReceivePacket);
                String messages = new String(ConReceivePacket.getData());
				String[] parts = messages.split(":");
				String part1 = parts[0];

				message = Boolean.parseBoolean(part1);
                concordiaSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error:" + e);
            }

            oldItemCheck = message;
        }

        if(oldItemCon.equals("MCG"))
        {
            String sendmessage = 4+":"+userid+":"+oldItem+":";
            try
            {
                DatagramSocket concordiaSocket = new DatagramSocket();

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress ServerAddress = InetAddress.getByName("localhost");
                sendData = sendmessage.getBytes();

                DatagramPacket McgSendPacket = new DatagramPacket(sendData, sendmessage.length(), ServerAddress, 9877);
                concordiaSocket.send(McgSendPacket);

                DatagramPacket McgReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                concordiaSocket.receive(McgReceivePacket);
                String messages = new String(McgReceivePacket.getData());
				String[] parts = messages.split(":");
				String part1 = parts[0];

				message = Boolean.parseBoolean(part1);
                concordiaSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error:" + e);
            }

            oldItemCheck = message;
        }

        if(oldItemCon.equals("MON"))
        {
            String sendmessage = 4+":"+userid+":"+oldItem+":";
            try
            {
                DatagramSocket concordiaSocket = new DatagramSocket();

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress ServerAddress = InetAddress.getByName("localhost");
                sendData = sendmessage.getBytes();

                DatagramPacket MonSendPacket = new DatagramPacket(sendData, sendmessage.length(), ServerAddress, 9878);
                concordiaSocket.send(MonSendPacket);

                DatagramPacket MonReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                concordiaSocket.receive(MonReceivePacket);
                String messages = new String(MonReceivePacket.getData());
				String[] parts = messages.split(":");
				String part1 = parts[0];

				message = Boolean.parseBoolean(part1);
				
                concordiaSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error:" + e);
            }

            oldItemCheck = message;
        }

        if(newItemCon.equals("CON"))
        {
            String sendmessage = 3+":"+userid+":"+newItem+":";
            try
            {
                DatagramSocket concordiaSocket = new DatagramSocket();

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress ServerAddress = InetAddress.getByName("localhost");
                sendData = sendmessage.getBytes();

                DatagramPacket ConSendPacket = new DatagramPacket(sendData, sendmessage.length(), ServerAddress, 9876);
                concordiaSocket.send(ConSendPacket);

                DatagramPacket ConReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                concordiaSocket.receive(ConReceivePacket);
                String messages = new String(ConReceivePacket.getData());
				String[] parts = messages.split(":");
				String part1 = parts[0];

				message = Boolean.parseBoolean(part1);
                concordiaSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error:" + e);
            }

            newItemCheck = message;
        }

        if(newItemCon.equals("MCG"))
        {
            String sendmessage = 3+":"+userid+":"+newItem+":";
            try
            {
                DatagramSocket concordiaSocket = new DatagramSocket();

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress ServerAddress = InetAddress.getByName("localhost");
                sendData = sendmessage.getBytes();

                DatagramPacket McgSendPacket = new DatagramPacket(sendData, sendmessage.length(), ServerAddress, 9877);
                concordiaSocket.send(McgSendPacket);

                DatagramPacket McgReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                concordiaSocket.receive(McgReceivePacket);
                String messages = new String(McgReceivePacket.getData());
				String[] parts = messages.split(":");
				String part1 = parts[0];

				message = Boolean.parseBoolean(part1);
				
                concordiaSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error:" + e);
            }

            newItemCheck = message;
        }

        if(newItemCon.equals("MON"))
        {
            String sendmessage = 3+":"+userid+":"+newItem+":";
            try
            {
                DatagramSocket concordiaSocket = new DatagramSocket();

                byte[] sendData = new byte[1024];
                byte[] receiveData = new byte[1024];

                InetAddress ServerAddress = InetAddress.getByName("localhost");
                sendData = sendmessage.getBytes();

                DatagramPacket MonSendPacket = new DatagramPacket(sendData, sendmessage.length(), ServerAddress, 9878);
                concordiaSocket.send(MonSendPacket);

                DatagramPacket MonReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                concordiaSocket.receive(MonReceivePacket);
                String messages = new String(MonReceivePacket.getData());
				String[] parts = messages.split(":");
				String part1 = parts[0];

				message = Boolean.parseBoolean(part1);
                concordiaSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Error:" + e);
            }

            newItemCheck = message;
        }

        if(oldItemCheck && newItemCheck)
        {
            returnItem(userid,oldItem);
            borrowItem(userid,newItem,0);
            message = true;
        }
        else
        {
            message = false;
        }
        return message;
    }

    public String exchangeCheck1 (String userid,String bookid)
    {
        String montrealCheck = null;
        String userCon = userid.substring(0,3);

        if(bookmapping.containsKey(bookid) && bookmapping.get(bookid).getQuantity() !=0)
        {
            if(userCon.equals("MON"))
            {
                if(bookmapping2.containsKey(userid))
                {
                    arraylist = bookmapping2.get(userid);
                    if(!arraylist.contains(bookid))
                    {
                        montrealCheck = "True:";
                    }
                    else
                    {
                        montrealCheck = "False";
                    }
                }
            }
            else
            {
                if (!bookmapping2.containsKey(userid))
                {
                    montrealCheck = "True:";
                }
                else if(bookmapping2.containsKey(userid))
				{
					arraylist = bookmapping2.get(userid);
					if(!arraylist.contains(bookid))
					{
						montrealCheck = "True:";
					}
					else
					{
						montrealCheck = "False";
					}
				}
                else
                {
                    montrealCheck = "False";
                }
            }
        }
        else
        {
            montrealCheck = "False:";
        }
        return montrealCheck;
    }

    public String exchangeCheck2 (String userid,String bookid)
    {
        String montrealCheck = null;
        if(bookmapping.containsKey(bookid))
        {
            if(bookmapping2.containsKey(userid))
            {
                arraylist = bookmapping2.get(userid);
                if(arraylist.contains(bookid))
                {
                    montrealCheck = "True:";
                }
                else
                {
                    montrealCheck = "False";
                }
            }
            else
            {
                montrealCheck = "False";
            }
        }
        else
        {
            montrealCheck = "False:";
        }
        return montrealCheck;
    }

    public static void serverlog(String userid, String item, String operation, String server_reply)
    {
        Logger log = Logger.getLogger(DLMS_Montreal_Implementation.class.getName());
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.format(cal.getTime());

        final String dir = System.getProperty("user.dir")+"\\src\\ReplicaManagerThree\\";
        File data_directory = new File(dir, "Montreal_Server_Log");
        data_directory.mkdir();
        File log_file = new File(data_directory, "Montreal_Client_"+userid+".txt");
        try
        {
            log_file.createNewFile();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        try
        {
            FileHandler fh;
            String log_path= dir+"Montreal Server Log\\Montreal_Client_"+userid+".txt";
            fh=new FileHandler(log_path, true);
            log.addHandler(fh);
            log.info("Date");
            log.info(sdf.format(cal.getTime()));
            log.info("Request");
            log.info(operation);
            log.info("Request ID");
            log.info(userid);
            log.info("Request Item");
            log.info(item);
            log.info("Server Response");
            log.info(server_reply);
        }
        catch(Exception e)
        {
        }
    }
}
