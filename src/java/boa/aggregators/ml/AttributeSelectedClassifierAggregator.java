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

import boa.runtime.Tuple;
import boa.aggregators.AggregatorSpec;
import weka.classifiers.meta.AttributeSelectedClassifier;

import java.io.IOException;

/**
 * A Boa aggregator for training the model using AttributeSelectedClassifier.
 *
 * @author ankuraga
 */
@AggregatorSpec(name = "attributeselectedclassifier", formalParameters = { "string" })
public class AttributeSelectedClassifierAggregator extends MLAggregator {
	private AttributeSelectedClassifier model;

	public AttributeSelectedClassifierAggregator() {
	}

	public AttributeSelectedClassifierAggregator(final String s) {
		super(s);
	}

	public void aggregate(String[] data, final String metadata) throws IOException, InterruptedException {
		aggregate(data, metadata, "AttributeSelectedClassifier");
	}

	public void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException {
		aggregate(data, metadata, "AttributeSelectedClassifier");
	}

	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish() throws IOException, InterruptedException {
		try {
			this.model = new AttributeSelectedClassifier();
			this.model.setOptions(options);
			this.model.buildClassifier(this.instances);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.saveModel(this.model);
		String info = "\n=== Model Info ===\n" + this.model.toString();
		this.collect(info);
	}
}
