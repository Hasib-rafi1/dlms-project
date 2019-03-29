package ReplicaManagerTwo;

public class Message {
	    public String message; 
	    public int sequenceId; 
	          
	    // A parameterized student constructor 
	    public Message(String message, int sequenceId) { 
	      
	        this.message = message; 
	        this.sequenceId = sequenceId; 
	    } 
	      
	    public String getMessage() { 
	        return message; 
	    }  
	    
	    public int getsequenceId() { 
	        return sequenceId; 
	    }
	
}
