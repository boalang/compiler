/*
 * Copyright 2017, Robert Dyer, Che Shian Hung,
 *                 and Bowling Green State University
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
package boa.compiler.visitors;

import boa.compiler.ast.Table;
import boa.compiler.ast.statements.SubView;

import java.util.List;
import java.util.ArrayList;

/**
 * Finds if the expression is a Call.
 * 
 * @author rdyer
 * @author hungc
 */
public class ViewFindingVisitor extends AbstractVisitorNoArgNoRet {
	Boolean isLocal;
	List<String> subViews;
	List<String> externalViews;
	List<String> subViewPaths;

	public ViewFindingVisitor() {
		this(false);
	}

	public ViewFindingVisitor(Boolean isLocal) {
		initialize();
		this.isLocal = isLocal;
	}

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		this.subViews = new ArrayList<String>();
		this.externalViews = new ArrayList<String>();
		this.subViewPaths = new ArrayList<String>();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Table n) {
		if (n.getJobNum() != null) {
			externalViews.add(n.getJobNum());
			subViewPaths.add(n.getSubViewPath());
		}
		else if (n.getUserName() != null) {
			externalViews.add(n.getUserName() + "/" + n.getViewName());
			subViewPaths.add(n.getSubViewPath());
		}
		else {
			subViews.add(n.getSubViewPath());
		}

	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SubView n) {
		if (!this.isLocal)
			n.getProgram().accept(this);
	}

	public void setIsLocal(final Boolean b) {
		this.isLocal = b;
	}

	public String getSubView(int i) {
		return this.subViews.get(i);
	}

	public String getExternalView(int i) {
		return this.externalViews.get(i);
	}
	public String getSubViewPath(int i) {
		return this.subViewPaths.get(i);
	}

	public List<String> getSubViews() {
		return this.subViews;
	}

	public List<String> getExternalViews() {
		return this.externalViews;
	}
	public List<String> getSubViewPaths() {
		return this.subViewPaths;
	}

	public void reset() {
		this.subViews.clear();
		this.externalViews.clear();
		this.subViewPaths.clear();
	}
}
