package boa.datagen.treed;

import boa.types.Shared.ChangeKind;

public interface TreedConstants {
	static final String PROPERTY_MAP = "m";
	static final String PROPERTY_STATUS = "s";
	static final String PROPERTY_INDEX = "i";
	
	static final int GRAM_MAX_LENGTH = 2;
	static final int MIN_HEIGHT = 2;
	static final double MIN_SIM = 0.5;
	static final double MIN_SIM_MOVE = 0.75;
	static final double SIM_SMOOTH = (3*MIN_SIM - 1) /(1 - MIN_SIM);

	static final ChangeKind STATUS_UNCHANGED = ChangeKind.UNCHANGED;
	static final ChangeKind STATUS_MODIFIED = ChangeKind.MODIFIED;
	static final ChangeKind STATUS_RELABELED = ChangeKind.RENAMED;
	static final ChangeKind STATUS_UNMAPPED = ChangeKind.UNMAPPED;
	static final ChangeKind STATUS_MOVED = ChangeKind.MOVED;
}
