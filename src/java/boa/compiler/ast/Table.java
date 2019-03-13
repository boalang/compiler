/*
 * Copyright 2018, Robert Dyer, Che Shian Hung, and Bowling Green State University
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
package boa.compiler.ast;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hungc
 */
public class Table extends Operand {
	protected Integer jobNum;
	protected String userName;
	protected String viewName;
	protected String outputName;
	protected List<String> subViews;
	protected List<String> paths;

	public Integer getJobNum () {
		return jobNum;
	}

	public String getUserName () {
		return userName;
	}

	public String getViewName () {
		return viewName;
	}

	public String getOutputName () {
		return outputName;
	}

	public List<String> getSubViews () {
		return subViews;
	}

	public String getSubViewPath() {
		if (subViews.size() == 0)
			return "";

		String p = subViews.get(0);
		for(int i = 1; i < subViews.size(); i++)
			p += "/" + subViews.get(i);

		return p;
	}

	public List<String> getPaths () {
		return paths;
	}

	public void addSubView(String sv) {
		if (subViews == null)
			subViews = new ArrayList<String>();
		subViews.add(sv);
	}

	public Table (final String s) {
		if (s != null) {
			String[] ary = s.split("/");
			paths = Arrays.asList(s.split("/"));
			outputName = ary[ary.length - 1];
			subViews = new ArrayList<String>();
			switch (s.charAt(0)) {
				case 'J':
				jobNum = Integer.parseInt(ary[0].substring(1));
				userName = null;
				viewName = null;
				for (int i = 1; i < ary.length - 1; i++) {
					subViews.add(ary[i]);
				}
				break;

				case '@':
				userName = ary[0].substring(1);
				viewName = ary[1];
				jobNum = null;
				for (int i = 2; i < ary.length - 1; i++) {
					subViews.add(ary[i]);
				}
				break;

				default:
				viewName = null;
				jobNum = null;
				userName = null;
				for (int i = 0; i < ary.length - 1; i++) {
					subViews.add(ary[i]);
				}
				break;
			}
		}
		else  {
			this.jobNum = null;
			this.userName = null;
			this.viewName = null;
			this.outputName = null;
			this.subViews = null;
		}
	}

	public Table (final Integer jobNum, final String userName, final String viewName, final String outputName, final List<String> subViews) {
		this.jobNum = jobNum;
		this.userName = userName;
		this.viewName = viewName;
		this.outputName = outputName;
		this.subViews = subViews;
	}

	/** {@inheritDoc} */
	@Override
	public <T,A> T accept(final AbstractVisitor<T,A> v, A arg) {
		return v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public <A> void accept(final AbstractVisitorNoReturn<A> v, A arg) {
		v.visit(this, arg);
	}

	/** {@inheritDoc} */
	@Override
	public void accept(final AbstractVisitorNoArgNoRet v) {
		v.visit(this);
	}

	public Table clone() {
		final Table p = new Table(new Integer(jobNum), userName, viewName, outputName, null);
		for (String sv : subViews) {
			p.addSubView(sv);
		}
		copyFieldsTo(p);
		return p;
	}
}
