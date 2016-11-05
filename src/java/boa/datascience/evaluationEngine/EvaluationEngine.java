package boa.datascience.evaluationEngine;

import java.util.List;

import com.google.protobuf.GeneratedMessage;

import boa.datascience.externalDataSources.DatagenProperties;
import boa.evaluator.BoaEvaluator;

public class EvaluationEngine {
	private BoaEvaluator evaluator;

	public EvaluationEngine(String prog, String data, String output) {
		evaluator = new BoaEvaluator(prog, data);
		DatagenProperties.BOA_OUT = output;
	}

	public boolean evaluate() {
		return evaluator.evaluate();
	}

	public String getResult() {
		return evaluator.getResults();
	}
	
	public boolean isSuccess(){
		return this.isSuccess();
	}

}
