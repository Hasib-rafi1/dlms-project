package ReplicaManagerTwo;

import java.util.Comparator;

public class MessageComparator implements Comparator<Message>{

	@Override
	public int compare(Message arg0, Message arg1) {
		// TODO Auto-generated method stub
		if (arg0.sequenceId < arg1.sequenceId) 
			return 1; 
		else if (arg0.sequenceId > arg1.sequenceId) 
			return -1; 
		return 0; 
	} 
} 
