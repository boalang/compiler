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
import weka.classifiers.Classifier;

/**
 * A {@link BoaType} representing ML model of LogitBoost with attached types.
 * 
 * @author ankuraga
 * @author hyj
 */
public class BoaLogitBoost extends BoaModel {
	/**
	 * Default BoaLogitBoost Constructor.
	 * 
	 */
	public BoaLogitBoost() {
	}

	/**
	 * Construct a BoaLogitBoost.
	 * 
	 * @param t A {@link BoaType} containing the types attached with this model
	 *
	 */
	public BoaLogitBoost(BoaType t) {
		this.t = t;
	}

	/**
	 * Construct a BoaLogitBoost.
	 * 
	 * @param clr A {@link Classifier} containing ML model
	 * 
	 * @param o   A {@link Object} containing type object
	 *
	 */
	public BoaLogitBoost(Classifier clr, Object o) {
		this.clr = clr;
		this.o = o;
	}

	@Override
	public Kind getKind() {
		return Kind.CLASSIFIER;
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
		return "boa.types.ml.BoaLogitBoost";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "boa.types.ml.BoaLogitBoost" + "/" + this.t;
	}
}