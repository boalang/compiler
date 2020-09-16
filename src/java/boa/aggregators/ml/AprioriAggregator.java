/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer,
 *                 and Iowa State University of Science and Technology
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
package boa.aggregators.ml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.nd4j.shade.guava.collect.Sets;

import boa.runtime.Tuple;
import boa.aggregators.AggregatorSpec;
import weka.associations.Apriori;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * A Boa aggregator for training the model using Apriori.
 * 
 * @author ankuraga
 */
@AggregatorSpec(name = "apriori", formalParameters = { "string" })
public class AprioriAggregator extends MLAggregator {
	private Apriori model;
	private List<String> attributeNames;
	private List<String[]> dataList;

	public AprioriAggregator() {
	}

	public AprioriAggregator(final String s) {
		super(s);
		handlePreOptions();
	}

	private void handlePreOptions() {
		List<String> others = new ArrayList<>();
		for (int i = 0; i < options.length; i++) {
			String cur = options[i];
			if (cur.equals("-A")) {
				attributeNames = Lists.newArrayList();
				for (String name : options[++i].split(":"))
					if (!name.equals(""))
						attributeNames.add(name);
			} else {
				others.add(options[i]);		
			}
		}
		options = others.toArray(new String[0]);
	}

	@Override
	public void aggregate(String[] data, String metadata) throws IOException, InterruptedException {
		if (dataList == null)
			dataList = Lists.newArrayList();
		dataList.add(data);
	}

	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
	}

	public void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException {
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		attributeCreation();
		instanceCreation();
		try {
			this.model = new Apriori();
			handlePostOptions();
			this.model.setOptions(options);
			this.model.buildAssociations(this.instances);
			this.saveModel(this.model);
			this.collect(printModelOptions() + "\n" + this.model.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handlePostOptions() {
		for (int i = 0; i < options.length; i++) {
			String cur = options[i];
			if (cur.equals("-MI")) { // minimum itemset size
				options[i] = "-M";
				int size = Integer.parseInt(options[++i]);
				options[i] = String.valueOf((double) size / dataList.size());
			}
		}
	}

	private String printModelOptions() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nApriori Model Options\n==============\n");
		sb.append(Arrays.toString(this.model.getOptions()) + "\n");
		return sb.toString();
	}

	private void instanceCreation() {
		instances = new Instances("Apriori", this.fvAttributes, 1);
		for (String[] data : dataList) {
			Instance instance = new DenseInstance(fvAttributes.size());
			for (int i = 0; i < data.length; i++)
				instance.setValue(fvAttributes.get(i), data[i]);
			instances.add(instance);
		}
	}

	private void attributeCreation() {
		List<HashSet<String>> nominalSets = Lists.newArrayList();
		for (int i = 0; i < attributeNames.size(); i++)
			nominalSets.add(Sets.newHashSet());

		for (String[] data : dataList)
			for (int i = 0; i < data.length; i++)
				nominalSets.get(i).add(data[i]);

		List<List<String>> nomialLists = Lists.newArrayList();
		for (HashSet<String> set : nominalSets)
			nomialLists.add(new ArrayList<>(set));

		for (int i = 0; i < attributeNames.size(); i++)
			fvAttributes.add(new Attribute(attributeNames.get(i), nomialLists.get(i)));
	}

}