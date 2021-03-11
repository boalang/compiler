package boa.aggregators.ml.util;

public class BoaMLProperty {
	/*
	 * web: https://www.javamex.com/tutorials/memory/string_memory_usage.shtml
	 */
	public static int bytesOfString(int numOfChars) {
		return 8 * (int) (((numOfChars * 2) + 45) / 8);
	}

	public static float calculateRecordPercent(int combinerMem, int recordMem) {
		float maxRecords = (float) combinerMem * (1 << 20) / recordMem;
		MAX_RECORDS_FOR_SPILL = (int) maxRecords + 10;
		return maxRecords / (IO_SORT_MB * IO_SORT_SPILL_PERCENT * (1 << 16));
	}

	public static float IO_SORT_SPILL_PERCENT;
	public static int IO_SORT_MB;
	public static float IO_SORT_RECORD_PERCENT;
	public static int MAX_RECORDS_FOR_SPILL;

	static {
		IO_SORT_SPILL_PERCENT = 0.8f;
		IO_SORT_MB = 100;
		IO_SORT_RECORD_PERCENT = calculateRecordPercent(50, bytesOfString(400));
//		System.out.println(MAX_RECORDS_FOR_SPILL);
	};
}
