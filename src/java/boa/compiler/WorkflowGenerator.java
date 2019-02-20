/*
 * Copyright 2019, Robert Dyer, Che Shian Hung
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
package boa.compiler;

import java.util.List;
import java.util.ArrayList;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STRawGroupDir;

/**
 * 
 * @author hungc
 * @author rdyer
 * 
 */

public class WorkflowGenerator {
	public static STGroup workflowStg;
	protected final List<String> workflows;

	private List<String> jobNames;
	private List<String> mains;
	private List<List<String>> subViews;
	private List<List<String>> args;
	private List<List<String>> subWorkflowPaths;

	public WorkflowGenerator () {
		workflowStg = new STGroupDir("templates");
		workflowStg.importTemplates(new STGroupFile("Views.stg"));

		workflows = new ArrayList<String>();
		jobNames = new ArrayList<String>();
		mains = new ArrayList<String>();
		args = new ArrayList<List<String>>();
		subViews = new ArrayList<List<String>>();
		subWorkflowPaths = new ArrayList<List<String>>();
	}

	public void setJobNames(List<String> jobNames) {
		this.jobNames = jobNames;
	}

	public void setSubViews(List<List<String>> subViews) {
		this.subViews = subViews;
	}

	public void setMains(List<String> mains) {
		this.mains = mains;
	}

	public void setArgs(List<List<String>> args) {
		this.args = args;
	}

	public void setSubWorkflowPaths(List<List<String>> subWorkflowPaths) {
		this.subWorkflowPaths = subWorkflowPaths;
	}
	// or add

	public List<String> getWorkflows() {
		return this.workflows;
	}

	public void createWorkflows() {
		workflows.clear();

		int i = 0;

		while (i < jobNames.size()) {
			final ST st = workflowStg.getInstanceOf("Workflow");

			List<String> subvs = subViews.get(i);
			List<String> paths = subWorkflowPaths.get(i);
			List<String> views = new ArrayList<String>();

			for (int j = 0; j < subvs.size(); j++)
				views.add(createSubWorkflow(subvs.get(j), paths.get(j)));

			st.add("jobName", jobNames.get(i));
			st.add("viewnames", subvs);
			st.add("views", views);
			st.add("main", mains.get(i));
			st.add("args", args.get(i));

			workflows.add(st.render());

			i++;
		}
	}

	public String createSubWorkflow(String jobName, String path) {
		final ST st = workflowStg.getInstanceOf("ViewWorkflow");
		st.add("jobName", jobName);
		st.add("path", path);

		return st.render();
	}
}