/*
 * Copyright 2014-2021, Anthony Urso, Hridesh Rajan, Robert Dyer,
 *                 Iowa State University of Science and Technology
 *                 and University of Nebraska Board of Regents
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
package boa.runtime;

import java.io.IOException;
import java.util.Collection;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import boa.io.EmitKey;
import boa.io.EmitValue;

/**
 * A {@link Mapper} that performs the brunt of all Boa work.
 *
 * @author anthonyu
 * @author rdyer
 */
public abstract class BoaMapper extends Mapper<Text, BytesWritable, EmitKey, EmitValue> implements Configurable {
	protected static final Logger LOG = Logger.getLogger(BoaMapper.class);

	private Configuration conf;
	protected Context context;
	protected Collection<String> excludeProjects;

	/** {@inheritDoc} */
	@Override
	public Configuration getConf() {
		return this.conf;
	}

	/** {@inheritDoc} */
	@Override
	public void setConf(final Configuration conf) {
		this.conf = conf;
	}

	/** {@inheritDoc} */
	@Override
	protected void setup(final Mapper<Text, BytesWritable, EmitKey, EmitValue>.Context context) throws IOException, InterruptedException {
		super.setup(context);

		this.context = context;
		this.excludeProjects = this.conf.getStringCollection("boa.exclude.projects");
	}

	protected boolean excludeProject(final String id) {
		return this.excludeProjects.contains(id);
	}
}
