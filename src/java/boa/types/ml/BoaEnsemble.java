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
package boa.types.ml;

import static boa.functions.BoaMLIntrinsics.deserialize;

import org.apache.hadoop.fs.FileStatus;

import boa.types.BoaType;

/**
 * A {@link BoaType} representing ensemble of any ML type.
 * 
 * @author hyj
 */
public class BoaEnsemble extends BoaModel {
	protected FileStatus[] files;
	private int index;

	/**
	 * Default BoaEnsemble Constructor.
	 * 
	 */
	public BoaEnsemble() {
	}

	/**
	 * Construct a BoaModel.
	 * 
	 * @param t A {@link BoaType} containing the types attached with this model
	 *
	 */
	public BoaEnsemble(BoaType t) {
		this.t = t;
	}
	
	public FileStatus[] getFiles() {
		return files;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		if (!super.assigns(that))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		if (!super.assigns(that))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.ml.BoaEnsemble";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ensemble";
	}
	
	public boolean hasNext() {
		return index < files.length;
	}
	
	public Object next() {
		System.out.println(files[index].getPath().getName());
		return deserialize(files[index++].getPath());
	}
	
	public void resetIndex() {
		index = 0;
	}

}