package boa.test.datagen;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestReceiverParameter extends BaseTest {
	@Test
	public void receiverparameter() {
		assertEquals(parseWrapped("void m(Test this)"), 
						"{\n" +
					    "   \"namespaces\": [\n" +
					    "      {\n" +
					    "         \"name\": \"\",\n" +
					    "         \"declarations\": [\n" +
					    "            {\n" +
					    "               \"name\": \"t\",\n" +
					    "               \"kind\": \"CLASS\",\n" +
					    "               \"methods\": [\n" +
					    "                  {\n" +
					    "                     \"name\": \"m\",\n" +
					    "                     \"return_type\": {\n" +
					    "                        \"kind\": \"OTHER\",\n" +
					    "                        \"name\": 0\n" +
					    "                     },\n" +
					    "                     \"statements\": [\n" +
					    "                        {\n" +
					    "                           \"kind\": \"BLOCK\"\n" +
					    "                        }\n" +
					    "                     ]\n" +
					    "                  }\n" +
					    "               ]\n" +
					    "            }\n" +
					    "         ]\n" +
					    "      }\n" +
					    "   ]\n" +
					    "}"	
				);
	}

}
