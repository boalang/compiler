package boa.datascience.internalDataStorage;

import java.util.List;

import org.apache.log4j.Logger;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.datascience.DataScienceComponent;
import boa.datascience.externalDataSources.DatagenProperties;
import boa.datascience.internalDataStorage.hadoopSequenceFile.SequenceFileStorage;

public class InternalDataStorage extends DataScienceComponent {
	protected static Logger LOG = Logger.getLogger(InternalDataStorage.class);
	private SequenceFileStorage storage;

	public InternalDataStorage() {
		this.storage = new SequenceFileStorage(
				DatagenProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DatagenProperties.HADOOP_SEQ_FILE_NAME);
	}

	public void store(GeneratedMessage dataInstance) {
		this.storage.store(dataInstance);
	}

	public void store(Queue<GeneratedMessage> queue) {
		if (queue == null) {
			throw new UnsupportedOperationException();
		}
		// this.queue.stream().limit(1).map(x -> x).forEach(dataInstance ->
		// this.storage.store(dataInstance));
		queue.stream().map(x -> x).forEach(dataInstance -> this.storage.store(dataInstance));
	}

	@Override
	public List<GeneratedMessage> getData() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public boolean getDataInQueue(Queue<GeneratedMessage> queue) {
		if(queue == null || !queue.isOpen()){
			throw new IllegalStateException("Queue is either null or has been closed");			
		}
		return false;
	}

}
