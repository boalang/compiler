package boa.compilerbuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import boa.datagen.util.FileIO;
import boa.dsi.DSIProperties;

public class BuildCompiler {
	private SchemaBuilder schemaBuilder;

	public BuildCompiler(String path) {
		this.schemaBuilder = new SchemaBuilder(path);
	}

	private SchemaBuilder getSchemaReader() {
		return schemaBuilder;
	}

	private void writeGeneratedCode(ArrayList<GeneratedDomainType> code) {
		StringBuilder qualifiedName = new StringBuilder();
		File clasFile = null;
		for (GeneratedDomainType clas : code) {
			String pck = clas.getPckg().replace('.', '/');
			qualifiedName.delete(0, qualifiedName.length());
			qualifiedName.append(pck);
			clasFile = new File(DSIProperties.BOA_DOMAIN_TYPE_GEN_LOC + qualifiedName.toString());
			clasFile.mkdirs();
			clasFile = new File(clasFile, clas.getName());
			FileIO.writeFileContents(clasFile, clas.code);
		}
	}

	public void build() throws IOException {
		DomainTypeGenerator gen = new DomainTypeGenerator(this.getSchemaReader().getSchema());
		this.writeGeneratedCode(gen.generateCode());
	}

	public static void main(String[] args) {
		BuildCompiler builder = new BuildCompiler("/Users/nmtiwari/Desktop/test");
		try {
			builder.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
