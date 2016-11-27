package boa.compilerbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.squareup.protoparser.DataType;
import com.squareup.protoparser.EnumElement;
import com.squareup.protoparser.FieldElement;
import com.squareup.protoparser.MessageElement;
import com.squareup.protoparser.OptionElement;
import com.squareup.protoparser.ProtoFile;
import com.squareup.protoparser.TypeElement;

public class DomainTypeGenerator {
	private StringBuilder memberbuilder;
	private ProtoFile schema;
	private String schemaFileName;
	public static STGroup stg;

	public DomainTypeGenerator(ProtoFile file) {
		this.stg = new STGroupFile("templates/DomainType.stg");
		this.memberbuilder = new StringBuilder();
		this.schema = file;
		String filename = file.filePath().substring(0, file.filePath().lastIndexOf('.'));
		this.schemaFileName = filename.substring(0, 1).toUpperCase() + filename.substring(1);
	}

	private String getCodeForNestedTyp(String name, String type, boolean isList) {
		this.memberbuilder.delete(0, memberbuilder.length());

		// add name
		this.memberbuilder.append("names.put(\"" + name + "\", counter++);");

		// add new line for clear
		this.memberbuilder.append("\n");

		// add member element
		if (isList) {
			this.memberbuilder.append("members.add(new BoaProtoList(new " + type + "()));");
		} else {
			this.memberbuilder.append("members.add(new " + type + "());");
		}
		this.memberbuilder.append("\n");
		return this.memberbuilder.toString();
	}

	private String generateMemberCode(ArrayList<FieldElement> members, Map<String, String> messageTyp) {
		StringBuilder builder = new StringBuilder();

		for (FieldElement ele : members) {
			builder.append(getCodeForNestedTyp(ele.name(), getElementTyp(ele, messageTyp), isListType(ele)));
			builder.append("\n");
		}

		return builder.toString();
	}

	private String getElementTyp(FieldElement ele, Map<String, String> messageTyp) {
		DataType typ = ele.type();
		if ("string".equalsIgnoreCase(typ.toString())) {
			return "BoaString";
		} else if ("int32".equalsIgnoreCase(typ.toString())) {
			return "BoaInt";
		} else if ("int64".equalsIgnoreCase(typ.toString())) {
			return "BoaInt";
		} else if ("int".equalsIgnoreCase(typ.toString())) {
			return "BoaInt";
		} else if ("float".equalsIgnoreCase(typ.toString())) {
			return "BoaFloat";
		} else if ("double".equalsIgnoreCase(typ.toString())) {
			return "BoaDouble";
		} else if ("bool".equalsIgnoreCase(typ.toString())) {
			return "BoaBool";
		} else {
			return typ.toString() + messageTyp.get(typ.toString());
		}
	}

	private boolean isListType(FieldElement ele) {
		for (OptionElement option : ele.options()) {
			if (option.kind() == OptionElement.Kind.LIST) {
				return true;
			}
		}
		return false;
	}

	private void mapTypEleToBoaTyp(Map<String, String> map, List<TypeElement> messages) {
		for (TypeElement ele : messages) {
			if (ele instanceof MessageElement) {
				map.put(ele.name(), "ProtoTuple");
			} else if (ele instanceof EnumElement) {
				map.put(ele.name(), "ProtoMap");
			}
		}
	}

	/*
	 * Generates the code the Java version of Domain types
	 */
	ArrayList<GeneratedDomainType> generateCode() {
		// list containing all generated types
		ArrayList<GeneratedDomainType> generatedtyps = new ArrayList<GeneratedDomainType>();

		// List of all messages in the schema
		List<TypeElement> messages = this.schema.typeElements();

		// a map to generatedMessageType to actual BoaType
		Map<String, String> messageTyp = new HashMap<String, String>();

		// map all the messages to Boa Types
		mapTypEleToBoaTyp(messageTyp, messages);

		/*
		 * name of the package declared in the proto files FIXME: At present it
		 * is fixed to be boa.types.proto, fix it for any user specific package
		 * details
		 */

		String packageName = this.schema.packageName() + ".proto";

		// an array for storing all the field elements of all messages. It is
		// used for each messgetype hence defining out of loop
		ArrayList<FieldElement> fieldEles = new ArrayList<FieldElement>();

		for (TypeElement element : messages) {
			if (element instanceof MessageElement) {
				// if type is message the it has fieldelements and it is of type
				// BoaProtoTuple
				MessageElement ele = (MessageElement) element;
				fieldEles.clear();
				for (FieldElement nested : ele.fields()) {
					fieldEles.add(nested);
				}
				String code = generateMemberCode(fieldEles, messageTyp);
				final ST st = stg.getInstanceOf("Program");

				st.add("name", ele.name());
				st.add("packagename", packageName);
				st.add("nestedtypes", code);
				generatedtyps.add(new GeneratedDomainType(ele.name() + "ProtoTuple.java", packageName, st.render()));
			} else if (element instanceof EnumElement) {
				// if type is message the it has EnumElement and it is of type
				// BoaProtoMap
				EnumElement ele = (EnumElement) element;
				final ST st = stg.getInstanceOf("Enum");

				st.add("name", ele.name());
				st.add("packagename", packageName);
				String type = packageName.substring(0, packageName.lastIndexOf('.')) + "." + this.schemaFileName + "."
						+ ele.name() + ".class";
				st.add("clasname", type);
				generatedtyps.add(new GeneratedDomainType(ele.name() + "ProtoMap.java", packageName, st.render()));

			}
		}
		return generatedtyps;
	}
}

class GeneratedDomainType {
	String name;
	String pckg;
	String code;

	GeneratedDomainType(String name, String pck, String code) {
		this.name = name;
		this.pckg = pck;
		this.code = code;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the pckg
	 */
	public String getPckg() {
		return pckg;
	}
}
