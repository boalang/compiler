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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.googlecode.protobuf.format.JsonFormat;

import com.intellij.core.CoreFileTypeRegistry;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiManager;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.idea.KotlinFileType;
import org.jetbrains.kotlin.parsing.KotlinParserDefinition;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreProjectEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreApplicationEnvironment;
import org.jetbrains.kotlin.extensions.PreprocessedFileCreator;

import boa.datagen.util.FileIO;
import boa.datagen.util.KotlinVisitor;
import boa.types.Ast.ASTRoot;


/*
 * @author rdyer
 * @author swflint
 */
public class KotlinToBoa {
	private static VirtualFileSystem kVirtFileSys = null;
	private static PsiManager kProjectManager = null;

	protected static String parseKotlin(final String content) {
		final StringBuilder sb = new StringBuilder();

		try {
			if (kVirtFileSys == null || kProjectManager == null) {
				final Disposable disp = Disposer.newDisposable();
				final KotlinCoreApplicationEnvironment kae = KotlinCoreApplicationEnvironment.create(disp, false);
				final KotlinCoreProjectEnvironment kpe = new KotlinCoreProjectEnvironment(disp, kae);
				final Project proj = kpe.getProject();
				((CoreFileTypeRegistry)FileTypeRegistry.getInstance()).registerFileType(KotlinFileType.INSTANCE, "kt");
				LanguageParserDefinitions.INSTANCE.addExplicitExtension(KotlinLanguage.INSTANCE,
											new KotlinParserDefinition());
				kVirtFileSys = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL);
				kProjectManager = PsiManager.getInstance(proj);
			}
		
			final File theFile = File.createTempFile("test-kotlin", ".kt");
			final FileWriter fw = new FileWriter(theFile);
			fw.write(content);
			fw.close();
			final VirtualFile file = kVirtFileSys.findFileByPath(theFile.getAbsolutePath());
			KtFile kt = new KtFile(kProjectManager.findViewProvider(file), false);

			final KotlinVisitor visitor = new KotlinVisitor();
			final ASTRoot.Builder ast = ASTRoot.newBuilder();

			ast.addNamespaces(visitor.getNamespace(kt));
			sb.append(JsonFormat.printToString(ast.build()));

			theFile.delete();
		} catch (final Throwable t) {
			return "";
		}

		return FileIO.normalizeEOL(sb.toString());
	}

	public static void main(String[] args) {
		for (final String s : args)
			System.out.println(parseKotlin(s));
	}
}
