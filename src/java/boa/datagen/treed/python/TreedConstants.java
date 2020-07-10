package boa.datagen.treed.python;

public interface TreedConstants {
	static final String PROPERTY_MAP = "m";
	static final String PROPERTY_STATUS = "s";
	static final String PROPERTY_INDEX = "i";
	
	static final int GRAM_MAX_LENGTH = 2;
	static final int MIN_HEIGHT = 0;
	static final int MAX_EXPENSION_SIZE = 10000;
	static final int MAX_BIPARTITE_MATCH_SIZE = 10000 * 10000;
	static final double MIN_SIM = 0.5;
	static final double MIN_SIM_MOVE = 0.75;
	static final double SIM_SMOOTH = (3*MIN_SIM - 1) /(1 - MIN_SIM);

}
