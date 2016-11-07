package boa.dsi.storage;

import java.util.List;

import org.apache.log4j.Logger;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.dsi.DSComponent;
import boa.dsi.dsource.AbstractSource;

public abstract class AbstractStorage implements DSComponent {
	protected static Logger LOG = Logger.getLogger(AbstractStorage.class);
	protected String location;
	protected AbstractSource parserSource;

	public AbstractStorage(String location, AbstractSource parserSource) {
		this.location = location;
		this.parserSource = parserSource;
	}

	public abstract boolean isAvailable(String source);

	public abstract void store(List<GeneratedMessage> dataInstance);

	public abstract void store(Queue<GeneratedMessage> queue);

	public abstract void storeAt(String location, GeneratedMessage dataInstance);

	public abstract String getDataLocation();

	public boolean getDataInQueue(Queue<GeneratedMessage> queue) {
		if (queue == null || !queue.isOpen()) {
			throw new IllegalStateException("Your queue is not yet initialized");
		}
		for (GeneratedMessage message : getData()) {
			queue.offer(message);
		}
		queue.close();
		return true;
	}
}
