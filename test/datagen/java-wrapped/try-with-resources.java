class t {
	void m() {
		FileOutputStream fileStream = new FileOutputStream("javatpoint.txt");
		try (fileStream) {
			System.out.println("File written");
		} catch (Exception e) {
		
		}
	}
}