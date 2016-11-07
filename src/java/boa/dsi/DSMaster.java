package boa.dsi;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.dsi.dsource.DataSources;
import boa.dsi.evaluator.EvaluationEngine;
import boa.dsi.storage.InternalDataStorage;

public class DSMaster {
	public static void main(String[] args) {
		// String url = "https://github.com/boalang/compiler";
		// String program =
		// "/Users/nmtiwari/git/research/boa_platform/compiler/example.boa";
		// String output = "/Users/nmtiwari/Desktop/boaout";

		String url = args[0];
		String program = args[1];
		String output = args[2];

		ArrayList<String> sources = new ArrayList<String>();
		sources.add(url);

		Queue<GeneratedMessage> eSourceIStorage = new Queue<GeneratedMessage>(
				new LinkedBlockingQueue<GeneratedMessage>());

		DataSources external = new DataSources(sources);
		InternalDataStorage storage = new InternalDataStorage(external.getProtoBufParser());
		EvaluationEngine engine = new EvaluationEngine(program, storage.getDataLocation(), output);

//		external.getDataInQueue(eSourceIStorage);
//		storage.store(eSourceIStorage);
		engine.evaluate();
		if (engine.isSuccess()) {
			System.out.println();
			System.out.println(engine.getResult());
		}
		// storage.getDataInQueue(iStorageEEngine);
		// System.out.println(iStorageEEngine.get());
	}
}
