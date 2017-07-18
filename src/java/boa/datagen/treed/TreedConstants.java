package boa.datagen.treed;

public interface TreedConstants {
	static final String PROPERTY_MAP = "m";
	static final String PROPERTY_STATUS = "s";
	static final String PROPERTY_INDEX = "i";
	
	static final int GRAM_MAX_LENGTH = 2;
	static final int MIN_HEIGHT = 2;
	static final double MIN_SIM = 0.5;
	static final double MIN_SIM_MOVE = 0.75;
	static final double SIM_SMOOTH = (3*MIN_SIM - 1) /(1 - MIN_SIM);

	static final int STATUS_UNCHANGED = 0;
	static final int STATUS_RELABELED = 1;
	static final int STATUS_UNMAPPED = 2;
	static final int STATUS_MOVED = 3;
}
