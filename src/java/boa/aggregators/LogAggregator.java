/*
 * Copyright 2014, Anthony Urso, Hridesh Rajan, Robert Dyer, 
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

import org.apache.log4j.Logger;

/**
 * A Boa aggregator to log values in a dataset by weight.
 * 
 * @author anthonyu
 */
//@AggregatorSpec(name = "log", weightType = "string")
public class LogAggregator extends Aggregator {
	private static Logger logger = Logger.getLogger(LogAggregator.class);

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException {
		if (metadata.equals("trace"))
			LogAggregator.logger.debug(data);
		else if (metadata.equals("debug"))
			LogAggregator.logger.debug(data);
		else if (metadata.equals("info"))
			LogAggregator.logger.info(data);
		else if (metadata.equals("warn"))
			LogAggregator.logger.warn(data);
		else if (metadata.equals("error"))
			LogAggregator.logger.error(data);
		else if (metadata.equals("fatal"))
			LogAggregator.logger.fatal(data);
	}
}
