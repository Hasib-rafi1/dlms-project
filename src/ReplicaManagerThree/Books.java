package ReplicaManagerThree;

public class Books 
{
	
	private String itemID;
	private String itemName;
	private int quantity;
	
	public Books(String itemID, String itemName,int quantity) {
		this.itemID = itemID;
		this.itemName = itemName;
		this.quantity = quantity;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	  @Override
	    public String toString() {
	        return "Item{" +
	                "itemName='" + itemName + '\'' +
	                ", itemQty='" + quantity + '\'' +
	                '}';
	    }
}
