package boa;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.dsi.dsource.fars.FARS;
import boa.dsi.dsource.github.GithubReader;
import boa.dsi.evaluator.EvaluationEngine;
import boa.dsi.storage.MSRDataStorage;
import boa.dsi.storage.sequencefile.SequenceFileStorage;

public class DSMaster {
	public static void main(String[] args) {
		// String url = "https://github.com/boalang/compiler";
		// String program =
		// "/Users/nmtiwari/git/research/boa_platform/compiler/example.boa";
		// String output = "/Users/nmtiwari/Desktop/boaout";

		String url = args[0];
		String program = args[1];
		String output = args[2];

		String accident = "/Users/nmtiwari/git/research/boa_platform/trans_data/accident.dbf";
		String person = "/Users/nmtiwari/git/research/boa_platform/trans_data/person.dbf";
		String vehicle = "/Users/nmtiwari/git/research/boa_platform/trans_data/vehicle.dbf";

		ArrayList<String> sources = new ArrayList<String>();
		sources.add(accident);
		sources.add(person);
		sources.add(vehicle);

		Queue<GeneratedMessage> eSourceIStorage = new Queue<GeneratedMessage>(
				new LinkedBlockingQueue<GeneratedMessage>());

		FARS external = new FARS(sources);
		SequenceFileStorage storage = new SequenceFileStorage(external);
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
