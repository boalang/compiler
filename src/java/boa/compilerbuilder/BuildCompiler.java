package boa.compilerbuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.squareup.protoparser.ProtoFile;

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

	/**
	 * 
	 * @param name
	 *            name of the toplevel domain type
	 * @param typename
	 *            complete qualified name of the toplevel domain type This
	 *            function write this key value pair in persistent file
	 *            "settings.json". This key value pair will be required while
	 *            running the Boa program.
	 */
	private void updateTypeNames(String name, String typename) {
		JSONObject allSettings = new JSONObject(FileIO
				.readFileContents(DSIProperties.SETTINGS_JSON_FILE_PATH + "/" + DSIProperties.SETTINGS_JSON_FILE_NAME));
		JSONArray domains = null;
		if (allSettings.has(DSIProperties.BOA_DOMAIN_TYP_FIELD)) {
			domains = allSettings.getJSONArray(DSIProperties.BOA_DOMAIN_TYP_FIELD);
		} else {
			domains = new JSONArray();
		}

		for (int i = 0; i < domains.length(); i++) {
			JSONObject domain = (JSONObject) domains.get(i);
			if (domain.has(name)) {
				domains.remove(i);
			}
		}

		domains.put(new JSONObject().put(name, typename));
		allSettings.remove(DSIProperties.BOA_DOMAIN_TYP_FIELD);
		allSettings.put(DSIProperties.BOA_DOMAIN_TYP_FIELD, domains);
		FileIO.writeFileContents(DSIProperties.SETTINGS_JSON_FILE_PATH + "/" + DSIProperties.SETTINGS_JSON_FILE_NAME,
				allSettings.toString());
	}

	public void compileAndBuild() throws IOException {
		// Generating the relevent code
		ProtoFile schema = this.getSchemaReader().getSchema();
		DomainTypeGenerator gen = new DomainTypeGenerator(this.getSchemaReader().getSchema());
		this.writeGeneratedCode(gen.generateCode());

		// save the toplevel domain type name and its corresponding full type
		String toplevel = schema.typeElements().get(0).name();
		updateTypeNames(toplevel, schema.packageName() + "." + gen.getSchemaFileName() + "." + toplevel);

		// // Building the code using ant
		// File buildFile = new File("build.xml");
		// Project p = new Project();
		// p.setUserProperty("ant.file", buildFile.getAbsolutePath());
		// p.init();
		// ProjectHelper helper = ProjectHelper.getProjectHelper();
		// p.addReference("ant.projectHelper", helper);
		// helper.parse(p, buildFile);
		// p.executeTarget(p.getDefaultTarget());
	}

	public static void main(String[] args) {
		BuildCompiler builder = new BuildCompiler("/Users/nmtiwari/Desktop/test/msr");
		try {
			builder.compileAndBuild();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
