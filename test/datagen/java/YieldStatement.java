class t {
	void m() {
		int num = switch (d) {
		case "MONDA" -> {
			yield 6;
		}
		default -> {
			yield 2;
		}
		};
	}
}