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
package boa.aggregators.ml.weka;

import boa.runtime.Tuple;
import boa.aggregators.AggregatorSpec;
import boa.aggregators.FinishedException;
import boa.io.EmitKey;
import boa.io.EmitValue;
import weka.classifiers.bayes.BayesNet;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer;

/**
 * A Boa aggregator for training the model using BayesNet.
 *
 * @author ankuraga
 * @author hyj
 */
@AggregatorSpec(name = "bayesnet", formalParameters = { "string" }, canCombine = true)
public class BayesNetAggregator extends MLAggregator {
	private BayesNet model;

	public BayesNetAggregator() {
	}

	public BayesNetAggregator(final String s) {
		super(s);
	}

	public void aggregate(String[] data, final String metadata) throws IOException, InterruptedException {
		aggregate(data, metadata, "BayesNet");
	}

	public void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException {
		aggregate(data, metadata, "BayesNet");
	}

	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException, FinishedException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void finish() throws IOException, InterruptedException {
		try {
			this.model = new BayesNet();
			this.model.setOptions(options);
			this.model.buildClassifier(this.instances);
			this.saveModel(this.model);
			System.out.println("trained BayesNet model");
			
			if (trainWithCombiner) {
				@SuppressWarnings("unchecked")
				Reducer<EmitKey, EmitValue, EmitKey, EmitValue>.Context context = getContext();
				EmitKey key = getKey();
				// pass the path of trained model
				context.write(key, new EmitValue(modelPath.toString(), "model_path"));
			} else {
				String info = "\n=== Model Info ===\n" + this.model.toString();
				this.collect(info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.saveModel(this.model);
		String info = "\n=== Model Info ===\n" + this.model.toString();
		this.collect(info);
	}

}
