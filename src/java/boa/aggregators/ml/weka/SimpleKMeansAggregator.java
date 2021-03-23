/*
 * Copyright 2020, Hridesh Rajan, Robert Dyer,
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
package boa.aggregators.ml.weka;

import boa.aggregators.AggregatorSpec;
import boa.aggregators.ml.util.KMeans;
import boa.runtime.Tuple;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

/**
 * A Boa aggregator for training the model using SimpleKMeans.
 *
 * @author ankuraga
 * @author hyj
 */
@AggregatorSpec(name = "simplekmeans", formalParameters = { "string" })
public class SimpleKMeansAggregator extends MLAggregator {

	private KMeans model;
	private List<String> attributeNames;
	private boolean[] isNominalVal;
	private List<String[]> dataList;

	public SimpleKMeansAggregator() {
		this.isClusterer = true;
	}

	public SimpleKMeansAggregator(final String s) {
		super(s);
		this.isClusterer = true;
		handlePreOptions();
	}

	private void handlePreOptions() {
		List<String> others = new ArrayList<>();
		for (int i = 0; i < options.length; i++) {
			String cur = options[i];
			if (cur.equals("-A")) {
				attributeNames = new ArrayList<>();
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
			dataList = new ArrayList<>();
		dataList.add(data);
	}

	@Override
	public void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException {
	}

	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish() throws IOException, InterruptedException {
		attributeCreation();
		instanceCreation();
		try {
			SimpleKMeans m = new SimpleKMeans();
			m.setOptions(options);
			m.buildClusterer(this.instances);
			this.model = new KMeans(m, getAttributes());
			this.saveModel(this.model);
			this.collect(printModelOptions() + "\n" + this.model.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<Attribute> getAttributes() {
		ArrayList<Attribute> attributes = new ArrayList<>();
		for (int i = 0; i < instances.numAttributes(); i++)
			attributes.add(instances.attribute(i));
		return attributes;
	}

	private String printModelOptions() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nSimpleKMeans Model Options\n==============\n");
		sb.append(Arrays.toString(this.model.model.getOptions()) + "\n");
		return sb.toString();
	}

	private void instanceCreation() {
		instances = new Instances("SimpleKMeans", this.fvAttributes, 1);
	}

	private void attributeCreation() {
		isNominalVal = new boolean[attributeNames.size()];
		boolean hasNominalVal = false;
		String[] firstData = dataList.get(0);
		for (int i = 0; i < attributeNames.size(); i++) {
			if (!NumberUtils.isNumber(firstData[i])) {
				isNominalVal[i] = true;
				hasNominalVal = true;
			}
		}

		if (!hasNominalVal)
			return;

		List<HashSet<String>> nominalSets = new ArrayList<>();
		for (int i = 0; i < attributeNames.size(); i++)
			nominalSets.add(new HashSet<>());

		for (String[] data : dataList)
			for (int i = 0; i < data.length; i++)
				if (isNominalVal[i])
					nominalSets.get(i).add(data[i]);

		List<List<String>> nomialLists =  new ArrayList<>();
		for (HashSet<String> set : nominalSets)
			nomialLists.add(new ArrayList<>(set));

		for (int i = 0; i < attributeNames.size(); i++)
			if (isNominalVal[i])
				fvAttributes.add(new Attribute(attributeNames.get(i), nomialLists.get(i)));
			else
				fvAttributes.add(new Attribute(attributeNames.get(i)));
	}

}