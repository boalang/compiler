package boa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;


import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

import boa.dsi.dsource.fars.FARS;
import boa.dsi.evaluator.EvaluationEngine;
import boa.dsi.storage.sequencefile.SequenceFileStorage;

public class DSMaster {
	
	public static void main(String[] args) {
		
		/*
		 * We have created a newer version of compiler to support the domain types specified for 
		 * the builder above. Now we can trigger some queries as long as the schema of input data
		 * matches the schema expressed in above given proto files.
		 */
		
		
		/*
		 * Generic Inputs
		 */
//		String program = args[0]; // which boa program to run
//		String inputlocation = args[1];  // where are the inputs stored
//		String output = args[2]; // where should the output go
		
		/*
		 * MSR Input
		 */
//		String program = "/Users/nmtiwari/git/research/boa_platform/compiler/ast.boa"; // which boa program to run
//		String inputlocation = "/Users/nmtiwari/git/research/boa_platform/msrdata/";  // where are the inputs stored
//		String output = " /Users/nmtiwari/git/research/boa_platform /Users/nmtiwari/Desktop/new/out"; // where should the output go
		
		
		/*
		 * Transportation Input
		 */
		//Weather Effect On Speed /Users/mislam/Desktop/datascienceInf/compiler/transportationExamples/weatherEffectOnSpeed.boa
		// weatherEffectInCounty, weatherEffectOnSpeed, avgSpeedAtDifferentTimesinCounty,avgSpeedAtDifferentTimesinCounty,gridinfo,
		//weatherEffectonAvgSpeed,locationsWithHigherSpeed,weatherEffectonAvgSpeed,avgSpeedAtDifferentTimesinCounty,
		//higherSpeedAtBadWeather,weatherSpeed
		args = new String[2];
		args[0] ="/Users/mislam/Desktop/boa/compiler/transportationExamples/locationsWithHigherSpeed.boa";
		String program = args[0]; // which boa program to run
		String inputlocation = "/Users/mislam/Desktop/boa";  // where are the inputs stored
		String output = "/Users/mislam/Desktop/new/out"; // where should the output go
		

		/*
		 * In case we need to create the data from scratch, then what are the raw data sources
		 */
		ArrayList<String> sources = new ArrayList<String>();
		
		String accident = "/Users/mislam/Desktop/Traffic Shared Data/FARS/2015/FARS2015NationalDBF/accident.dbf";
		String person = "/Users/mislam/Desktop/Traffic Shared Data/FARS/2015/FARS2015NationalDBF/person.dbf";
		String vehicle = "/Users/mislam/Desktop/Traffic Shared Data/FARS/2015/FARS2015NationalDBF/vehicle.dbf";
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
		
		 
		/**
		 * Generate Sequence File for Top level
		 */
//		  Queue<GeneratedMessage> toplevel = new Queue<GeneratedMessage>(new LinkedBlockingQueue<GeneratedMessage>());
//		  external.getDataInQueue(toplevel);
//		  storage.store(toplevel);
		  
		  
		  /**
		   * Generate Sequence file for Speed
		   */
//		  Queue<GeneratedMessage> speed = new Queue<GeneratedMessage>(new LinkedBlockingQueue<GeneratedMessage>());
//		  external.getSpeedInQueue(speed);
//		  storage.storeSpeed(speed);
		  
		  
		  /**
		   * Generate Sequence file for weather
		   */
		  
		  Queue<GeneratedMessage> weather = new Queue<GeneratedMessage>(new LinkedBlockingQueue<GeneratedMessage>());
//		  external.getWeatherInQueue(weather);
//		  storage.storeWeather(weather);
//		 
		
		
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