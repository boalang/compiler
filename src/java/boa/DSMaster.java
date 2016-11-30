package boa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.compilerbuilder.BuildCompiler;
import boa.dsi.dsource.fars.FARS;
import boa.dsi.evaluator.EvaluationEngine;
import boa.dsi.storage.sequencefile.SequenceFileStorage;

public class DSMaster {
	
	public static void main(String[] args) {
		
		/*
		 *  Compile and Build new Domain Data types
		 */
		BuildCompiler builder = new BuildCompiler("/Users/nmtiwari/Desktop/msr/transport");
		try {
			builder.compileAndBuild();
		} catch (IOException e) {
			e.printStackTrace();
			return; 
		}
		
		
		/*
		 * We have created a newer version of compiler to support the domain types specified for 
		 * the builder above. Now we can trigger some queries as long as the schema of input data
		 * matches the schema expressed in above given proto files.
		 */
		
		
		
		String program = args[0]; // which boa program to run
		String inputlocation = args[1];  // where are the inputs stored
		String output = args[2]; // where should the output go

		/*
		 * In case we need to create the data from scratch, then what are the raw data sources
		 */
		ArrayList<String> sources = new ArrayList<String>();
		
		String accident = "/Users/nmtiwari/git/research/boa_platform/trans_data/accident.dbf";
		String person = "/Users/nmtiwari/git/research/boa_platform/trans_data/person.dbf";
		String vehicle = "/Users/nmtiwari/git/research/boa_platform/trans_data/vehicle.dbf";
		sources.add(accident);
		sources.add(person);
		sources.add(vehicle);
		
		/*   Raw data set ends */ 

		// Create the external data source reader for above mentioned raw data sources
		FARS external = new FARS(sources);
		
		// what kind of storage system are you interested in using
		// inputlocation:  where to save processed data
		// external : Which AbstractDataSource to use to parse the data
		SequenceFileStorage storage = new SequenceFileStorage(inputlocation, external);
		
		// what kind of evaluation engine to be used in here. Currently using Boa
		// program: boa program to use
		// arg2: where is input data
		// output: where to write output
		EvaluationEngine engine = new EvaluationEngine(program, storage.getDataLocation(), output);

		
		/*
		 * All the components of the system are added and connected to each other with proper communication interfacing.
		 * Now we can start reading raw data and convert it to processed form, store it using aforementioned storage technology
		 * and evaluate the queries over stored data.
		 */
		
		/* 
		 * Queue<GeneratedMessage> eSourceIStorage = new Queue<GeneratedMessage>(new LinkedBlockingQueue<GeneratedMessage>());
		 * external.getDataInQueue(eSourceIStorage);
		 * storage.store(eSourceIStorage);
		 */
		
		
		// evaluate the queries using engine 
		engine.evaluate();
		
		/*
		 * if succesfully evalauted then display the results 
		 */
		if (engine.isSuccess()) {
			System.out.println();
			System.out.println(engine.getResult());
		}
		// storage.getDataInQueue(iStorageEEngine);
		// System.out.println(iStorageEEngine.get());
	}
}
