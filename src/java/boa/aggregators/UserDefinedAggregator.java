/*
 * Copyright 2015, Anthony Urso, Hridesh Rajan, Robert Dyer,
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
package boa.aggregators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import boa.BoaTup;
import boa.UsrDfndReduceFunc;
import boa.io.EmitKey;

/**
 * A Boa aggregator to calculate the sum of the values in a dataset.
 *
 * @author anthonyu
 * @author rdyer
 */
@AggregatorSpec(name = "userDefinedAggregator", type = "UserDefined", canCombine = true)
public class UserDefinedAggregator extends Aggregator {
    private long sum;
    private UsrDfndReduceFunc function;
    private List<BoaTup> tupleValues;
    private List<Long> longValues;
    private List<String> stringValues;
    private List<Double> doubleValues;
    public UserDefinedAggregator(UsrDfndReduceFunc function) {
        this.function = function;
        this.tupleValues = new ArrayList<BoaTup>();
        this.longValues = new ArrayList<Long>();
        this.doubleValues = new ArrayList<Double>();
        this.stringValues = new ArrayList<String>();
    }

    @Override
    public void aggregate(String data, String metadata) throws IOException, InterruptedException, FinishedException {
        this.stringValues.add(data);
    }

    @Override
    public void aggregate(long data, String metadata) throws IOException, InterruptedException, FinishedException {
        this.longValues.add(data);
    }

    @Override
    public void aggregate(double data, String metadata) throws IOException, InterruptedException, FinishedException {
        this.doubleValues.add(data);
    }

    /** {@inheritDoc} */
    @Override
    public void start(final EmitKey key) {
        super.start(key);

        this.sum = 0;
    }

    public void aggregate(final BoaTup data, final String metadata) throws IOException, InterruptedException, FinishedException {
        this.tupleValues.add(data);
    }

    /** {@inheritDoc} */
    @Override
    public void finish() throws IOException, InterruptedException {
        try{
            List<Object> values = new ArrayList<Object>();
            if(!longValues.isEmpty()) {
                values = getAsObjectArray(this.longValues);
            }else if(!stringValues.isEmpty()) {
                values = getAsObjectArray(this.stringValues);
            }else if(!doubleValues.isEmpty()) {
                values = getAsObjectArray(this.doubleValues);
            }else if(!tupleValues.isEmpty()) {
                values = getAsObjectArray(this.tupleValues);
            }
            this.collect(this.function.invoke(values));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static<T> List<Object> getAsObjectArray(List<T> input) {
        List<Object> result = new ArrayList<Object>();
        for(T o: input) {
            result.add(o);
        }
        return result;
    }
}
