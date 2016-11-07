package boa.dsi.evaluator;

import boa.dsi.dsource.DatagenProperties;
import boa.evaluator.BoaEvaluator;

public class EvaluationEngine {
	private BoaEvaluator evaluator;
	private boolean isSuccess;

	public EvaluationEngine(String prog, String data, String output) {
		evaluator = new BoaEvaluator(prog, data);
		DatagenProperties.BOA_OUT = output;
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
