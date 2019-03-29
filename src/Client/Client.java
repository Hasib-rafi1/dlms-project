package Client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import ServerObjectInterfaceApp.ServerObjectInterface;
import ServerObjectInterfaceApp.ServerObjectInterfaceHelper;

public class Client {

	public static void main(String args[])
	{
		try {
			ORB orb = ORB.init(args, null);
			// -ORBInitialPort 1050 -ORBInitialHost localhost
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			startSystem(ncRef);
		} catch (Exception e) {
			System.out.println("Hello Client exception: " + e);
			e.printStackTrace();
		}
		
	}
	// Initiating client site program 
	private static void startSystem(NamingContextExt ncRef) {
		System.out.println("Enter your username: ");
		Scanner scanner = new Scanner(System.in);
		String username = scanner.nextLine().toUpperCase();
		System.out.println("You are loging as " + username);
		if(username.length()!=8) {
			System.out.println("Wrong ID");
			startSystem(ncRef);
		}
		if(!username.substring(3,4).equals("U") && !username.substring(3,4).equals("M")){
			System.out.println("Invalid ID");
			startSystem(ncRef);
		}
		String accessParameter = username.substring(3, Math.min(username.length(), 4));
		System.out.println("You are loging as " + accessParameter);
		if(accessParameter.equals("U") || accessParameter.equals("u") ) {
			try {
				user(username,ncRef);
				startSystem(ncRef);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(accessParameter.equals("M") || accessParameter.equals("m")) {
			try {
				manager(username,ncRef);
//				startSystem();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.out.println("This user is not authorized");
			startSystem(ncRef);
		}
	}
	
	private static void user(String username,NamingContextExt ncRef) throws Exception
	{
		String serverPort = decideServerport(username);
		if(serverPort.equals("1")) {
			startSystem(ncRef);
		}
		ServerObjectInterface obj = (ServerObjectInterface) ServerObjectInterfaceHelper.narrow(ncRef.resolve_str(serverPort));
		System.out.println("1. Borrow Item \n 2.Find Item \n 3. Return item \n 4. Exchange \n 5. Logout");
		System.out.println("Select the option you want to do: ");
		Scanner scanner = new Scanner(System.in);
		String menuSelection = scanner.nextLine();

		if(menuSelection.equals("1")) {
			String itemId = setItemId(username);
			int numbersOfDay = setNumbersOfDay(username);
			boolean n = obj.borrowItem(username, itemId,numbersOfDay);
			System.out.println("Item Borrowed : " + n);
			if(!n) {
				System.out.println("Item is not available now. Do you like to stay in waiting list? \n Press Y for yes and enter. N for NO and enter");
				String waitingOption = scanner.nextLine();
				if(waitingOption.equals("Y")||waitingOption.equals("y")) {
					boolean m = obj.waitInQueue(username, itemId);
					if(m) {
						System.out.println("Your Item will be lend to you, when it will be available.");
					}
				}else {
					user(username,ncRef);
				}
			}
			user(username,ncRef);
		}
		else if(menuSelection.equals("2")) {
			String itemName = setItemName(username);
			System.out.println("Item List is given below. ");
			System.out.println(obj.findItem(username, itemName));
			System.out.println("To GO back press E and enter");
			String exit = scanner.nextLine();
			if(exit.equals("E") || exit.equals("e")) {
				user(username,ncRef);
			}else {
				user(username,ncRef);
			}
		}
		else if(menuSelection.equals("3")) {
			String itemId = setItemId(username);
			boolean n = obj.returnItem(username, itemId);
			System.out.println("Item Returend : " + n);
			user(username,ncRef);
		}
		else if(menuSelection.equals("4")) {
			String newItemId = setItemId(username);
			String oldItemId = setItemId(username);
			boolean n = obj.exchangeItem(username,newItemId, oldItemId);
			System.out.println("Item Exchanged : " + n);
			user(username,ncRef);
		}
		else if (menuSelection.equals("5")) {
			startSystem(ncRef);
		}
		else {
			user(username,ncRef);
		}
	}
	
	private static void manager(String username,NamingContextExt ncRef) throws Exception
	{
		String serverPort = decideServerport(username);
		if(serverPort.equals("1")) {
			startSystem(ncRef);
		}
		ServerObjectInterface obj = (ServerObjectInterface) ServerObjectInterfaceHelper.narrow(ncRef.resolve_str(serverPort));
		
		System.out.println("1. Add Items \n 2.Remove Item \n 3. List of the items \n 4. Logout");
		System.out.println("Select the option you want to do: ");
		Scanner scanner = new Scanner(System.in);
		String menuSelection = scanner.nextLine();
		if(menuSelection.equals("1")) {
			String itemId = setItemId(username);
			String itemName = setItemName(username);
			int itemQty = setItemQty(username);
			String n = obj.addItem(username, itemId,itemName,itemQty);
			System.out.println(n);
			manager(username,ncRef);
		}
		else if(menuSelection.equals("2")) {
			String itemId = setItemId(username);
			int itemQty = setItemQty(username);
			String n = obj.removeItem(username, itemId,itemQty);
			System.out.println(n);
			manager(username,ncRef);
		}
		else if(menuSelection.equals("3")) {
			System.out.println("Item List is given below. ");
			System.out.println(obj.listItemAvailability(username));
			System.out.println("To GO back press E and enter");
			String exit = scanner.nextLine();
			if(exit.equals("E") || exit.equals("e")) {
				manager(username,ncRef);
			}else {
				manager(username,ncRef);
			}
		}
		else if (menuSelection.equals("4")) {
			startSystem(ncRef);
		}
		else {
			manager(username,ncRef);
		}

	}
	
	private static String decideServerport(String username) {
		String serverPort="frontend";
		
		return serverPort;
	}
	
	private static String setItemId(String username) {
		String libraryCode = username.substring(0, Math.min(username.length(), 3));
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter Item Id: ");
		String itemId = scanner.nextLine().toUpperCase();
		String itemPrefix = itemId.substring(0, Math.min(itemId.length(), 3));
		if(itemId.length()!=7 && libraryCode !=itemPrefix) {
			System.out.println("Enter a valid Item Id: ");
			itemId = setItemId(username);
		}
		return  itemId;
	}
	
	private static String setItemName(String username) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter Item name: ");
		String itemName = scanner.nextLine().toUpperCase();
	
		
		return  itemName;
	}
	
	private static int setItemQty(String username) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter Item Quantity: ");
		int itemQty;
		if(scanner.hasNextInt()){
			itemQty = scanner.nextInt();
			if(itemQty<0) {
				System.out.println("Enter a valid Number: ");
				itemQty = setItemQty(username);
			}
		}else{
			System.out.println("Enter a valid Item Id: ");
			itemQty = setItemQty(username);
		}
		return  itemQty;
	}
	
	private static int setNumbersOfDay(String username) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter Number Of Days: ");
		int numberOfDays;
		if(scanner.hasNextInt()){
			numberOfDays = scanner.nextInt();
		}else{
			System.out.println("Enter a valid Number: ");
			numberOfDays = setNumbersOfDay(username);
		}
		return  numberOfDays;
	}
}
