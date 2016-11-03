package boa.datascience.externalDataSources;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.datascience.DataScienceComponent;

/**
 * Created by nmtiwari on 11/2/16.
 */
public class ExternalDataSources extends DataScienceComponent {
	private static Logger LOG = Logger.getLogger(ExternalDataSources.class);
	ArrayList<String> sources;

	public ExternalDataSources(ArrayList<String> sources) {
		this.sources = sources;
	}

	public GeneratedMessage getProcessedDataFrom(String source) {
		AbstractDataReader reader = AbstractDataReader.getDataReaders(source);
		GeneratedMessage data = reader.getData();
		return data;
	}

	public boolean getDataInQueue(Queue<GeneratedMessage> queue) {
		if (queue == null || !queue.isOpen()) {
			throw new IllegalStateException("Your queue is not yet initialized");
		}
		sources.stream().forEach(source -> queue.offer(this.getProcessedDataFrom(source)));
		queue.close();
		return true;
	}

	/**
	 * FIXME: Implement this method
	 */
	@Override
	public List<GeneratedMessage> getData() {
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
