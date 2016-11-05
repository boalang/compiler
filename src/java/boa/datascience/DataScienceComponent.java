package boa.datascience;

import java.util.List;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

public abstract class DataScienceComponent {

	public abstract List<GeneratedMessage> getData();

	public boolean getDataInQueue(Queue<GeneratedMessage> queue) {
		if (queue == null || !queue.isOpen()) {
			throw new IllegalStateException("Your queue is not yet initialized");
		}
		// getData().forEach(data -> queue.offer(data));
		for (GeneratedMessage message : getData()) {
			queue.offer(message);
		}
		queue.close();
		return true;
	}
}
