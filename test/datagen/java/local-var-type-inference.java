class t {
	 @Test
	   public void whenVarInitWithString_thenGetStringTypeVar() {
	       var message = "Hello, Java 10";
	       assertTrue(message instanceof String);
	   }
}