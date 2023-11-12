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

package boa.datagen.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.PsiErrorElementImpl;

import org.jetbrains.kotlin.psi.KtFile;

/**
 * @author rdyer
 */
public class KotlinErrorCheckVisitor extends PsiElementVisitor {
	private boolean hasError = false;

	public boolean hasError(final KtFile kt) {
		hasError = kt == null;
		if (!hasError)
			kt.accept(this);
		return hasError;
	}

	@Override
	public void visitElement(final PsiElement element) {
		if (element instanceof PsiErrorElementImpl)
			hasError = true;
		if (!hasError)
			element.acceptChildren(this);
	}
}
