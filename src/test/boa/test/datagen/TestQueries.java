package boa.test.datagen;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import boa.datagen.util.FileIO;
import boa.evaluator.BoaEvaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class TestQueries {

	@Test
	public void testBugFix() {
		String expected = "AddedNullCheck[] = 1\n";
		queryTest("test/known-good/bug-fix.boa", expected);
	}
	
	@Test
	public void testq20() {
		String expected = "TransientMax[] = 140492550, 3.0\n"
				+ "TransientMean[] = 0.0410958904109589\n"
				+ "TransientMin[] = 140492550, 0.0\n"
				+ "TransientTotal[] = 3\n"
				+ "VolatileMax[] = 140492550, 1.0\n"
				+ "VolatileMean[] = 0.0136986301369863\n"
				+ "VolatileMin[] = 140492550, 0.0\n"
				+ "VolatileTotal[] = 1\n";
		queryTest("test/known-good/q20.boa", expected);
	}

	@Test
	public void testAnnot_names() {
		String expected = "AnnotUse[FunctionalInterface] = 3\n"
				+ "AnnotUse[Override] = 11\n"
				+ "AnnotUse[Retention] = 1\n"
				+ "AnnotUse[SafeVarargs] = 1\n"
				+ "AnnotUse[SuppressWarnings] = 1\n"
				+ "AnnotUse[Target] = 2\n"
				+ "AnnotUse[ThreadSafe] = 1\n"
				+ "AnnotUse[ToDo] = 17\n";
		queryTest("test/known-good/annot-names.boa", expected);
	}
	
	@Test
	public void testAnnotations_define() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v1/ThreadSafe.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v2/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v3/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v4/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v5/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/ToDo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/ToDos.java][1531520725000000] = 1\n";
		queryTest("test/known-good/annotations-define.boa", expected);
	}
	
	@Test
	public void testAssert() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v1/AssertDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v2/AssertDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v3/AssertDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v3/AssertDemo.java][1532028844000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v3/AssertDemo.java][1532032882000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v4/AssertDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v4/AssertDemo.java][1532029015000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS2/AssertDemo/v4/AssertDemo.java][1532032882000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/TEDemo/v1/TEDemo.java][1531520725000000] = 1\n";
		queryTest("test/known-good/assert.boa", expected);
	}
	
	@Test
	public void testAnnotations_use() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v1/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v2/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v3/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v3/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v4/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v4/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v4/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v4/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v5/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v5/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v5/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v5/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnDemo/v5/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/AnnDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/AnnDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/AnnDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/ToDo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/AnnProcDemo/ToDo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/CovarDemo/v1/CovarDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/CovarDemo/v1/CovarDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/CovarDemo/v1/CovarDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/CovarDemo/v2/CovarDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/CovarDemo/v2/CovarDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS3/CovarDemo/v2/CovarDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/CustomARM/CustomARM.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/Planets/Planets.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/Planets/Planets.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/SortInts/v1/SortInts.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/SortInts/v1/SortInts.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/SortInts/v2/SortInts.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/SortInts/v2/SortInts.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/SupExDemo/SupExDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo.java][1532021515000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo.java][1532026609000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo.java][1532711730000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo.java][1532711801000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo.java][1532712183000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo.java][1532712227000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo2.java][1532029613000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/UnsafeVarargsDemo/UnsafeVarargsDemo2.java][1532032882000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v1/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v1/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v1/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v1/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v1/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v1/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v1/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v1/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v1/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v1/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v2/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v2/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v2/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v2/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v2/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v2/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v2/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v2/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v2/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v2/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v3/DMDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/DMDemo/v4/DMDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/IGTIDemo/IGTIDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v2/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v3/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v3/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v4/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v7/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/LambdaDemo/v8/LambdaDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/MRDemo/v2/MRDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/MRDemo/v3/MRDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/MRDemo/v6/MRDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/Account.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/Account.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/Account.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/Account.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/ToDo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/ToDos.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/RADemo/ToDos.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS8/SMDemo/SMDemo.java][1531520725000000] = 1\n";
		queryTest("test/known-good/annotations-use.boa", expected);
	}
	
	@Test
	public void testBinary_lit() {
		String expected = "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531520725000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n"
				+ "USES[https://github.com/boalang/test-datagen][src/JLS4/BinLitDemo/BinLitDemo.java][1531880750000000] = 1\n";
		queryTest("test/known-good/binary-lit.boa", expected);
	}
	
	@Test
	public void testProgramingLanguages() {
		String expected = "counts[] = Java, 1.0\n";
		queryTest("test/known-good/catAp1.boa", expected);
	}
	
	@Test
	public void testYearCreated() {
		String expected = "counts[2018] = 1\n";
		queryTest("test/known-good/catAp3.boa", expected);
	}
	
	@Test
	public void testYearJavaAdded() {
		String expected = "counts[2018] = 1\n";
		queryTest("test/known-good/catAp4.boa", expected);
	}
	
	@Test
	public void testNumberOfRevisions() {
		String expected = "counts[] = 49\n";
		queryTest("test/known-good/catBp1.boa", expected);
	}
	
	@Test
	public void testChurnRate() {
		String expected = "counts[] = 5.530612244897959\n";
		queryTest("test/known-good/catBp14.boa", expected);
	}
	
	@Test
	public void testcatBp13() {
		String expected = "counts[2018] = 1\n";
		queryTest("test/known-good/catBp13.boa", expected);
	}
	
	@Test
	public void testcatBp15() {
		String expected = "counts[140492550] = 5.530612244897959\n";
		queryTest("test/known-good/catBp15.boa", expected);
	}
	
	@Test
	public void testcatBp17() {
		String expected = "counts[2018] = 49\n";
		queryTest("test/known-good/catBp17.boa", expected);
	}
	
	@Test
	public void testcatBp18() {
		String expected = "counts[] = 4.346938775510204\n";
		queryTest("test/known-good/catBp18.boa", expected);
	}
	
	public String getResults(File outputDir) {
		for (final File f : outputDir.listFiles()) {
			if (f.getName().startsWith("part")) {
				return FileIO.readFileContents(f);
			}
		}
		return "";
	}
	
	public void queryTest(String inputPath, String expected) {
//		String[] args = {"./boa.sh -e",
//				 "-i " + inputPath,
//				 "-d test/datagen/test_datagen",
//				 "-o test/datagen/temp_output" };
		
		
		
		
		try {
			Process p = Runtime.getRuntime().exec("./boa.sh -e "
					+ "-i " + inputPath + " "
					+ "-d test/datagen/test_datagen "
					+ "-o test/datagen/temp_output");
			p.waitFor();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		File outputDir = new File("test/datagen/temp_output");
		String actual = getResults(outputDir);
		try {
			FileUtils.deleteDirectory(outputDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertEquals(expected, actual);
	}
}
