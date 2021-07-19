public record Vehicle(String brand, String licensePlate) {
	
	public String brandAsLowerCase() {
		return brand().toLowerCase();
	}
};