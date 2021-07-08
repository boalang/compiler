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

// import kotlin.Unit;
// import kotlinx.ast.common.ast.Ast;
// import kotlinx.ast.common.AstResult;
// import kotlinx.ast.common.AstSource;
// import kotlinx.ast.grammar.kotlin.common.SummaryKt;
// import kotlinx.ast.grammar.kotlin.target.antlr.java.KotlinGrammarAntlrJavaParser;

import boa.datagen.util.FileIO;
import boa.datagen.util.KotlinVisitor;
import boa.types.Ast.ASTRoot;


// Dependencies for Kotlin
import java.io.*;
import org.jetbrains.kotlin.psi.KtFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.core.CoreFileTypeRegistry;
import com.intellij.lang.LanguageParserDefinitions;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.parsing.KotlinParserDefinition;
import com.intellij.psi.PsiManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreProjectEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironment;
import org.jetbrains.kotlin.extensions.PreprocessedFileCreator;


/*
 * @author rdyer
 */
public class KotlinToBoa {
	private static boolean kotlinParserPrepared = false;
	private static VirtualFileSystem kVirtFileSys;
	private static PsiManager kProjectManager;

	private static KtFile getKtFile(final String content) throws IOException {
		if (!kotlinParserPrepared) {
			Disposable disp = Disposer.newDisposable();
			KotlinCoreApplicationEnvironment kae = KotlinCoreApplicationEnvironment.create(disp, false);
			KotlinCoreProjectEnvironment kpe = new KotlinCoreProjectEnvironment(disp, kae);
			Project proj = kpe.getProject();
			((CoreFileTypeRegistry) FileTypeRegistry.getInstance()).registerFileType(KotlinFileType.INSTANCE, "kt");
			LanguageParserDefinitions.INSTANCE.addExplicitExtension(KotlinLanguage.INSTANCE,
										new KotlinParserDefinition());
			kVirtFileSys = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL);
			kProjectManager = PsiManager.getInstance(proj);
			kotlinParserPrepared = true;
		}
		File theFile = File.createTempFile("test-kotlin", ".kt");
                FileWriter fw = new FileWriter(theFile);
		fw.write(content);
		fw.close();
		VirtualFile file = kVirtFileSys.findFileByPath(theFile.getAbsolutePath());
		KtFile kt = new KtFile(kProjectManager.findViewProvider(file), false);
		return kt;
	}

	protected static String parseKotlin(final String content) {
			final StringBuilder sb = new StringBuilder();

			// final AstSource source = new AstSource.String("", content);
// 			final AstResult<Unit, List<Ast>> astList = SummaryKt.summary(KotlinGrammarAntlrJavaParser.INSTANCE.parseKotlinFile(source), true);
// for (final Ast ast : astList.get())
//     kotlinx.ast.common.PrintKt.print(ast);
// System.out.println("");

			KtFile kt = null;

			try {
				kt = getKtFile(content);
			}
                        catch (Throwable t) {
				return "";
			}


			final KotlinVisitor visitor = new KotlinVisitor();
			final ASTRoot.Builder ast = ASTRoot.newBuilder();

			ast.addNamespaces(visitor.getNamespace(kt));
			sb.append(JsonFormat.printToString(ast.build()));

			return FileIO.normalizeEOL(sb.toString());
	}

	public static void main(String[] args) {
		for (final String s : args)
			System.out.println(parseKotlin(s));
	}
}
