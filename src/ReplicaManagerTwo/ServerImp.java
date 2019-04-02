package ReplicaManagerTwo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class ServerImp {

	public static boolean fault =true;
	public static int RMNo = 2;
	private Lock lock = new ReentrantLock();

	class Admin{
		String adminID = " ";
	}
	class User{
		String userID = " ";
		int borrowCount = 0;
	}
	class Item{
		String ID;
		String name;
		int num;
		
		 @Override
		    public String toString() {
		        return "Item{" +
		                "itemName='" + name + '\'' +
		                ", itemQty='" + num + '\'' +
		                '}';
		    }
	}

	private HashMap<String, Item> items = new HashMap<>();
	private HashMap<String, ArrayList<String> > waitList = new HashMap<>();
	private HashMap<String, ArrayList<String> > borrowedItems = new HashMap<>();
	private String Campus = " ";
	private int portUdp =0;

	public void StartServer(String campus, int portUdpFromSequencer) {
		Campus = campus;
		portUdp = portUdpFromSequencer;
		try {
			Log(Campus,getFormatDate() + " Server for " + Campus + " started");
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(Campus.equals("CON")) {
			Item i1 = new Item();
			i1.name = "Distributed System";
			i1.num = 10;
			i1.ID = "CON1010";
			Item i2 = new Item();
			i2.name = "Absoulate Java";
			i2.num = 11;
			i2.ID = "CON1011";
			Item i3 = new Item();
			i3.name = "Data Structure";
			i3.num = 5;
			i3.ID = "CON1012";
			items.put(i1.ID,i1);
			items.put(i2.ID,i2);
			items.put(i3.ID,i3);
			RMNo = 21;
		}else if(Campus.equals("MCG")) {
			Item i1 = new Item();
			i1.name = "Distributed System";
			i1.num = 20;
			i1.ID = "MCG1010";
			Item i2 = new Item();
			i2.name = "Absoulate Java";
			i2.num = 22;
			i2.ID = "MCG1011";
			Item i3 = new Item();
			i3.name = "Data Structure";
			i3.num = 10;
			i3.ID = "MCG1012";
			items.put(i1.ID,i1);
			items.put(i2.ID,i2);
			items.put(i3.ID,i3);
			RMNo = 22;
		}else if(Campus.equals("MON")) {
			Item i1 = new Item();
			i1.name = "Distributed System";
			i1.num = 20;
			i1.ID = "MON1010";
			Item i2 = new Item();
			i2.name = "Absoulate Java";
			i2.num = 22;
			i2.ID = "MON1011";
			Item i3 = new Item();
			i3.name = "Data Structure";
			i3.num = 10;
			i3.ID = "MON1012";
			items.put(i1.ID,i1);
			items.put(i2.ID,i2);
			items.put(i3.ID,i3);
			RMNo = 23;
		}else {
			Item i1 = new Item();
			i1.name = "a";
			i1.num = 1;
			i1.ID = Campus+"1111";
			Item i2 = new Item();
			i2.name = "b";
			i2.num = 2;
			i2.ID = Campus+"2222";
			items.put(i1.ID,i1);
			items.put(i2.ID,i2);
		}

		Runnable task2 = () -> {
			receiveFromSequencer();
		};
		Thread thread2 = new Thread(task2);
		thread2.start();
		sendMessageBackToFrontend("Listen from RM2 Concordia");
	}

	private static void Log(String serverID,String Message) throws Exception{
		final String dir = System.getProperty("user.dir");
		String path = dir+"\\src\\ReplicaManagerTwo\\ServerLog\\" + serverID + "_Server.log";
		FileWriter fileWriter = new FileWriter(path,true);
		BufferedWriter bf = new BufferedWriter(fileWriter);
		bf.write(Message + "\n");
		bf.close();
	}

	private String getFormatDate(){
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}

	//    public boolean managerLogin(String managerID) {
	//
	//        Boolean exist = false;
	//
	//        for (Admin adminClient : adminClients) {
	//            if (adminClient.adminID.equals(managerID)) {
	//                exist = true;
	//                break;
	//            }
	//        }
	//
	//        if(!exist){
	//            Admin newAdmin = new Admin();
	//            newAdmin.adminID = managerID;
	//            adminClients.add(newAdmin);
	//        }
	//        System.out.println("ManagerClient [" + managerID + "] log in successfully");
	//        try {
	//            Log(Campus, getFormatDate() + " ManagerClient [" + managerID + "] log in successfully" );
	//        } catch (Exception e) {
	//            e.printStackTrace();
	//        }
	//        return true;
	//    }
	//
	//    public boolean userLogin(String studentID) {
	//
	//        Boolean exist = false;
	//
	//        for (User userClient : userClients) {
	//            if (userClient.userID.equals(studentID)) {
	//                exist = true;
	//                break;
	//            }
	//        }
	//        if(!exist){
	//            User newStudent = new User();
	//            newStudent.userID = studentID;
	//            newStudent.borrowCount = 0;
	//            userClients.add(newStudent);
	//        }
	//        System.out.println("UserClient " + studentID + " log in successfully");
	//        try {
	//            Log(Campus, getFormatDate() + " UserClient " + studentID + " log in successfully" );
	//        } catch (Exception e) {
	//            e.printStackTrace();
	//        }
	//        return true;
	//    }

	//manager operations

	public String addItem(String managerID, String itemID, String itemName, int quantity) {
		int intQuantity = quantity;
		String result = "";
		String error = "";
		if(itemID.isEmpty() || itemName.isEmpty()){
			return result;
		}

		synchronized(this) {
			if(intQuantity >= 0){
				if (items.containsKey(itemID)){
					if(itemName.equals(items.get(itemID).name)) {
						items.get(itemID).num += intQuantity;
						result = "Books added Successfully";
					}else{
						result = "Books Can't be added";
						error = "Books Can't be added";
					}
				}else {
					int flag = 0;
					for (HashMap.Entry<String, Item> entry : items.entrySet()) {
						if (entry.getValue().name.equals(itemName)) {
							error = "Books Can't be added";
							result = "Books Can't be added";
							flag = 1;
						}
					}
					if (flag == 0) {
						Item newItem = new Item();
						newItem.name = itemName;
						newItem.num = intQuantity;
						items.put(itemID, newItem);
						result = "Books added Successfully";
					}
				}
			}
			else{
				error = "Books Can't be added";
				result = "Books Can't be added";
			}
		}

		if(!result.isEmpty()){
			System.out.println(result);
			try {
				Log(Campus, getFormatDate() + result);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(waitList.containsKey(itemID) && waitList.get(itemID).size() > 0) {
				String lendResult = autoLend(itemID);
				if (!lendResult.isEmpty()) {
					String log = " Server auto lend item ["+itemID+"] " +
							"to user : " + lendResult+" after manager ["+managerID+"] add item. ";
					try {
						System.out.println(log);
						Log(Campus, getFormatDate() + log);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					String log1 = " Server auto lend item ["+itemID+"] " +
							"to user failed after manager ["+managerID+"] add item. ";
					try {
						System.out.println(log1);
						Log(Campus, getFormatDate() + log1);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			try {
				Log(Campus, getFormatDate() + error);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public String removeItem(String managerID, String itemID, int quantity) {
		int intQuantity = quantity;
		String result = "";
		String error = "";
		synchronized(this) {
			if(items.containsKey(itemID)){
				if(items.get(itemID).num > 0){
					if(intQuantity < 0 ){
						//remove all
						items.remove(itemID);
						result = "Book removed successful";
						if(waitList.containsKey(itemID)){
							waitList.remove(itemID);
						}if(borrowedItems.containsKey(itemID)){
							borrowedItems.remove(itemID);
						}
					}else if(intQuantity <= items.get(itemID).num) {
						items.get(itemID).num -= intQuantity;
						result = "Book removed successful";
					}else{
						result = "Book can't be removed";
						error = "Book can't be removed";
					}
				}else{
					result = "Book can't be removed";
					error = "Book can't be removed";
				}
			}else{
				result = "Book can't be removed";
				error = "Book can't be removed";
			}
		}
		if(!result.isEmpty()) {
			try {
				Log(Campus, getFormatDate() + result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else{
			String log = " Manager [" + managerID + "] remove ["
					+ quantity + "] of item [" + itemID + "] failed: ";
			log += error;
			try {
				Log(Campus, getFormatDate() +log);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public String listItemAvailability (String managerID) {
		String result = items.toString();
//		for(HashMap.Entry<String, Item> entry : items.entrySet()){
//			result = result + entry.getKey() + " " + entry.getValue().name + " " + entry.getValue().num + " , ";
//		}
		if(result.isEmpty()){
			String log = " Manager [" + managerID + "] list all of item failed";
			System.out.println(log);
			try {
				Log(Campus, getFormatDate() + log);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			String log1 = " Manager [" + managerID + "] list all of item success. "
					+ "All Items: " + result;
			System.out.println(log1);
			try {
				Log(Campus, getFormatDate() + log1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}


	//user operations

	public boolean borrowItem (String userID, String itemID, int numberOfDays) {
		boolean result = false;
		String campusName = itemID.substring(0,3);
		String command = "borrowItem(" + userID + "," + itemID + "," + String.valueOf(numberOfDays) + ")";

		try {
			if (campusName.equals(Campus)) {
				result = borrowLocal(userID, itemID);
			} else if (campusName.equals("CON")) {
				int serverport = 2234;
				result = UDPRequest.UDPborrowItem(command, serverport);
			} else if (campusName.equals("MCG")) {
				int serverport = 2235;
				result = UDPRequest.UDPborrowItem(command, serverport);
			} else if (campusName.equals("MON")) {
				int serverport = 2236;
				result = UDPRequest.UDPborrowItem(command, serverport);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!campusName.equals(Campus)) {
			if (!result) {
				String log = " Server borrow item [" + itemID + "] for user [" + userID + "] from server [" + campusName + "] failed";
				System.out.println(log);
				try {
					Log(Campus, getFormatDate() + log);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				String log2 = " Server borrow item [" + itemID + "] for user [" + userID + "] from server [" + campusName + "] success";
				System.out.println(log2);
				try {
					Log(Campus, getFormatDate() + log2);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	public boolean borrowLocal(String userID, String itemID){
		boolean result = false;
		String failReason = "";
		int flag = 0;
		String userCampus = userID.substring(0,3);
		synchronized (this){
			if(!userCampus.equals(Campus)){
				for(HashMap.Entry<String, ArrayList<String>> entry : borrowedItems.entrySet()){
					if(entry.getValue().contains(userID)){
						flag = 1;
						failReason = "User can only borrow 1 item from other libraries";
					}
				}
			}
			if(flag == 0) {
				if (items.get(itemID).num > 0) {
					if (!borrowedItems.containsKey(itemID)) {
						ArrayList<String> newBorrowedUser = new ArrayList<>();
						newBorrowedUser.add(userID);
						borrowedItems.put(itemID, newBorrowedUser);
						items.get(itemID).num--;
					} else {
						if (!borrowedItems.get(itemID).contains(userID)) {
							borrowedItems.get(itemID).add(userID);
							items.get(itemID).num--;
						}
					}
					result = true;
				}else{
					failReason = "No item left";
				}
			}
			if (!result) {
				String log = " User [" + userID + "] borrow item ["+itemID+"] failed: ";
				System.out.println(log);
				try {
					Log(Campus, getFormatDate() + log +failReason);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				String log2 = " User [" + userID + "] borrow item ["+itemID+"] success.";
				System.out.println(log2);
				try {
					Log(Campus, getFormatDate() + log2);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}



	public boolean waitInQueue(String userID, String itemID) {
		boolean result = false;
		String campusName = itemID.substring(0,3);
		String command = "waitInQueue(" + userID + "," + itemID + ")";
		try {
			if (campusName.equals(Campus)) {
				result = waitInLocal(userID, itemID);
			} else if (campusName.equals("CON")) {
				int serverport = 2234;
				result = UDPRequest.UDPwaitInQueue(command, serverport);
			} else if (campusName.equals("MCG")) {
				int serverport = 2235;
				result = UDPRequest.UDPwaitInQueue(command, serverport);
			} else if (campusName.equals("MON")) {
				int serverport = 2236;
				result = UDPRequest.UDPwaitInQueue(command, serverport);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public boolean waitInLocal(String userID, String itemID){
		boolean result = false;
		synchronized (this) {

			if(!waitList.containsKey(itemID)){
				ArrayList<String> users = new ArrayList<>();
				users.add(userID);
				waitList.put(itemID,users);
				result = true;
			}else{
				if(!waitList.get(itemID).contains(userID)){
					waitList.get(itemID).add(userID);
					result = true;
				}
			}

			if (!result) {
				String log = " Server add user [" + userID + "] in wait queue of item ["+itemID+ "] failed.";
				System.out.println(log);
				try {
					Log(Campus, getFormatDate() + log);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				String log1 = " Server add user [" + userID + "] in wait queue of " +
						"item ["+itemID+ "] at position [" +result+"] success.";
				System.out.println(log1);
				try {
					Log(Campus, getFormatDate() + log1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return result;
	}



	public String findItem (String userID, String itemName) {
		String result = "";
		result = findItemLocal(itemName);
		String command = "findItem(" + itemName + ")";

		try {
			switch (Campus) {
			case "CON": {
				int serverport1 = 2235;
				int serverport2 = 2236;
				result = "Concordia "+result + " Montreal " + UDPRequest.UDPfindItem(command, serverport2);
				result = result + " McGill " + UDPRequest.UDPfindItem(command, serverport1);
				break;
			}
			case "MCG": {
				int serverport1 = 2234;
				int serverport2 = 2236;
				result = " Montreal " + UDPRequest.UDPfindItem(command, serverport2) + " McGill "+result;
				result = "Concordia " + UDPRequest.UDPfindItem(command, serverport1)+result;
				break;
			}
			default: {
				int serverport1 = 2234;
				int serverport2 = 2235;
				result = " Montreal "+result + " McGill " + UDPRequest.UDPfindItem(command, serverport2);
				result = "Concordia " + UDPRequest.UDPfindItem(command, serverport1)+result;
				break;
			}
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		if(!result.isEmpty()) {
			String log =" User [" + userID + "] found all item named ["+itemName +"] success . Items: "+result;
			System.out.println(log);
			try {
				Log(Campus, getFormatDate() + log );
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			String log1 =" User [" + userID + "] found all item named ["+itemName +"] failed. ";
			System.out.println(log1);
			try {
				Log(Campus, getFormatDate() + log1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	public String findItemLocal(String itemName){
		String result = "No items are available";
		synchronized(this) {
			for (Map.Entry<String, Item> entry : items.entrySet()) {
				System.out.println(itemName);
				String name = entry.getValue().name;
				if(name.equalsIgnoreCase(itemName)) {
					result = entry.toString();

				}

			}
		}
		return result;
	}



	public boolean returnItem(String userID, String itemID) {
		boolean result = false;
		String campusName = itemID.substring(0,3);
		String command = "returnItem(" + itemID + "," + userID + ")";
		int serverPort;

		try {
			if(campusName.equals(Campus)){
				result = returnLocal(itemID,userID);

			}
			else if(campusName.equals("CON")){
				serverPort = 2234;
				result = UDPRequest.UDPreturnItem(command,serverPort);

			}
			else if(campusName.equals("MCG")){
				serverPort = 2235;
				result = UDPRequest.UDPreturnItem(command,serverPort);

			}
			else if(campusName.equals("MON")){
				serverPort = 2236;
				result = UDPRequest.UDPreturnItem(command,serverPort);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if(!campusName.equals(Campus)) {
			if (result) {
				String log = " Server return item [" + itemID + "] for user [" + userID + "] to server ["
						+ campusName + "] success";
				System.out.println(log);
				try {
					Log(Campus, getFormatDate() + log);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				String log1 = " Server return item [" + itemID + "] for user [" + userID + "] to server ["
						+ campusName + "] failed";
				System.out.println(log1);
				try {
					Log(Campus, getFormatDate() + log1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	public boolean returnLocal(String itemID,String userID) {
		boolean result = false;
		synchronized (this) {
			if (borrowedItems.containsKey(itemID)) {
				if (borrowedItems.get(itemID).contains(userID)) {
					borrowedItems.get(itemID).remove(userID);
					items.get(itemID).num++;
					result = true;
					String log = " User [" + userID + "] return item [" + itemID + "] success.";
					System.out.println(log);
					try {
						Log(Campus, getFormatDate() + log);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(waitList.containsKey(itemID) && waitList.get(itemID).size() > 0) {
						String lendResult = autoLend(itemID);
						if (!lendResult.isEmpty()) {
							String log2 = " Server auto lend item ["+itemID+"] " +
									"to user : " + lendResult+" after user ["+userID+"] return.";
							System.out.println(log2);
							try {
								Log(Campus, getFormatDate() + log2);
							} catch (Exception e) {
								e.printStackTrace();
							}
							result = true;
						} else {
							String log3 = " Server auto lend item ["
									+itemID+"] failed after user ["+userID+"] return.";
							try {
								System.out.println(log3);
								Log(Campus, getFormatDate() + log3);
							} catch (Exception e) {
								e.printStackTrace();
							}
							result = true;
						}
					}

				}
			}
		}
		if (!result){
			String log1 = " User [" + userID + "] return item [" + itemID + "] failed.";
			System.out.println(log1);
			try {
				Log(Campus, getFormatDate() + log1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}


	//    public String listBorrowedItem(String userID){
	//        String result = "";
	//        result = listBorrowedLocal(userID);
	//        String command = "listBorrowedItem(" + userID + ")";
	//
	//        try {
	//            if(Campus.equals("CON")){
	//                int serverport1 = 2235;
	//                int serverport2 = 2236;
	//                result += UDPRequest.UDPlistBorrowedItem(command, serverport1);
	//                result += UDPRequest.UDPlistBorrowedItem(command, serverport2);
	//            }
	//            else if(Campus.equals("MCG")){
	//                int serverport1 = 2234;
	//                int serverport2 = 2236;
	//                result += UDPRequest.UDPlistBorrowedItem(command, serverport1);
	//                result += UDPRequest.UDPlistBorrowedItem(command, serverport2);
	//            }
	//            else if(Campus.equals("MON")){
	//                int serverport1 = 2234;
	//                int serverport2 = 2235;
	//                result += UDPRequest.UDPlistBorrowedItem(command, serverport1);
	//                result += UDPRequest.UDPlistBorrowedItem(command, serverport2);
	//            }
	//        } catch (SocketException e1) {
	//            e1.printStackTrace();
	//        }
	//
	//        return result;
	//    }
	//    public String listBorrowedLocal(String userID){
	//        String result = "";
	//        for(HashMap.Entry<String,ArrayList<String>> entry : borrowedItems.entrySet()){
	//            if(entry.getValue().contains(userID)){
	//                result += entry.getKey()+" "+items.get(entry.getKey()).name+", ";
	//            }
	//        }
	//        return result;
	//    }

	public boolean exchangeItem(String studentID, String newItemID, String oldItemID) {
		boolean result = false;
		String newCampus = newItemID.substring(0,3);
		String oldCampus = oldItemID.substring(0,3);
		String command = "exchangeItem(" + studentID + "," + newItemID + "," + oldItemID + ")";
		int serverPort;

		if(newCampus.equals(oldCampus)){
			try {
				if(newCampus.equals(Campus)){
					result = exchangeLocal(studentID,newItemID,oldItemID);
				}
				else if(newCampus.equals("CON")){
					serverPort = 2234;
					result = UDPRequest.UDPexchangeItem(command,serverPort);
				}
				else if(newCampus.equals("MCG")){
					serverPort = 2235;
					result = UDPRequest.UDPexchangeItem(command,serverPort);
				}
				else if(newCampus.equals("MON")){
					serverPort = 2236;
					result = UDPRequest.UDPexchangeItem(command,serverPort);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			try {
				lock.lock();
				if(oldCampus.equals(Campus)){
					command = "borrowItem(" + studentID + "," + newItemID + "," + String.valueOf(1) + ")";
					boolean returnResult = returnLocal(oldItemID, studentID);
					if(returnResult) {
						if(newCampus.equals("CON")){
							serverPort = 2234;
							result = UDPRequest.UDPborrowItem(command, serverPort);
						}
						else if(newCampus.equals("MCG")){
							serverPort = 2235;
							result = UDPRequest.UDPborrowItem(command, serverPort);
						}
						else if(newCampus.equals("MON")){
							serverPort = 2236;
							result = UDPRequest.UDPborrowItem(command, serverPort);
						}
					}
				}else {
					command = "returnItem(" + oldItemID + "," + studentID + ")";
					boolean returnResult = false;
						if(oldCampus.equals("CON")){
							serverPort = 2234;
							returnResult = UDPRequest.UDPreturnItem(command, serverPort);
						}
						else if(oldCampus.equals("MCG")){
							serverPort = 2235;
							returnResult = UDPRequest.UDPreturnItem(command, serverPort);
						}
						else if(oldCampus.equals("MON")){
							serverPort = 2236;
							returnResult = UDPRequest.UDPreturnItem(command, serverPort);
						}
						if(returnResult) {
							result=borrowLocal(studentID,newItemID);
						}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				lock.unlock();
			}
		}
		return result;
	}
	public boolean exchangeLocal(String studentID, String newItemID, String oldItemID) {
		boolean result = false;
		if(borrowedItems.containsKey(oldItemID) && borrowedItems.get(oldItemID).contains(studentID)){
			try{
				lock.lock();
				boolean returResult =returnLocal(oldItemID, studentID);
				

				if(returResult) {
					result = borrowLocal(studentID,newItemID);;
					String log = " User [" + studentID + "] exchange with item [" + oldItemID + "] for item [" +
							newItemID + "] success.";
					System.out.println(log);
					try {
						Log(Campus, getFormatDate() + log);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}finally {
				lock.unlock();
			}
		}else{
			String log = " User [" + studentID + "] exchange item ["+oldItemID+"] failed: ";
			String error = "Item is not borrowed or is not borrowed by user [" + studentID + "]";
			try {
				Log(Campus, getFormatDate() + log + error);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	//server auto operation
	private String autoLend (String itemID) {
		boolean result = false;
		String users = "";
		if (waitList.containsKey(itemID) && waitList.get(itemID).size() > 0 ) {
			int left = items.get(itemID).num;
			int pointer = 0;
			while(left > 0 && waitList.get(itemID).size() > 0 && pointer < waitList.get(itemID).size() ) {
				String waitUser = waitList.get(itemID).get(pointer);
				result = borrowLocal(waitUser, itemID);
				if(result){
					waitList.get(itemID).remove(waitUser);
					users += waitUser+",";
				}else{
					pointer ++;
				}
				left = items.get(itemID).num;
			}
		}
		return users;
	}

	private  void receiveFromSequencer() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(portUdp);
			byte[] buffer = new byte[1000];
			System.out.println("Sequencer UDP Server "+portUdp+" Started............");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String sentence = new String( request.getData(), 0,
						request.getLength() );
				if(!sentence.equals("Test")&&!sentence.equals("fault")) {
					findNextMessage(sentence);
				}else if(sentence.equals("fault")) {
					fault=false;
				}
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

	public  void findNextMessage(String sentence) {
				String message = sentence;
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
					sendingResult = this.addItem(userID,itemId, itemName,number);
				}else if(function.equals("removeItem")) {
					String result = this.removeItem(userID, itemId,number);
					sendingResult = result;
				}else if(function.equals("listItemAvailability")) {
					String result = this.listItemAvailability(userID);
					sendingResult = result;
				}else if(function.equals("borrowItem")) {
					boolean result = this.borrowItem(userID, itemId,number);
					sendingResult = Boolean.toString(result);
				}else if(function.equals("findItem")) {
					sendingResult = this.findItem(userID,itemName);
				}else if(function.equals("returnItem")) {
					boolean result = this.returnItem(userID,itemId);
					sendingResult = Boolean.toString(result);
				}else if(function.equals("waitInQueue")) {
					boolean result = this.waitInQueue(userID,itemId);
					sendingResult = Boolean.toString(result);
				}else if(function.equals("exchangeItem")) {
					boolean result = this.exchangeItem(userID,newItemId,itemId);
					sendingResult = Boolean.toString(result);
				}

				sendingResult= sendingResult+":"+RMNo+":"+message+":";
				sendMessageBackToFrontend(sendingResult);			 
	}
	
	public  void sendMessageBackToFrontend(String message) {
		System.out.println(message);
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
