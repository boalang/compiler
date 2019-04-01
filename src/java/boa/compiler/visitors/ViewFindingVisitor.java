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

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * Finds if the expression is a Call.
 * 
 * @author rdyer
 * @author hungc
 */
public class ViewFindingVisitor extends AbstractVisitorNoArgNoRet {
	final boolean GET_VIEW_NAME = true;
	final boolean GET_SUBVIEW_PATH = false;

	int scopeLevel;
	Set<String> localSubViewNames;
	List<String> referencedOutputs;
	List<String> subViews;
	List<String> localSubViews;
	Map<String, Set<String>> externalViews;
	Map<String, Set<String>> localExternalViews;

	public ViewFindingVisitor() {
		initialize();
	}

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		this.scopeLevel = 0;
		this.localSubViewNames = new HashSet<String>();
		this.referencedOutputs = new ArrayList<String>();
		this.subViews = new ArrayList<String>();
		this.localSubViews = new ArrayList<String>();
		this.externalViews = new LinkedHashMap<String, Set<String>>();
		this.localExternalViews = new LinkedHashMap<String, Set<String>>();
	}

	/** {@inheritDoc} */
	@Override
	public void visit(final Table n) {
		if (scopeLevel == 0) {
			if (n.getJobNum() != null)
				addViewMap(localExternalViews, n.getJobNum(), n.getSubViewPath());
			else if (n.getUserName() != null)
				addViewMap(localExternalViews, n.getUserName() + "/" + n.getViewName(), n.getSubViewPath());
			else {
				if (!localSubViews.contains(n.getSubViewPath())) {
					localSubViews.add(n.getSubViewPath());
					referencedOutputs.add(n.getSubViewPath() + "/output/" + n.getOutputName());
				}
			}
		}
		if (n.getJobNum() != null)
			addViewMap(externalViews, n.getJobNum(), n.getSubViewPath());
		else if (n.getUserName() != null)
			addViewMap(externalViews, n.getUserName() + "/" + n.getViewName(), n.getSubViewPath());
		else {
			if (!subViews.contains(n.getSubViewPath()))
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
		if (scopeLevel == 0)
			localSubViewNames.add(n.getId().getToken());
		scopeLevel++;
		n.getProgram().accept(this);
		scopeLevel--;
	}

	public Set<String> getLocalSubViewNames() {
		return this.localSubViewNames;
	}

	public List<String> getReferencedOutputs() {
		return this.referencedOutputs;
	}

	public List<String> getSubViews() {
		return this.subViews;
	}

	public List<String> getExternalViews() {
		return mapToList(externalViews, GET_VIEW_NAME);
	}
	public List<String> getExternalSubViewPaths() {
		return mapToList(externalViews, GET_SUBVIEW_PATH);
	}

	public List<String> getLocalSubViews() {
		return this.localSubViews;
	}

	public List<String> getLocalExternalViews() {
		return mapToList(localExternalViews, GET_VIEW_NAME);
	}
	public List<String> getLocalExternalSubViewPaths() {
		return mapToList(localExternalViews, GET_SUBVIEW_PATH);
	}

	private void addViewMap(Map<String, Set<String>> m, String viewName, String subViewPath) {
		if (m.containsKey(viewName)) {
			Set<String> s = m.get(viewName);
			s.add(subViewPath);
			m.put(viewName, s);
		}
		else {
			Set<String> s = new LinkedHashSet<String>();
			s.add(subViewPath);
			m.put(viewName, s);
		}
	}

	private List<String> mapToList(Map<String, Set<String>> m, Boolean getViewName) {
		List<String> list = new ArrayList<String>();
		for(Map.Entry<String, Set<String>> entry : m.entrySet()) {
			Set<String> subViewPaths = entry.getValue();
			for(String p : subViewPaths) {
				list.add(getViewName ? entry.getKey() : p);
			}
		}
		return list;
	}

	public void reset() {
		this.scopeLevel = 0;
		this.localSubViewNames.clear();
		this.referencedOutputs.clear();
		this.subViews.clear();
		this.externalViews.clear();
		this.localSubViews.clear();
		this.localExternalViews.clear();
	}
}
