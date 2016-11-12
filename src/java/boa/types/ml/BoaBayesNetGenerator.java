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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaType;
import boa.types.BoaName;

import weka.classifiers.Classifier;
/**
 * A {@link BoaType} representing ML model of BayesNetGenerator with attached types.
 * 
 * @author ankuraga
 */
public class BoaBayesNetGenerator extends BoaModel{
	private Classifier clr;
	private BoaType t;
	private Object o;
	
	/**
	 * Default BoaBayesNetGenerator Constructor.
	 * 
	 */
	public BoaBayesNetGenerator(){
	}
	
	/**
	 * Construct a BoaBayesNetGenerator.
	 * 
	 * @param t
	 *            A {@link BoaType} containing the types attached with this model
	 *
	 */
	public BoaBayesNetGenerator(BoaType t){
		this.t = t;
	}
	
	/**
	 * Construct a BoaBayesNetGenerator.
	 * 
	 * @param clr
	 *            A {@link Classifier} containing ML model
	 * 
	 * @param o
	 *            A {@link Object} containing type object
	 *
	 */
	public BoaBayesNetGenerator(Classifier clr, Object o){
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
	
	/** {@inheritDoc} */
	@Override
	public boolean assigns(final BoaType that) {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final BoaType that) {
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.ml.BoaBayesNetGenerator";
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "boa.types.ml.BoaBayesNetGenerator" + "/" + this.t;
	}
}