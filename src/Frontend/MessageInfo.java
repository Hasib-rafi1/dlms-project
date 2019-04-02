package Frontend;

public class MessageInfo {

	private int sequenceId;
	private int RMNo;
	private String Message;
	private String Response;
	private String function;
	private String userID;
	private String itemName;
	private String itemId;
	private String newItem;
	private int number;
	
	public String getFunction() {
		return function;
	}
	public void setFunction(String function) {
		this.function = function;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getNewItem() {
		return newItem;
	}
	public void setNewItem(String newItem) {
		this.newItem = newItem;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getSequenceId() {
		return sequenceId;
	}
	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}
	public int getRMNo() {
		return RMNo;
	}
	public void setRMNo(int rMNo) {
		RMNo = rMNo;
	}
	public String getMessage() {
		return Message;
	}
	public void setMessage(String message) {
		Message = message;
	}
	public String getResponse() {
		return Response;
	}
	public void setResponse(String response) {
		Response = response;
	}
	
}
