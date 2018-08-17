package boa.test.datagen.queries;

import org.junit.Test;

public class TestImports extends QueryTest{

	@Test
	public void testImports() {
		String expected = "imports[] = java.util.List, 9.0\n"
				+ "imports[] = java.util.ArrayList, 6.0\n"
				+ "imports[] = java.util.Arrays, 6.0\n"
				+ "imports[] = java.io.IOException, 5.0\n"
				+ "imports[] = java.util.Iterator, 3.0\n"
				+ "imports[] = java.util.function.Consumer, 3.0\n"
				+ "imports[] = java.io.File, 2.0\n"
				+ "imports[] = java.io.FileInputStream, 2.0\n"
				+ "imports[] = java.lang.annotation.ElementType, 2.0\n"
				+ "imports[] = java.lang.annotation.Target, 2.0\n"
				+ "imports[] = java.math.BigDecimal, 2.0\n"
				+ "imports[] = java.util.function.Function, 2.0\n"
				+ "imports[] = java.util.function.Predicate, 2.0\n"
				+ "imports[] = java.util.function.Supplier, 2.0\n"
				+ "imports[] = java.awt.AWTException, 1.0\n"
				+ "imports[] = java.io.*, 1.0\n"
				+ "imports[] = java.io.Closeable, 1.0\n"
				+ "imports[] = java.io.FileFilter, 1.0\n"
				+ "imports[] = java.io.FileNotFoundException, 1.0\n"
				+ "imports[] = java.io.FileOutputStream, 1.0\n"
				+ "imports[] = java.io.InputStream, 1.0\n"
				+ "imports[] = java.lang.annotation.Retention, 1.0\n"
				+ "imports[] = java.lang.annotation.RetentionPolicy, 1.0\n"
				+ "imports[] = java.nio.file.FileSystem, 1.0\n"
				+ "imports[] = java.nio.file.FileSystems, 1.0\n"
				+ "imports[] = java.nio.file.FileVisitResult, 1.0\n"
				+ "imports[] = java.nio.file.FileVisitor, 1.0\n"
				+ "imports[] = java.nio.file.Files, 1.0\n"
				+ "imports[] = java.nio.file.Path, 1.0\n"
				+ "imports[] = java.nio.file.PathMatcher, 1.0\n"
				+ "imports[] = java.nio.file.Paths, 1.0\n"
				+ "imports[] = java.nio.file.SimpleFileVisitor, 1.0\n"
				+ "imports[] = java.nio.file.attribute.BasicFileAttributes, 1.0\n"
				+ "imports[] = java.security.AccessController, 1.0\n"
				+ "imports[] = java.security.PrivilegedAction, 1.0\n"
				+ "imports[] = java.sql.SQLException, 1.0\n"
				+ "imports[] = java.util.Collections, 1.0\n"
				+ "imports[] = java.util.Comparator, 1.0\n"
				+ "imports[] = java.util.Date, 1.0\n"
				+ "imports[] = java.util.concurrent.Callable, 1.0\n"
				+ "imports[] = static foo.Switchable.*, 1.0\n";
		queryTest("test/known-good/imports.boa", expected);
	}
}
