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

		Queue<GeneratedMessage> eSourceIStorage = new Queue<>(new LinkedBlockingQueue<GeneratedMessage>());
		Queue<GeneratedMessage> iStorageEEngine = new Queue<>(new LinkedBlockingQueue<GeneratedMessage>());

		ExternalDataSources external = new ExternalDataSources(sources);
		InternalDataStorage storage = new InternalDataStorage(external.getProtoBufParser());
		external.getDataInQueue(eSourceIStorage);
		storage.store(eSourceIStorage);
//		List<GeneratedMessage> data = storage.getData();
		storage.getDataInQueue(iStorageEEngine);
		System.out.println(iStorageEEngine.get());
	}
}
