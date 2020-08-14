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
import boa.types.BoaFunction;
import boa.types.BoaName;
import boa.types.BoaType;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.core.Attribute;

/**
 * A {@link BoaType} representing model of any ML type.
 * 
 * @author ankuraga
 * @author hyj
 */
public class BoaModel extends BoaType {
	protected Classifier clr;
	protected Clusterer clu;
	protected BoaType t;
	protected Object o;

	/**
	 * Default BoaModel Constructor.
	 * 
	 */
	public BoaModel() {
	}

	/**
	 * Construct a BoaModel.
	 * 
	 * @param t A {@link BoaType} containing the types attached with this model
	 *
	 */
	public BoaModel(BoaType t) {
		this.t = t;
	}

	/**
	 * Construct a BoaModel.
	 * 
	 * @param clr A {@link Classifier} containing ML model
	 * 
	 * @param o   A {@link Object} containing type object
	 *
	 */
	public BoaModel(Classifier clr, Object o) {
		this.clr = clr;
		this.o = o;
	}

	/**
	 * Get the classifier of this model.
	 * 
	 * @return A {@link Classifier} representing ML model
	 * 
	 */
	public Classifier getClassifier() {
		return this.clr;
	}

	/**
	 * Construct a BoaModel.
	 * 
	 * @param clu A {@link Clusterer} containing ML model
	 * 
	 * @param o   A {@link Object} containing type object
	 *
	 */
	public BoaModel(Clusterer clu, Object o) {
		this.clu = clu;
		this.o = o;
	}

	/**
	 * Get the classifier of this model.
	 * 
	 * @return A {@link Clusterer} representing ML model
	 * 
	 */
	public Clusterer getClusterer() {
		return this.clu;
	}
	
	public ArrayList<Attribute> getAttributes() {
		return null;
	}

	/**
	 * Get the type attached with this model.
	 * 
	 * @return A {@link BoaType} representing type attached with ML model
	 * 
	 */
	public BoaType getType() {
		return this.t;
	}

	/**
	 * Get the type object of this model.
	 * 
	 * @return A {@link Object} representing type object
	 * 
	 */
	public Object getObject() {
		return this.o;
	}

	public Kind getKind() {
		return null;
	}

	public enum Kind {
		CLASSIFIER, CLUSTERER, VECTOR, ASSOCIATION
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		// if that is a function, check the return type
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// if that is a component, check the type
		if (that instanceof BoaName)
			return this.assigns(((BoaName) that).getType());

		// otherwise, if it's not an model, forget it
		if (!(that instanceof BoaModel))
			return false;

		// ok
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		// if that is a function, check the return value
		if (that instanceof BoaFunction)
			return this.assigns(((BoaFunction) that).getType());

		// otherwise, if it's not an model, forget it
		if (!(that instanceof BoaModel))
			return false;

		// ok
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.ml.BoaModel";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "model";
	}
}