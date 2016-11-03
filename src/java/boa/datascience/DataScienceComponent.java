package boa.datascience;

import java.util.List;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

public abstract class DataScienceComponent {
	protected Queue<GeneratedMessage> queue;

	public DataScienceComponent(Queue<GeneratedMessage> queue) {
		this.queue = queue;
	}

	public abstract List<GeneratedMessage> getData();
	
	public abstract boolean getDataInQueue();
}
