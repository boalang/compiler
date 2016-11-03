package boa.datascience;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.datascience.externalDataSources.ExternalDataSources;
import boa.datascience.internalDataStorage.InternalDataStorage;

public class DataScienceMaster {
	public static void main(String[] args) {
		String url = "https://github.com/boalang/compiler";
		ArrayList<String> sources = new ArrayList<>();
		sources.add(url);

		Queue<GeneratedMessage> queue = new Queue<>(new LinkedBlockingQueue<GeneratedMessage>());
		ExternalDataSources external = new ExternalDataSources(queue, sources);
		InternalDataStorage storage = new InternalDataStorage(queue);
		external.getDataInQueue();
		storage.store();
	}
}
