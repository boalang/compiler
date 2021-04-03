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

import boa.types.BoaType;
import weka.clusterers.Clusterer;

/**
 * A {@link BoaType} representing ML model of SimpleKMeans with attached types.
 * 
 * @author ankuraga
 */
public class BoaSimpleKMeans extends BoaModel {

	/**
	 * Default BoaSimpleKMeans Constructor.
	 * 
	 */
	public BoaSimpleKMeans() {
	}

	/**
	 * Construct a BoaSimpleKMeans.
	 * 
	 * @param t A {@link BoaType} containing the types attached with this model
	 *
	 */
	public BoaSimpleKMeans(BoaType t) {
		this.t = t;
	}

	/**
	 * Construct a BoaSimpleKMeans.
	 * 
	 * @param clu A {@link Clusterer} containing ML model
	 * 
	 * @param o   A {@link Object} containing type object
	 *
	 */
	public BoaSimpleKMeans(Clusterer clu, Object o) {
		this.clu = clu;
		this.o = o;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		if (!super.assigns(that))
			return false;

		// ok
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		if (!super.assigns(that))
			return false;

		// ok
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.ml.BoaSimpleKMeans";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "boa.types.ml.BoaSimpleKMeans" + "/" + this.t;
	}
}