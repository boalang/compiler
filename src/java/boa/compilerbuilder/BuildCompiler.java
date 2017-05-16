package boa.compilerbuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.squareup.protoparser.ProtoFile;

import boa.datagen.util.FileIO;
import boa.dsi.DSIProperties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

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
			FileIO.writeFileContents(clasFile, clas.getCode());
		}
	}

	private String generateSymbolTableCode(ArrayList<GeneratedDomainType> code, String ds, String toplevel) {
		StringBuilder qualifiedName = new StringBuilder();
		for (GeneratedDomainType gen : code) {
			String name = gen.getName().substring(0, gen.getName().lastIndexOf('.'));
			qualifiedName.append("		");
			qualifiedName.append(ds);
			qualifiedName.append(".put(\"");
			qualifiedName.append(gen.getType());
			qualifiedName.append("\", new ");
			qualifiedName.append(name);
			qualifiedName.append("());");
			qualifiedName.append("\n");
			if (gen.getType().equals(this.schemaBuilder.getToplevel())) {
				qualifiedName.append("\n");
				qualifiedName.append("		");
				qualifiedName.append("globals.put(\"input\", new ").append(name).append("());");
				qualifiedName.append("\n");
			}
		}
		return qualifiedName.toString();
	}

	private void updateSymbolTable(String code) {
		final long insertAt = 143;
		String toBeReplaced = "		globals.put(\"input\", new ProjectProtoTuple());";
		try {
			FileReader fileR = new FileReader(DSIProperties.BOA_SYMBOLTABLE);
			BufferedReader fileReader = new BufferedReader(fileR);
			// read starting insertAt lines
			StringBuilder newcontent = new StringBuilder();
			for (int i = 1; i < insertAt; i++) {
				// append all lines to new content
				String line = fileReader.readLine();
				if (!line.equals(toBeReplaced)) {
					newcontent.append(line);
				}
				newcontent.append("\n");
			}

			newcontent.append("		\n\n");
			newcontent.append("/** Automatically generated code start **/");
			newcontent.append("\n");
			newcontent.append(code);
			newcontent.append("		");
			newcontent.append("/** Automatically generated code ends **/");
			newcontent.append("\n\n\n");

			String line;
			while ((line = fileReader.readLine()) != null) {
				newcontent.append(line);
				newcontent.append("\n");
			}

			fileReader.close();
			fileR.close();

			FileWriter fileW = new FileWriter(DSIProperties.BOA_SYMBOLTABLE);
			BufferedWriter fileWriter = new BufferedWriter(fileW);
			fileWriter.write(newcontent.toString());

			fileWriter.flush();
			fileWriter.close();
			fileW.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
	private void updateTypeInfoInSettings(String name, String typename) {
		JSONObject allSettings = new JSONObject(new FileIO().readFileContents(DSIProperties.SETTINGS_JSON_FILE));
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
		DomainTypeGenerator gen = new DomainTypeGenerator(this.getSchemaReader().getSchema(),
				this.getSchemaReader().getToplevel(), this.getSchemaReader().getToplevelPackage());

		ArrayList<GeneratedDomainType> gentypes = gen.generateCode();
		this.writeGeneratedCode(gentypes);

		updateSymbolTable(generateSymbolTableCode(gentypes, "idmap", this.getSchemaReader().getToplevel()));

		String toplevel = this.getSchemaReader().getToplevel();
		updateTypeInfoInSettings(toplevel,
				gen.getToplevelPackage() + "." + this.getSchemaReader().getToplevelFileName() + "." + toplevel);
		build();
	}

	private void build() {
		// // Building the code using ant
		File buildFile = new File("build.xml");
		Project p = new Project();
		p.setUserProperty("ant.file", buildFile.getAbsolutePath());
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(consoleLogger);

		try {
			p.fireBuildStarted();
			p.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			p.addReference("ant.projectHelper", helper);
			helper.parse(p, buildFile);
			p.executeTarget(p.getDefaultTarget());
			p.fireBuildFinished(null);
		} catch (BuildException e) {
			p.fireBuildFinished(e);
		}
	}

	public static void main(
			String[] args) {/*
							 * Compile and Build new Domain Data types
							 */
		String url = "/Users/mislam/Desktop/boa/protos/newSchema.proto";
		BuildCompiler builder = new BuildCompiler(url);
		try {
			builder.compileAndBuild();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
}
