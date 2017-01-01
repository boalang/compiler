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
    private List<Object> values;
    public UserDefinedAggregator(UsrDfndReduceFunc function) {
        this.function = function;
        this.values = new ArrayList<Object>();
    }

    /** {@inheritDoc} */
    @Override
    public void start(final EmitKey key) {
        super.start(key);

        this.sum = 0;
    }

    /** {@inheritDoc} */
    @Override
    public void aggregate(final String data, final String metadata) throws IOException, InterruptedException, FinishedException {
        this.aggregate(Double.valueOf(data).longValue(), metadata);
    }

    /** {@inheritDoc} */
    @Override
    public void aggregate(final long data, final String metadata) {
        this.sum += data;
    }

    public void aggregate(final Object data, final String metadata) throws IOException, InterruptedException, FinishedException {
        this.values.add(data);
    }

    /** {@inheritDoc} */
    @Override
    public void finish() throws IOException, InterruptedException {
        try{
            this.collect(this.function.invoke(this.values));
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
