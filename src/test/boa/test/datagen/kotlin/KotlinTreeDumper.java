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
package boa.test.datagen.kotlin;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.kotlin.psi.KtVisitor;


/*
 * @author rdyer
 */
public class KotlinTreeDumper extends KtVisitor<Void, Void> {
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

	@Override
	public void visitWhiteSpace(final PsiWhiteSpace space) {
	}
}
