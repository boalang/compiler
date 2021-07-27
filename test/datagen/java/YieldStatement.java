class t {
	void m(Day d) {
		int len = switch (d) {
		case SATURDAY, SUNDAY -> d.ordinal();
		default -> {
			yield d.toString().length();
		}
		};
	}
}