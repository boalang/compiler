package boa.datascience;

import java.util.List;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

public abstract class DataScienceComponent {

	public abstract List<GeneratedMessage> getData();
	
	public abstract boolean getDataInQueue(Queue<GeneratedMessage> q);
}
