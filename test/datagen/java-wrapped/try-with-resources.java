class t {
	void m() {
		try (fFileOutputStream fileStream = new FileOutputStream("javatpoint.txt");) {
			System.out.println("File written");
		} catch (Exception e) {
		
		}

	}
}