package boa.aggregators.ml.util;

import java.io.Serializable;
import java.util.ArrayList;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;

public class KMeans implements Serializable {
	private static final long serialVersionUID = 1L;
	public SimpleKMeans model;
	public ArrayList<Attribute> attributes;

	public KMeans(SimpleKMeans model, ArrayList<Attribute> attributes) {
		this.model = model;
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return model.toString();
	}
}