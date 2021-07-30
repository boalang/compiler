class t {
	void m() {
		String d = "MONDAY";
		int num = switch (d) {
		case "MONDA", "SUNDAY": yield 6;
		default: yield 2;
		};
	}
}