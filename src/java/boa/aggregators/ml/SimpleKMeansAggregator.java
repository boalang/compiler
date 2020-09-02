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
package boa.aggregators.ml;

import boa.aggregators.AggregatorSpec;
import boa.aggregators.ml.util.KMeans;
import boa.runtime.Tuple;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.math.NumberUtils;
import org.nd4j.shade.guava.collect.Sets;

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
			m.buildClusterer(this.trainingSet);
			this.model = new KMeans(m, getAttributes());
			this.saveModel(this.model);
			this.collect(printModelOptions() + "\n" + this.model.toString());
			this.evaluate(m, trainingSet);
			if (trainingPerc != 100)
				this.evaluate(m, testingSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ArrayList<Attribute> getAttributes() {
		ArrayList<Attribute> attributes = Lists.newArrayList();
		for (int i = 0; i < trainingSet.numAttributes(); i++)
			attributes.add(trainingSet.attribute(i));
		return attributes;
	}

	private String printModelOptions() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nSimpleKMeans Model Options\n==============\n");
		sb.append(Arrays.toString(this.model.model.getOptions()) + "\n");
		return sb.toString();
	}

	private void instanceCreation() {
		trainingSet = new Instances("SimpleKMeans", this.fvAttributes, 1);
		testingSet = new Instances("SimpleKMeans", this.fvAttributes, 1);
		for (String[] data : dataList) {
			Instance instance = new DenseInstance(fvAttributes.size());
			for (int i = 0; i < data.length; i++)
				if (isNominalVal[i])
					instance.setValue(fvAttributes.get(i), data[i]);
				else
					instance.setValue(fvAttributes.get(i), Double.parseDouble(data[i]));
			if (pick(trainingPerc))
				trainingSet.add(instance);
			else
				testingSet.add(instance);
		}
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

		List<HashSet<String>> nominalSets = Lists.newArrayList();
		for (int i = 0; i < attributeNames.size(); i++)
			nominalSets.add(Sets.newHashSet());

		for (String[] data : dataList)
			for (int i = 0; i < data.length; i++)
				if (isNominalVal[i])
					nominalSets.get(i).add(data[i]);

		List<List<String>> nomialLists = Lists.newArrayList();
		for (HashSet<String> set : nominalSets)
			nomialLists.add(new ArrayList<>(set));

		for (int i = 0; i < attributeNames.size(); i++)
			if (isNominalVal[i])
				fvAttributes.add(new Attribute(attributeNames.get(i), nomialLists.get(i)));
			else
				fvAttributes.add(new Attribute(attributeNames.get(i)));
	}

}