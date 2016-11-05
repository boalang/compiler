package boa.datascience.internalDataStorage;

import java.util.ArrayList;
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

	public InternalDataStorage(String parser) {
		this.storage = new SequenceFileStorage(
				DatagenProperties.HADOOP_SEQ_FILE_LOCATION + "/" + DatagenProperties.HADOOP_SEQ_FILE_NAME, parser);
	}

	public void store(List<GeneratedMessage> dataInstance) {
		this.storage.store(dataInstance);
	}

	public void store(Queue<GeneratedMessage> queue) {
		if (queue == null) {
			throw new UnsupportedOperationException();
		}
		// this.storage.store(queue.stream().map(x ->
		// x).collect(Collectors.toList()));

		int totalMessages = queue.size();
		ArrayList<GeneratedMessage> msg = new ArrayList<GeneratedMessage>();
		for (int i = 0; i < totalMessages; i++) {
			msg.add(queue.get());
		}
		this.storage.store(msg);

	}

	@Override
	public List<GeneratedMessage> getData() {
		return this.storage.getData();
	}

	public String getDataLocation() {
		return this.storage.getDataLocation();
	}

}
