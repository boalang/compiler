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
import weka.classifiers.functions.GaussianProcesses;

import java.io.IOException;

/**
 * A Boa aggregator for training the model using GaussianProcesses.
 *
 * @author ankuraga
 */
@AggregatorSpec(name = "gaussianprocesses", formalParameters = {"string"})
public class GaussianProcessesAggregator extends MLAggregator {
    private GaussianProcesses model;


    public GaussianProcessesAggregator() {
    }

    public GaussianProcessesAggregator(final String s) {
        super(s);
    }

    public void aggregate(String[] data, final String metadata) throws IOException, InterruptedException {
        aggregate(data, metadata, "GaussianProcesses");
    }

    public void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException {
        aggregate(data, metadata, "GaussianProcesses");
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
            this.model = new GaussianProcesses();
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