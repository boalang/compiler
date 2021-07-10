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

import com.intellij.core.CoreFileTypeRegistry;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.parsing.KotlinParserDefinition;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreProjectEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironment;

import boa.datagen.util.FileIO;
import boa.datagen.util.KotlinVisitor;
import boa.test.compiler.BaseTest;
import boa.types.Ast.ASTRoot;

/*
 * @author rdyer
 * @author swflint
 */
public class KotlinBaseTest extends BaseTest {
	private static PsiManager kProjectManager = null;

	protected static KtFile getKtFile(final String content) throws Exception {
		if (kProjectManager == null) {
			final Disposable disp = Disposer.newDisposable();
			final KotlinCoreApplicationEnvironment kae = KotlinCoreApplicationEnvironment.create(disp, false);
			final KotlinCoreProjectEnvironment kpe = new KotlinCoreProjectEnvironment(disp, kae);
			final Project proj = kpe.getProject();
			((CoreFileTypeRegistry)FileTypeRegistry.getInstance()).registerFileType(KotlinFileType.INSTANCE, "kt");
			LanguageParserDefinitions.INSTANCE.addExplicitExtension(KotlinLanguage.INSTANCE,
										new KotlinParserDefinition());
			kProjectManager = PsiManager.getInstance(proj);
		}

		final VirtualFile file = new LightVirtualFile("test.kt", KotlinFileType.INSTANCE, content);
		return new KtFile(kProjectManager.findViewProvider(file), false);
	}

	private final static KotlinTreeDumper v = new KotlinTreeDumper();

	protected static void dumpKotlin(final String content) {
		dumpKotlin(content, false);
	}

	protected static void dumpKotlin(final String content, final boolean showEx) {
		try {
			getKtFile(content).accept(v);
		} catch (final Throwable t) {
			if (showEx) t.printStackTrace();
		}
	}

	protected static String parseKotlin(final String content) {
		return parseKotlin(content, false);
	}

	protected static String parseKotlin(final String content, final boolean showEx) {
		try {
			final KotlinVisitor visitor = new KotlinVisitor();
			final ASTRoot.Builder ast = ASTRoot.newBuilder();

			ast.addNamespaces(visitor.getNamespace(getKtFile(content)));

			return FileIO.normalizeEOL(JsonFormat.printToString(ast.build()));
		} catch (final Throwable t) {
			if (showEx) t.printStackTrace();
			return "";
		}
	}
}
