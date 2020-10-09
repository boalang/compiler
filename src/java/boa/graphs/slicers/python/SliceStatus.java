package boa.graphs.slicers.python;

public enum SliceStatus {
	SLICE_DONE, //program point p is impacted and fits filter criteria
	NOT_CANDIDATE, //Doesn't fit filter criteria
	CANDIDATE_NOT_SLICED //fits but not sliced
}
