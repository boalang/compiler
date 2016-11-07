package boa.dsi.evaluator;

import boa.dsi.DSComponent;

public abstract class AbstractEvaluationEngine implements DSComponent {
	protected String inputProgram;
	protected String inputData;

	public AbstractEvaluationEngine(String prog, String data) {
		this.inputProgram = prog;
		this.inputData = data;
	}

	public String getInputProgram() {
		return inputProgram;
	}

	public String getInputData() {
		return inputData;
	}

	public void setInputProgram(String newProg) {
		this.inputProgram = newProg;
	}

	public void setInputData(String data) {
		this.inputData = data;
	}

	public abstract boolean evaluate();

	public abstract String getResults();
	
	public abstract boolean isSuccess();
}
