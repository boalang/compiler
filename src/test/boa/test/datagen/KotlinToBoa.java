/*
 * Copyright 2021, Robert Dyer
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

import java.util.List;

import com.googlecode.protobuf.format.JsonFormat;

import kotlin.Unit;
import kotlinx.ast.common.ast.Ast;
import kotlinx.ast.common.AstResult;
import kotlinx.ast.common.AstSource;
import kotlinx.ast.grammar.kotlin.common.SummaryKt;
import kotlinx.ast.grammar.kotlin.target.antlr.java.KotlinGrammarAntlrJavaParser;

import boa.datagen.util.FileIO;
import boa.datagen.util.KotlinVisitor;
import boa.types.Ast.ASTRoot;

/*
 * @author rdyer
 */
public class KotlinToBoa {
	protected static String parseKotlin(final String content) {
			final StringBuilder sb = new StringBuilder();

			final AstSource source = new AstSource.String("", content);
			final AstResult<Unit, List<Ast>> astList = SummaryKt.summary(KotlinGrammarAntlrJavaParser.INSTANCE.parseKotlinFile(source), true);

			final KotlinVisitor visitor = new KotlinVisitor();
			final ASTRoot.Builder ast = ASTRoot.newBuilder();

			ast.addNamespaces(visitor.getNamespaces(astList.get()));
			sb.append(JsonFormat.printToString(ast.build()));

			return FileIO.normalizeEOL(sb.toString());
	}

	public static void main(String[] args) {
		for (final String s : args)
			System.out.println(parseKotlin(s));
	}
}
