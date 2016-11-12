package boa.dsi.evaluator;

import boa.dsi.DSIProperties;
import boa.evaluator.BoaEvaluator;

public class EvaluationEngine {
	private BoaEvaluator evaluator;
	private boolean isSuccess;

	public EvaluationEngine(String prog, String data, String output) {
		evaluator = new BoaEvaluator(prog, data, output);
		DSIProperties.BOA_OUT = output;
	}

	public boolean evaluate() {
		 isSuccess = evaluator.evaluate();
		 return isSuccess;
	}

	public String getResult() {
		return evaluator.getResults();
	}
	
	public boolean isSuccess(){
		return this.isSuccess;
	}

}
