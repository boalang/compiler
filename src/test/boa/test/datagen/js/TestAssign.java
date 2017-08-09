package boa.test.datagen;

import java.io.IOException;

import org.junit.Test;

public class TestAssign extends JavaScriptBaseTest {

	@Test
	public void assignTest1() throws IOException{
		nodeTest( load("test/datagen/javascript/AssignNode.boa"), load("test/datagen/javascript/AssignNode.js"));
	}
	
	@Test
	public void assignAddTest1() throws IOException{
		nodeTest( load("test/datagen/javascript/AssignAddNode.boa"), load("test/datagen/javascript/AssignAddNode.js"));
	}
	
	@Test
	public void assignDecTest1() throws IOException{
		nodeTest( load("test/datagen/javascript/AssignDecNode.boa"), load("test/datagen/javascript/AssignDecNode.js"));
	}
	
	@Test
	public void assignMultTest1() throws IOException{
		nodeTest( load("test/datagen/javascript/AssignMultNode.boa"), load("test/datagen/javascript/AssignMultNode.js"));
	}
	
	@Test
	public void assignBitAndTest1() throws IOException{
		nodeTest( load("test/datagen/javascript/AssignBitAndNode.boa"), load("test/datagen/javascript/AssignBitAndNode.js"));
	}
	
}
