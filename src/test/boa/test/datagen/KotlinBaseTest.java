/*
 * Copyright 2021, Robert Dyer, Samuel W. Flint,
 *                 and University of Nebraska Board of Regents
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package boa.test.datagen;

import com.googlecode.protobuf.format.JsonFormat;

import org.jetbrains.kotlin.psi.KtFile;

import boa.datagen.util.FileIO;
import boa.datagen.util.KotlinVisitor;
import boa.functions.langmode.KotlinLangMode;
import boa.test.compiler.BaseTest;
import boa.types.Ast.ASTRoot;

/*
 * @author rdyer
 * @author swflint
 */
public class KotlinBaseTest extends BaseTest {
	protected static void dumpKotlin(final String content) {
		dumpKotlin(content, false);
	}

	private final static KotlinTreeDumper treeDumper = new KotlinTreeDumper();

	protected static void dumpKotlin(final String content, final boolean showEx) {
		final KtFile theKt = KotlinLangMode.tryparse("test.kt", content, showEx);
		if (theKt == null) return;
		theKt.accept(treeDumper);
	}

	protected static String parseKotlin(final String content) {
		return parseKotlin(content, false);
	}

	private static final KotlinVisitor ktToBoa = new KotlinVisitor();

	protected static String parseKotlin(final String content, final boolean showEx) {
		final KtFile theKt = KotlinLangMode.tryparse("test.kt", content, showEx);
		if (theKt == null) return "";

		final ASTRoot.Builder ast = ASTRoot.newBuilder();
		ast.addNamespaces(ktToBoa.getNamespace(theKt));

		return FileIO.normalizeEOL(JsonFormat.printToString(ast.build()));
	}
}
