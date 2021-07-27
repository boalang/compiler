class t {
	void m() {
		String month;
		int season = switch (month) {
		case JAN, FEB, MARCH -> 1;
		case APRIL, MAY, JUNE -> 2;
		};
	}
}
