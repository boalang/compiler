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

import boa.BoaTup;
import boa.aggregators.AggregatorSpec;
import boa.aggregators.FinishedException;
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

    public void aggregate(final BoaTup data, final String metadata) throws IOException, InterruptedException, FinishedException, IllegalAccessException {
        aggregate(data, metadata, "LinearRegression");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish() throws IOException, InterruptedException {
        try {
            System.out.println("Linearregression working now with: " + this.trainingSet.numInstances());
            System.out.println(this.trainingSet);
            this.model.buildClassifier(this.trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("modeling done");
        this.saveModel(this.model);
//		this.saveTrainingSet(this.trainingSet);
        this.collect(this.model.toString());
    }
}
