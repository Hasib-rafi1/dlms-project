package ReplicaManagerOne.ImplementRemoteInterface;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import ReplicaManagerOne.Map.BorrowedItem;
import ReplicaManagerOne.Map.Item;
import ReplicaManagerOne.Map.WaitingList;

public class MontrealClass{

	private Map<String, Item> itemsMaps ;
	private Map<String, List<BorrowedItem>> borrowedItems;
	private Map<String, List<WaitingList>> waitingList;
	private int concordiaServerPort = 8888;
	private int mcgillServerPort = 7777;
	private int montrealServerPort = 6666;
	private String libraryPrifix = "mon";

	public MontrealClass() throws Exception{
		super();
		Map<String, Item> itemsMap = new HashMap<>();
		itemsMap.put("MON1010", new Item("MON1010", "Distributed System", 20));
		itemsMap.put("MON1011", new Item("MON1011", "Absoulate Java", 22));
		itemsMap.put("MON1012", new Item("MON1012", "Data Structure", 10));
		Map<String, List<BorrowedItem>> borrowedItems = new HashMap<>();
		Map<String, List<WaitingList>> waitingList = new HashMap<>();
		this.itemsMaps =  itemsMap;
		this.borrowedItems = borrowedItems;
		this.waitingList = waitingList;
	}

	public String addItem(String managerId, String itemID, String itemName, int quantity){
		String itemPrefix = itemID.substring(0, Math.min(itemID.length(), 3)).toLowerCase();
		String managerPrefix = managerId.substring(0, Math.min(managerId.length(), 3)).toLowerCase();
		String userType = managerId.substring(3, Math.min(managerId.length(), 4));
		if(itemPrefix.equals(managerPrefix) && (userType.equals("M") || userType.equals("m"))){
			if(itemsMaps.containsKey(itemID)) {
				itemsMaps.get(itemID).setitemQty(itemsMaps.get(itemID).getitemQty()+ quantity);

				String action = "ADD ITEM "+ itemID;
				try {
					logCreate(managerId, action, "ITEM ADDED: true");
					serverLogCreate(managerId,action,"ITEM ADDED: true", "Success",  "USER ID: "+managerId+"/ Item Id: "+  itemID+"/ Item Name: "+itemName+"/ Quantity: "+quantity);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				lendItemFromWaiting(itemID);
				return "Books added Successfully";
			}else {
				itemsMaps.put(itemID, new Item(itemID, itemName, quantity));
				String action = "ADD ITEM "+ itemID;
				try {
					logCreate(managerId, action, "ITEM ADDED: true");
					serverLogCreate(managerId,action,"ITEM ADDED: true", "Success",  "USER ID: "+managerId+"/ Item Id: "+  itemID+"/ Item Name: "+itemName+"/ Quantity: "+quantity);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				lendItemFromWaiting(itemID);
				return "Books added Successfully"; 
			}
		}
		String action = "ADD ITEM "+ itemID;
		try {
			logCreate(managerId, action, "ITEM CAN't BE ADDED: False");

			serverLogCreate(managerId,action,"ITEM CAN't BE ADDED: False", "Failed",  "USER ID: "+managerId+"/ Item Id: "+  itemID+"/ Item Name: "+itemName+"/ Quantity: "+quantity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Books Can't be added";
	}

	public String removeItem(String managerID, String itemID, int quantity) {
		String itemPrefix = itemID.substring(0, Math.min(itemID.length(), 3)).toLowerCase();

		String managerPrefix = managerID.substring(0, Math.min(managerID.length(), 3)).toLowerCase();

		String userType = managerID.substring(3, Math.min(managerID.length(), 4));

		if(itemPrefix.equals(managerPrefix) && (userType.equals("M") || userType.equals("m"))){
			if(itemsMaps.containsKey(itemID)) {
				int itemLeft = itemsMaps.get(itemID).getitemQty() - quantity;
				if(itemLeft>=0) {
					itemsMaps.get(itemID).setitemQty(itemLeft);
					String action = "REMOVE ITEM "+ itemID;
					try {
						logCreate(managerID, action, "ITEM REMOVED: true");

						serverLogCreate(managerID,action,"ITEM REMOVED: true", "Success",  "USER ID: "+managerID+"/ Item Id: "+  itemID);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return "Book removed successful";
				}
				else{
					//itemsMaps.remove(itemID);
					String action = "REMOVE ITEM "+ itemID;
					try {
						logCreate(managerID, action, "ITEM REMOVED: false");

						serverLogCreate(managerID,action,"ITEM REMOVED: false", "Failed",  "USER ID: "+managerID+"/ Item Id: "+  itemID);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return "Book can't be removed";
				}

			}
		}
		String action = "REMOVE ITEM "+ itemID;
		try {
			logCreate(managerID, action, "ITEM CAN'T BE REMOVED: false");

			serverLogCreate(managerID,action,"ITEM CAN'T BE REMOVED: false", "Failed",  "USER ID: "+managerID+"/ Item Id: "+  itemID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Book can't be removed";
	}

	public String listItemAvailability(String managerID){
		String action = "List of Items ";
		try {
			logCreate(managerID, action, itemsMaps.toString());

			serverLogCreate(managerID,action,itemsMaps.toString(), "Success",  "USER ID: "+managerID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return itemsMaps.toString();

	}

	public boolean borrowItem(String userID,String itemID,int numberOfDay) {
		String itemPrefix = itemID.substring(0, Math.min(itemID.length(), 3)).toLowerCase();

		String userPrefix = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();
		String userType = userID.substring(3, Math.min(userID.length(), 4));
		if(libraryPrifix.equals(itemPrefix) && (userType.equals("U") || userType.equals("u"))){
			if(itemsMaps.containsKey(itemID)) {
				if(itemsMaps.get(itemID).getitemQty()>0) {
					if(borrowedItems.containsKey(userID)) {
						if(userPrefix.equals(libraryPrifix)) {
							List<BorrowedItem> items = borrowedItems.get(userID);
							if(!items.stream().filter(o -> o.getItemId().equals(itemID)).findFirst().isPresent()) {
								items.add(new BorrowedItem(itemID,numberOfDay));
								borrowedItems.replace(userID,items);
								//borrowedItems.get(userID).indexOf(itemID);
								itemsMaps.get(itemID).setitemQty(itemsMaps.get(itemID).getitemQty() -1);
								String action = "Borrowed an Item: "+itemID;
								try {
									logCreate(userID, action, "true");

									serverLogCreate(userID,action,"true", "Success",  "USER ID: "+userID+"/ Item ID: "+itemID+"/ Number OF Day: "+ numberOfDay);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return true;
							}
							else {
								return false;
							}

						}
					}
					else {
						List<BorrowedItem> items = new ArrayList<>();
						items.add(new BorrowedItem(itemID,numberOfDay));
						borrowedItems.put(userID, items);
						itemsMaps.get(itemID).setitemQty(itemsMaps.get(itemID).getitemQty() -1);
						String action = "Borrowed an Item: "+itemID;
						try {
							logCreate(userID, action, "true");

							serverLogCreate(userID,action,"true", "Success",  "USER ID: "+userID+"/ Item ID: "+itemID+"/ Number OF Day: "+ numberOfDay);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return true;
					}

				}
			}else {
				String action = "Borrowed an Item: "+itemID;
				try {
					logCreate(userID, action, "false");

					serverLogCreate(userID,action,"false", "Failed",  "USER ID: "+userID+"/ Item ID: "+itemID+"/ Number OF Day: "+ numberOfDay);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}
		}
		else if((userType.equals("U") || userType.equals("u"))){
			if(itemPrefix.equals("mcg")) {
				String result = sendMessage(mcgillServerPort,"borrow", userID, null,  itemID,  numberOfDay);
				System.out.println(result);
				return Boolean.parseBoolean(result);
			}else if(itemPrefix.equals("mon")) {
				String result = sendMessage(montrealServerPort,"borrow", userID, null,  itemID,  numberOfDay);
				return Boolean.parseBoolean(result);
			}else if(itemPrefix.equals("con")) {
				String result = sendMessage(concordiaServerPort,"borrow", userID, null,  itemID,  numberOfDay);
				return Boolean.parseBoolean(result);
			}
		}
		String action = "Borrowed an Item: "+itemID;
		try {
			logCreate(userID, action, "false");
			serverLogCreate(userID,action,"false", "Failed",  "USER ID: "+userID+"/ Item ID: "+itemID+"/ Number OF Day: "+ numberOfDay);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public String findItem(String userID, String itemName) {

		String itemList = "No items are available";
		for (Map.Entry<String, Item> entry : itemsMaps.entrySet()) {
			System.out.println(itemName);
			String name = entry.getValue().getitemName();
			if(name.equalsIgnoreCase(itemName)) {
				String action = "find an Item: "+itemName;
				try {
					logCreate(userID, action, entry.toString());
					serverLogCreate(userID,action, entry.toString(), "Success",  "USER ID: "+userID+"/ Item Name: "+itemName);
				} catch (IOException e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				itemList = entry.toString();

			}

		}
		String userPrefix = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();
		if(userPrefix.equals(libraryPrifix)) {
			if(libraryPrifix.equals("mcg")) {
				String resultMon = sendMessage(montrealServerPort,"find", userID, itemName,  null,  0);
				String resultCon = sendMessage(concordiaServerPort,"find", userID, itemName,  null,  0);
				itemList =  "Concordia "+resultCon+" Montreal "+resultMon+" McGill "+itemList;
			}else if(libraryPrifix.equals("mon")) {
				String resultMcgill = sendMessage(mcgillServerPort,"find", userID, itemName,  null,  0);
				String resultCon = sendMessage(concordiaServerPort,"find", userID, itemName,  null,  0);
				itemList =  "Concordia "+resultCon+" Montreal "+itemList+" McGill "+resultMcgill;
			}else if(libraryPrifix.equals("con")) {
				String resultMcgill = sendMessage(mcgillServerPort,"find", userID, itemName,  null,  0);
				String resultMon = sendMessage(montrealServerPort,"find", userID, itemName,  null,  0);

				itemList = "Concordia "+itemList+" Montreal "+resultMon+" McGill "+resultMcgill;
			}
		}

		return itemList;
	}

	public String findBorrowedItems(String userID)  {



		if(borrowedItems.containsKey(userID)) {
			return borrowedItems.get(userID).toString();
		}

		return "No items Found.";
	}

	public String findWaitingItems()  {



		if(!waitingList.isEmpty()) {
			return waitingList.toString();
		}

		return "No items Found.";
	}
	public boolean returnItem(String userID, String itemID){
		String itemPrefix = itemID.substring(0, Math.min(itemID.length(), 3)).toLowerCase();
//		String managerPrefix = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();
		String userType = userID.substring(3, Math.min(userID.length(), 4));
		if(itemPrefix.equals(libraryPrifix) && (userType.equals("U") || userType.equals("u"))){
			if(borrowedItems.containsKey(userID)) {
				List<BorrowedItem> items = borrowedItems.get(userID);

				for (Iterator<BorrowedItem> iterator = items.iterator(); iterator.hasNext(); ) {
					BorrowedItem value = iterator.next();
					if (value.getItemId().equals(itemID)) {
						if(itemsMaps.containsKey(itemID)) {
							iterator.remove();
							int itemLeft = itemsMaps.get(itemID).getitemQty() + 1;
							itemsMaps.get(itemID).setitemQty(itemLeft);
							String action = "return an Item: "+itemID;
							try {
								logCreate(userID, action, "true");
								serverLogCreate(userID,action, "true", "Success",  "USER ID: "+userID+"/ Item ID: "+itemID);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							lendItemFromWaiting(itemID);
							if(items.isEmpty()) {
								borrowedItems.remove(userID);
							}
							return true;
						}else 
						{
							return false;
						}
					}
				}



				return false;

			}
		}else if(userType.equals("U") || userType.equals("u")) {
			if(itemPrefix.equals("mcg")) {
				String result = sendMessage(mcgillServerPort,"return", userID, null,  itemID,  0);
				System.out.println(result);
				return Boolean.parseBoolean(result);
			}else if(itemPrefix.equals("mon")) {
				String result = sendMessage(montrealServerPort,"return", userID, null,  itemID,  0);
				return Boolean.parseBoolean(result);
			}else if(itemPrefix.equals("con")) {
				String result = sendMessage(concordiaServerPort,"return", userID, null,  itemID,  0);
				return Boolean.parseBoolean(result);
			}
		}

		return false;
	}
	public boolean waitInQueue(String userID,String itemID){
		String itemPrefix = itemID.substring(0, Math.min(itemID.length(), 3)).toLowerCase();
//		String userPrefix = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();
		String userType = userID.substring(3, Math.min(userID.length(), 4));
		if(itemPrefix.equals(libraryPrifix) && (userType.equals("U") || userType.equals("u"))){
			if(waitingList.containsKey(itemID)) {
				List<WaitingList> users = waitingList.get(itemID);
				users.add(new WaitingList(userID));
				waitingList.replace(itemID, users);
				//borrowedItems.get(userID).indexOf(itemID);
				String action = "Assked for waiting List: "+itemID;
				try {
					logCreate(userID, action, "true");
					serverLogCreate(userID,action, "true", "Success",  "USER ID: "+userID+"/ Item ID: "+itemID);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;	
			}
			else {
				List<WaitingList> users = new ArrayList<>();
				users.add(new WaitingList(userID));
				waitingList.put(itemID, users);
				String action = "Assked for waiting List: "+itemID;
				try {
					logCreate(userID, action, "true");
					serverLogCreate(userID,action, "true", "Success",  "USER ID: "+userID+"/ Item ID: "+itemID);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}

		}else if(userType.equals("U") || userType.equals("u")){
			if(itemPrefix.equals("mcg")) {
				String result = sendMessage(mcgillServerPort,"wait", userID, null,  itemID,  0);
				System.out.println(result);
				return Boolean.parseBoolean(result);
			}else if(itemPrefix.equals("mon")) {
				String result = sendMessage(montrealServerPort,"wait", userID, null,  itemID,  0);
				return Boolean.parseBoolean(result);
			}else if(itemPrefix.equals("con")) {
				String result = sendMessage(concordiaServerPort,"wait", userID, null,  itemID,  0);
				return Boolean.parseBoolean(result);
			}
		}

		return false;
	}

	public void lendItemFromWaiting(String itemId){
		if(waitingList.containsKey(itemId)) {
			int quantity = itemsMaps.get(itemId).getitemQty();
			if(quantity>0) {
				Iterator<WaitingList> iterator = waitingList.get(itemId).iterator();
				for(int i = quantity; i>0;i--) {
					if(iterator.hasNext()) {
						WaitingList value = iterator.next();
						iterator.remove();
						borrowItem(value.getUserName(),itemId,7);
					}
				}
			}
		}
	}
	public void logCreate(String userID, String acion, String response) throws IOException {
		String userPrefix = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();
		final String dir = System.getProperty("user.dir");
		String fileName = dir;
		if(userPrefix.equals("con")) {
			fileName = dir+"\\src\\ReplicaManagerOne\\Log\\Client\\Concordia\\"+userID+".txt";
		}else if(userPrefix.equals("mcg")) 
		{
			fileName = dir+"\\src\\ReplicaManagerOne\\Log\\Client\\Mcgill\\"+userID+".txt";
		}else if(userPrefix.equals("mon")) 
		{
			fileName = dir+"\\src\\ReplicaManagerOne\\Log\\Client\\Montreal\\"+userID+".txt";
		}


		FileWriter fileWriter = new FileWriter(fileName,true);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.println("Action: "+acion+" | Resonse: "+ response);

		printWriter.close();

	}


	public void serverLogCreate(String userID, String acion, String response,String requestResult, String peram) throws IOException {
		String userPrefix = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();
		final String dir = System.getProperty("user.dir");
		String fileName = dir;
		if(userPrefix.equals("con")) {
			fileName = dir+"\\src\\ReplicaManagerOne\\Log\\Server\\concordia.txt";
		}else if(userPrefix.equals("mcg")) 
		{
			fileName = dir+"\\src\\ReplicaManagerOne\\Log\\Server\\mcgill.txt";
		}else if(userPrefix.equals("mon")) 
		{
			fileName = dir+"\\src\\ReplicaManagerOne\\Log\\Server\\montreal.txt";
		}

		Date date = new Date();

		String strDateFormat = "yyyy-MM-dd hh:mm:ss a";

		DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

		String formattedDate= dateFormat.format(date);


		FileWriter fileWriter = new FileWriter(fileName,true);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.println("DATE: "+formattedDate+"Action: "+acion+" | Parameters: "+ peram +" | Action Status: "+requestResult+" | Resonse: "+ response);

		printWriter.close();

	}

	private static String sendMessage(int serverPort,String function,String userID,String itemName, String itemId, int numberOfDays) {
		DatagramSocket aSocket = null;
		String result ="";
		String dataFromClient = function+";"+userID+";"+itemName+";"+itemId+";"+numberOfDays;
		try {
			aSocket = new DatagramSocket();
			byte[] message = dataFromClient.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, dataFromClient.length(), aHost, serverPort);
			aSocket.send(request);

			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			aSocket.receive(reply);
			result = new String(reply.getData());
			String[] parts = result.split(";");
			result = parts[0];
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		return result;

	}



	public boolean exchangeItem(String userID,String newItemID,String oldItemID){
		boolean newItemAvailable = false;
		boolean oldItemAvailable =false;
		boolean checkAlreadyBorrowed = true;
		String itemPrefix = newItemID.substring(0, Math.min(newItemID.length(), 3)).toLowerCase();
		String oldItemIDPrefix = oldItemID.substring(0, Math.min(oldItemID.length(), 3)).toLowerCase();
//		String userPrefix = userID.substring(0, Math.min(userID.length(), 3)).toLowerCase();
		String userType = userID.substring(3, Math.min(userID.length(), 4));
		if(libraryPrifix.equals(itemPrefix) && libraryPrifix.equals(oldItemIDPrefix) && (userType.equals("U") || userType.equals("u"))){
			if(itemsMaps.containsKey(newItemID) && itemsMaps.containsKey(oldItemID)) {
				if(itemsMaps.get(newItemID).getitemQty()>0) {
					if(borrowedItems.containsKey(userID)) {
						List<BorrowedItem> items = borrowedItems.get(userID);
						if(items.stream().filter(o -> o.getItemId().equals(oldItemID)).findFirst().isPresent()) {
							System.out.println(6);
							items.stream().filter(o -> o.getItemId().equals(oldItemID)).findFirst().get().setItemId(newItemID);

							itemsMaps.get(oldItemID).setitemQty(itemsMaps.get(oldItemID).getitemQty() +1);
							itemsMaps.get(newItemID).setitemQty(itemsMaps.get(newItemID).getitemQty() -1);
							String action = "Replaced "+oldItemID+" with"+newItemID;
							try {
								logCreate(userID, action, "true");

								serverLogCreate(userID,action,"true", "Success",  "USER ID: "+userID+"/ New Item ID: "+newItemID+"/ old Item id: "+ oldItemID);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return true;
						}							
					}
				}
				else {
					String action = "Replaced "+oldItemID+" with"+newItemID;
					try {
						logCreate(userID, action, "false");

						serverLogCreate(userID,action,"false", "Failed",  "USER ID: "+userID+"/ New Item ID: "+newItemID+"/ old Item id: "+ oldItemID);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return false;
				}

			}
		}else {
			if(libraryPrifix.equals(itemPrefix)) {
				newItemAvailable= isAvailableInLibrary(newItemID);
			}else {
				if(itemPrefix.equals("mcg")) {
					String result = sendMessage(mcgillServerPort,"isAvailableInLibrary", userID, null,  newItemID,  0);
					newItemAvailable = Boolean.parseBoolean(result);
				}else if(itemPrefix.equals("mon")) {
					String result = sendMessage(montrealServerPort,"isAvailableInLibrary", userID, null,  newItemID,  0);
					newItemAvailable = Boolean.parseBoolean(result);
				}else if(itemPrefix.equals("con")) {
					String result = sendMessage(concordiaServerPort,"isAvailableInLibrary", userID, null,  newItemID,  0);
					newItemAvailable = Boolean.parseBoolean(result);
				}
			}

			if(libraryPrifix.equals(oldItemIDPrefix)) {
				oldItemAvailable= isBorrowed(userID,oldItemID);
			}else {
				if(oldItemIDPrefix.equals("mcg")) {
					String result = sendMessage(mcgillServerPort,"isBorrowed", userID, null,  oldItemID,  0);
					oldItemAvailable = Boolean.parseBoolean(result);
				}else if(oldItemIDPrefix.equals("mon")) {
					String result = sendMessage(montrealServerPort,"isBorrowed", userID, null,  oldItemID,  0);
					oldItemAvailable = Boolean.parseBoolean(result);
				}else if(oldItemIDPrefix.equals("con")) {
					String result = sendMessage(concordiaServerPort,"isBorrowed", userID, null,  oldItemID,  0);
					oldItemAvailable = Boolean.parseBoolean(result);
				}
			}

			if(!oldItemIDPrefix.equals(itemPrefix)) {
				if(libraryPrifix.equals(itemPrefix)) {
					checkAlreadyBorrowed= true;
				}else {
					if(itemPrefix.equals("mcg")) {
						String result = sendMessage(mcgillServerPort,"isAlreadyBorrowed", userID, null,  null,  0);
						checkAlreadyBorrowed = !Boolean.parseBoolean(result);
					}else if(itemPrefix.equals("mon")) {
						String result = sendMessage(montrealServerPort,"isAlreadyBorrowed", userID, null,  null,  0);
						checkAlreadyBorrowed = !Boolean.parseBoolean(result);
					}else if(itemPrefix.equals("con")) {
						String result = sendMessage(concordiaServerPort,"isAlreadyBorrowed", userID, null,  null,  0);
						checkAlreadyBorrowed = !Boolean.parseBoolean(result);
					}
				}
			}
			if(oldItemAvailable && newItemAvailable && checkAlreadyBorrowed) {
				boolean returnSuccess = false;
				returnSuccess= returnItem(userID, oldItemID);
				System.out.println(returnSuccess);
				if(returnSuccess) {
					returnSuccess = borrowItem(userID, newItemID, 7);
					System.out.println(returnSuccess);
				}
				System.out.println(returnSuccess);
				return returnSuccess;
			}

		}

		return false;
	}

	public boolean isAvailableInLibrary(String newItemID) {
		if(itemsMaps.containsKey(newItemID)) {
			if(itemsMaps.get(newItemID).getitemQty()>0) {
				return true;
			}
		}
		return false;
	}

	public boolean isBorrowed(String userId,String oldItemID) {
		if(borrowedItems.containsKey(userId)) {
			List<BorrowedItem> items = borrowedItems.get(userId);

			if(items.stream().filter(o -> o.getItemId().equals(oldItemID)).findFirst().isPresent()) {
				return true;
			}
		}
		return false;
	}

	public boolean isAlreadyBorrowed(String userId) {
		if(borrowedItems.containsKey(userId)) {
			return true;
		}
		return false;
	}
}