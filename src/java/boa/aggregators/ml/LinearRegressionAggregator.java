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
import boa.aggregators.FinishedException;
import boa.runtime.Tuple;
import weka.classifiers.functions.LinearRegression;

import java.io.IOException;

/**
 * A Boa aggregator for training the model using LinearRegression.
 *
 * @author ankuraga
 */
@AggregatorSpec(name = "linearregression", formalParameters = {"string"})
public class LinearRegressionAggregator extends MLAggregator {
	private LinearRegression model;

	public LinearRegressionAggregator() {
		this.model = new LinearRegression();
	}

	public LinearRegressionAggregator(final String s) {
		super(s);
	}

	@Override
	public void aggregate(String data, String metadata) throws NumberFormatException, IOException, InterruptedException {
		aggregate(data, metadata, "LinearRegression");
	}

	public void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException, FinishedException, IllegalAccessException {
		aggregate(data, metadata, "LinearRegression");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish() throws IOException, InterruptedException {
		try {
			this.model.buildClassifier(this.trainingSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("modeling done");
		this.saveModel(this.model);
		System.out.println("model saved");
		this.evaluate(this.model, this.trainingSet);
		this.evaluate(this.model, this.testingSet);
		this.collect(this.model.toString());
	}
}
