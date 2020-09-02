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
import boa.runtime.Tuple;
import weka.classifiers.lazy.LWL;

import java.io.IOException;

/**
 * A Boa aggregator for training the model using LWL.
 *
 * @author ankuraga
 */
@AggregatorSpec(name = "lwl", formalParameters = { "string" })
public class LWLAggregator extends MLAggregator {
	private LWL model;

	public LWLAggregator() {
	}

	public LWLAggregator(final String s) {
		super(s);
	}

	@Override
	public void aggregate(String[] data, String metadata) throws IOException, InterruptedException {
		if (!incrementalLearning) {
			aggregate(data, metadata, "LWL");
		} else {
			attributeCreation(data, "LWL");
			if (pick(trainingPerc)) {
				instanceCreation(data, trainingSet);
				try {
					incrementalLearning();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				instanceCreation(data, testingSet);
				if (testingSet != null) {
					if (!pick(evalTestPerc))
						testingSet.remove(testingSet.numInstances() - 1);
				}
			}
		}
	}

	@Override
	public void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException {
		if (!incrementalLearning) {
			aggregate(data, metadata, "LWL");
		} else {
			attributeCreation(data, "LWL");
			if (pick(trainingPerc)) {
				instanceCreation(data, trainingSet);
				try {
					incrementalLearning();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				instanceCreation(data, testingSet);
				if (testingSet != null) {
					if (!pick(evalTestPerc))
						testingSet.remove(testingSet.numInstances() - 1);
				}
			}
		}
	}

	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException {
	}
	
	public void incrementalLearning() throws Exception {
		if (model == null) {
			model = new LWL();
			model.setOptions(options);
			model.buildClassifier(this.trainingSet);
		}
		if (trainingSet != null) {
			model.updateClassifier(trainingSet.lastInstance());
			if (!pick(evalTrainPerc))
				trainingSet.remove(trainingSet.numInstances() - 1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish() throws IOException, InterruptedException {
		if (!incrementalLearning) {
			try {
				this.model = new LWL();
				this.model.setOptions(options);
				this.model.buildClassifier(this.trainingSet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.saveModel(this.model);
		String info = "\n=== Model Info ===\n" + this.model.toString();
		this.collect(info);
		System.out.println("train size: " + trainingSet.numInstances());
		this.evaluate(this.model, this.trainingSet);
		System.out.println("test size: " + testingSet.numInstances());
		this.evaluate(this.model, this.testingSet);
	}

}