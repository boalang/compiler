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
import weka.classifiers.meta.AdaBoostM1;
import java.io.IOException;

/**
 * A Boa aggregator for training the model using AdaBoostM1.
 *
 * @author ankuraga
 * @author nmtiwari
 */
@AggregatorSpec(name = "adaboost", formalParameters = {"string"})
public class AdaBoostM1Aggregator extends MLAggregator {
    private AdaBoostM1 model;

    public AdaBoostM1Aggregator() {}

    public AdaBoostM1Aggregator(final String s) {
        super(s);
    }

    public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
    	aggregate(data, metadata, "AdaBoostM1");
    }

    public void aggregate(final Tuple data, final String metadata) throws IOException, InterruptedException {
    	aggregate(data, metadata, "AdaBoostM1");
    }

    @Override
    public void finish() throws IOException, InterruptedException {
        try {
        	this.model = new AdaBoostM1();
            this.model.setOptions(options);
            this.model.buildClassifier(this.trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.saveModel(this.model);
//        this.saveTrainingSet(this.trainingSet);
        this.evaluate(this.model, this.trainingSet);
		this.evaluate(this.model, this.testingSet);
        this.collect(this.model.toString());
    }
}
