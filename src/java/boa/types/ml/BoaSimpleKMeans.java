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

import java.util.ArrayList;

import boa.aggregators.ml.util.KMeans;
import boa.types.BoaType;
import weka.clusterers.Clusterer;
import weka.core.Attribute;

/**
 * A {@link BoaType} representing ML model of SimpleKMeans with attached types.
 * 
 * @author ankuraga
 * @author hyj
 */
public class BoaSimpleKMeans extends BoaModel {

	private KMeans model;

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

	public BoaSimpleKMeans(KMeans m, Object o) {
		this.model = m;
		this.o = o;
	}

	@Override
	public Clusterer getClusterer() {
		return this.model.model;
	}

	@Override
	public ArrayList<Attribute> getAttributes() {
		return this.model.attributes;
	}

	@Override
	public Kind getKind() {
		return Kind.CLUSTERER;
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
		return "boa.types.ml.BoaSimpleKMeans";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "boa.types.ml.BoaSimpleKMeans" + "/" + this.t;
	}
}