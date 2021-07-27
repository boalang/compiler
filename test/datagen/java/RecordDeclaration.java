public record RecordDeclaration(String brand, String licensePlate) {
	
	public String brandAsLowerCase() {
		return brand().toLowerCase();
	}
};