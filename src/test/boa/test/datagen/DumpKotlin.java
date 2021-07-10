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

import java.io.File;

import com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtVisitor;
import com.intellij.core.CoreFileTypeRegistry;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.StandardFileSystems;
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

/*
 * @author rdyer
 */
public class DumpKotlin {
	public static class KotlinTreeDumper extends KtVisitor<Void, Void> {
		private int indent = 0;

		private void indent() {
			for (int i = 0; i < indent * 2; i++)
				System.err.print(" ");
		}

		@Override
		public void visitElement(final PsiElement element) {
			indent();
			System.err.print(element);
			if (element instanceof org.jetbrains.kotlin.psi.KtConstantExpression)
				System.err.print("(" + ((org.jetbrains.kotlin.psi.KtConstantExpression)element).getText() + ")");
			else if (element instanceof org.jetbrains.kotlin.psi.KtBinaryExpression)
				System.err.print("(" + ((org.jetbrains.kotlin.psi.KtBinaryExpression)element).getOperationToken() + ")");
			else if (element instanceof com.intellij.psi.impl.source.tree.LeafPsiElement)
				System.err.print("(" + ((com.intellij.psi.impl.source.tree.LeafPsiElement)element).getText() + ")");
			System.err.println(" - " + element.getClass());
			indent++;
			element.acceptChildren(this);
			indent--;
		}
	}

	public static void main(String[] args) {
		final KotlinTreeDumper v = new KotlinTreeDumper();

		final String path = "cmdline.kt";
		final String content = args[0];

		KtFile theKt = null;

		try {
			final Disposable disp = Disposer.newDisposable();
			final KotlinCoreApplicationEnvironment kae = KotlinCoreApplicationEnvironment.create(disp, false);
			final KotlinCoreProjectEnvironment kpe = new KotlinCoreProjectEnvironment(disp, kae);
			final Project proj = kpe.getProject();
			((CoreFileTypeRegistry)FileTypeRegistry.getInstance()).registerFileType(KotlinFileType.INSTANCE, "kt");
			LanguageParserDefinitions.INSTANCE.addExplicitExtension(KotlinLanguage.INSTANCE,
										new KotlinParserDefinition());
			final PsiManager kProjectManager = PsiManager.getInstance(proj);

			final VirtualFile file = new LightVirtualFile(path, KotlinFileType.INSTANCE, content);
			theKt = new KtFile(kProjectManager.findViewProvider(file), false);
			theKt.accept(v);
		} catch (final Throwable e) {
		}
	}
}
