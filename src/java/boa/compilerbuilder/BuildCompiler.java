package boa.compilerbuilder;

public class BuildCompiler {
	private BuildCompiler instance;
    private BuildCompiler(){
    	this.instance = new BuildCompiler();
    }
    
    public BuildCompiler getInstance(){
    	return this.instance;
    }
    
    private boolean generateReqFiles(){
    	
    }
}
