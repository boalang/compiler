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
import boa.compiler.ast.statements.VarDeclStatement;
import boa.types.BoaOutputType;

import java.util.List;
import java.util.ArrayList;

/**
 * Finds if the expression is a Call.
 * 
 * @author rdyer
 * @author hungc
 */
public class ViewFindingVisitor extends AbstractVisitorNoArgNoRet {
	int scopeLevel;
	List<String> referencedOutputs;
	List<String> subViews;
	List<String> externalViews;
	List<String> subViewPaths;
	List<String> localSubViews;
	List<String> localExternalViews;
	List<String> localSubViewPaths;

	public ViewFindingVisitor() {
		initialize();
	}

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		this.scopeLevel = 0;
		this.referencedOutputs = new ArrayList<String>();
		this.subViews = new ArrayList<String>();
		this.externalViews = new ArrayList<String>();
		this.subViewPaths = new ArrayList<String>();
		this.localSubViews = new ArrayList<String>();
		this.localExternalViews = new ArrayList<String>();
		this.localSubViewPaths = new ArrayList<String>();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Table n) {
		if (scopeLevel == 0) {
			if (n.getJobNum() != null) {
				localExternalViews.add(n.getJobNum());
				localSubViewPaths.add(n.getSubViewPath());
			}
			else if (n.getUserName() != null) {
				localExternalViews.add(n.getUserName() + "/" + n.getViewName());
				localSubViewPaths.add(n.getSubViewPath());
			}
			else {
				localSubViews.add(n.getSubViewPath());
				referencedOutputs.add(n.getSubViewPath() + "/output/" + n.getOutputName());
			}
		}
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
	public void visit(final VarDeclStatement n) {
		if (scopeLevel == 0 && n.hasType() && n.type instanceof BoaOutputType)
			referencedOutputs.add("output/" + n.getId().getToken());

		n.getId().accept(this);
		if (n.hasType())
			n.getType().accept(this);
		if (n.hasInitializer())
			n.getInitializer().accept(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final SubView n) {
		scopeLevel++;
		n.getProgram().accept(this);
		scopeLevel--;
	}

	public List<String> getReferencedOutputs() {
		return this.referencedOutputs;
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

	public String getLocalSubView(int i) {
		return this.localSubViews.get(i);
	}

	public String getLocalExternalView(int i) {
		return this.localExternalViews.get(i);
	}
	public String getLocalSubViewPath(int i) {
		return this.localSubViewPaths.get(i);
	}

	public List<String> getLocalSubViews() {
		return this.localSubViews;
	}

	public List<String> getLocalExternalViews() {
		return this.localExternalViews;
	}
	public List<String> getLocalSubViewPaths() {
		return this.localSubViewPaths;
	}

	public void reset() {
		this.scopeLevel = 0;
		this.referencedOutputs.clear();
		this.subViews.clear();
		this.externalViews.clear();
		this.subViewPaths.clear();
		this.localSubViews.clear();
		this.localExternalViews.clear();
		this.localSubViewPaths.clear();
	}
}
