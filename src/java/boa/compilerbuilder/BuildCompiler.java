package boa.compilerbuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.squareup.protoparser.ProtoFile;

import boa.datagen.util.FileIO;

/*
 * BoaJavaHadoop.stg
 */
public class BuildCompiler {
	private SchemaBuilder schemaBuilder;

	private BuildCompiler(String path) {
		this.schemaBuilder = new SchemaBuilder(path);
	}

	public SchemaBuilder getSchemaReader() {
		return schemaBuilder;
	}

	private void writeGeneratedCode(ArrayList<GeneratedDomainType> code) {
		StringBuilder qualifiedName = new StringBuilder();
		File clasFile = null;
		for (GeneratedDomainType clas : code) {
			String pck = clas.getPckg().replace('.', '/');
			qualifiedName.delete(0, qualifiedName.length());
			qualifiedName.append(pck);
			clasFile = new File("./src/java/" + qualifiedName.toString());
			System.out.println("Dir path: " + clasFile.getAbsolutePath());
			clasFile.mkdirs();
			clasFile = new File(clasFile, clas.getName());
			System.out.println("File path: " + clasFile.getAbsolutePath());
			FileIO.writeFileContents(clasFile, clas.code);
		}
	}

	public static void main(String[] args) throws IOException {
		BuildCompiler builder = new BuildCompiler("/Users/nmtiwari/Desktop/test");
		SchemaBuilder schema = builder.getSchemaReader();
		ProtoFile schemaFile = schema.getSchema();

		DomainTypeGenerator gen = new DomainTypeGenerator(schemaFile, "Accident");
		ArrayList<GeneratedDomainType> generatedtyps = gen.generateCode();
		builder.writeGeneratedCode(generatedtyps);
	}
}
