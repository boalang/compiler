package boa.datascience.evaluationEngine;

import java.util.List;

import com.google.protobuf.GeneratedMessage;

import boa.evaluator.BoaEvaluator;

public class EvaluationEngine {
	private BoaEvaluator evaluator;
	
	public EvaluationEngine(String prog, String data) {
		evaluator = new BoaEvaluator(prog, data);
	}

	public boolean evaluate() {
		return evaluator.evaluate();
	}

	public List<GeneratedMessage> getData() {
		return evaluator.getData();
	}
	
}
