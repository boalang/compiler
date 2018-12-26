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

import boa.compiler.visitors.AbstractVisitor;
import boa.compiler.visitors.AbstractVisitorNoArgNoRet;
import boa.compiler.visitors.AbstractVisitorNoReturn;

/**
 * 
 * @author rdyer
 * @author hungc
 */
public class Table extends Operand {
	protected String jobName;
	protected String userName;
	protected String tagName;
	protected String outputName;
	protected String subViewId;

	public String getJobName () {
		return jobName;
	}

	public String getUserName () {
		return userName;
	}

	public String getTagName () {
		return tagName;
	}

	public String getOutputName () {
		return outputName;
	}

	public String getSubViewId () {
		return subViewId;
	}

	public Table (final String s) {
		if (s != null) {
			String[] ary = s.split("/");
			switch (s.charAt(0)) {
				case 'J':
				jobName = ary[0].substring(1);
				outputName = ary[1];
				userName = null;
				tagName = null;
				subViewId = null;
				break;

				case '@':
				userName = ary[0].substring(1);
				tagName = ary[1];
				outputName = ary[2];
				jobName = null;
				subViewId = null;
				break;

				default:
				subViewId = ary[0];
				outputName = ary[1];
				jobName = null;
				userName = null;
				tagName = null;
				break;
			}
		}
		else  {
			this.jobName = null;
			this.userName = null;
			this.tagName = null;
			this.outputName = null;
			this.subViewId = null;
		}
	}

	public Table (final String jobName, final String userName, final String tagName, final String outputName, final String subViewId) {
		this.jobName = jobName;
		this.userName = userName;
		this.tagName = tagName;
		this.outputName = outputName;
		this.subViewId = subViewId;
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
		final Table p = new Table(jobName, userName, tagName, outputName, subViewId);
		copyFieldsTo(p);
		return p;
	}
}
