class t {
   void m() {
	   try (new MyAutoCloseable() { }.finalWrapper.finalCloseable) {
		
		} catch (Exception ex) { }
	   
   }
}