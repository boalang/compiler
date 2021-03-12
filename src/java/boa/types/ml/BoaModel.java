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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.lang.math.NumberUtils;

import boa.runtime.Tuple;
import boa.types.BoaFunction;
import boa.types.BoaName;
import boa.types.BoaType;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

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
	protected Instances data;
	protected ArrayList<Attribute> fvAttributes;

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

	public String classify(long[] vector) {
		int NumOfAttributes = vector.length + 1;
		if (fvAttributes == null)
			fvAttributes = getAttributes(vector);
		if (data == null) {
			data = new Instances("Classifier", fvAttributes, 1);
			data.setClassIndex(NumOfAttributes - 1);
		}

		Instance instance = new DenseInstance(NumOfAttributes);
		for (int i = 0; i < NumOfAttributes - 1; i++)
			instance.setValue(fvAttributes.get(i), vector[i]);
		data.add(instance);

		double predval = -1;
		try {
			if (this.getKind() == BoaModel.Kind.CLASSIFIER) {
				predval = clr.classifyInstance(data.instance(0));
			} else if (this.getKind() == BoaModel.Kind.CLUSTERER) {
				predval = clu.clusterInstance(data.instance(0));
			}
			data.remove(0);
		} catch (Exception e) {
		}

		String predict = data.classAttribute().isNominal() ? data.classAttribute().value((int) predval)
				: String.valueOf(predval);
		return predict;
	}
	
	public static String predict(Classifier clr, Instance instance) {
		double predval = -1;
		try {
			predval = clr.classifyInstance(instance);
		} catch (Exception e) {
		}
		String predict = instance.classAttribute().isNominal() ? instance.classAttribute().value((int) predval)
				: String.valueOf(predval);
		return predict;
	}
	
	public static String expected(Instance instance) {
		return instance.classAttribute().value((int) instance.classValue());
	}

	private ArrayList<Attribute> getAttributes(long[] vector) {
		int NumOfAttributes = vector.length + 1;
		ArrayList<Attribute> fvAttributes = new ArrayList<Attribute>();
		for (int i = 0; i < NumOfAttributes - 1; i++)
			fvAttributes.add(new Attribute("Attribute" + i));

		try {
			Field[] fields = ((Tuple) o).getClass().getDeclaredFields();
			Field lastfield = fields[fields.length - 1];
			if (lastfield.getType().isEnum()) {
				ArrayList<String> fvNominalVal = new ArrayList<String>();
				for (Object obj : lastfield.getType().getEnumConstants())
					fvNominalVal.add(obj.toString());
				fvAttributes.add(new Attribute("Nominal" + (NumOfAttributes - 1), fvNominalVal));
			} else {
				fvAttributes.add(new Attribute("Attribute" + (NumOfAttributes - 1)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fvAttributes;
	}
	
	public String classify(Tuple vector) {
		if (fvAttributes == null)
			fvAttributes = getAttributes(vector);
		int NumOfAttributes = fvAttributes.size();
		if (data == null) {
			data = new Instances("Classifier", fvAttributes, 1);
			data.setClassIndex(NumOfAttributes - 1);
		}

		Instance instance = new DenseInstance(NumOfAttributes);

		for (int i = 0; i < NumOfAttributes - 1; i++)
			if (NumberUtils.isNumber(vector.getValues()[i]))
				instance.setValue((Attribute) fvAttributes.get(i), Double.parseDouble(vector.getValues()[i]));
			else
				instance.setValue((Attribute) fvAttributes.get(i), vector.getValues()[i]);
		data.add(instance);

		double predval = -1;
		try {
			if (this.getKind() == BoaModel.Kind.CLASSIFIER) {
				predval = clr.classifyInstance(data.instance(0));
			} else if (this.getKind() == BoaModel.Kind.CLUSTERER) {
				predval = clu.clusterInstance(data.instance(0));
			}
			data.remove(0);
		} catch (Exception e) {
		}

		String predict = data.classAttribute().isNominal() ? data.classAttribute().value((int) predval)
				: String.valueOf(predval);
		return predict;
	}
	
	private ArrayList<Attribute> getAttributes(Tuple vector) {
		ArrayList<Attribute> fvAttributes = new ArrayList<Attribute>();
		try {
			String[] fieldNames = vector.getFieldNames();
			int count = 0;
			for (int i = 0; i < fieldNames.length; i++) {
				if (vector.getValue(fieldNames[i]).getClass().isEnum()) {
					ArrayList<String> fvNominalVal = new ArrayList<String>();
					for (Object obj : vector.getValue(fieldNames[i]).getClass().getEnumConstants())
						fvNominalVal.add(obj.toString());
					fvAttributes.add(new Attribute("Nominal" + count, fvNominalVal));
					count++;
				} else if (vector.getValue(fieldNames[i]).getClass().isArray()) {
					int l = Array.getLength(vector.getValue(fieldNames[i])) - 1;
					for (int j = 0; j <= l; j++) {
						fvAttributes.add(new Attribute("Attribute" + count));
						count++;
					}
				} else {
					fvAttributes.add(new Attribute("Attribute" + count));
					count++;
				}
			}

			Field[] fields = ((Tuple) o).getClass().getDeclaredFields();
			Field lastfield = fields[fields.length - 1];
			if (lastfield.getType().isEnum()) {
				ArrayList<String> fvNominalVal = new ArrayList<String>();
				for (Object obj : lastfield.getType().getEnumConstants())
					fvNominalVal.add(obj.toString());
				fvAttributes.add(new Attribute("Nominal" + count, fvNominalVal));
				count++;
			} else {
				fvAttributes.add(new Attribute("Attribute" + count));
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fvAttributes;
	}
}