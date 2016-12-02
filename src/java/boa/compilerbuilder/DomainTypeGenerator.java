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
	private List<ProtoFile> schema;
	private String toplevelName;
	private String packagename;
	public static STGroup stg;

	public DomainTypeGenerator(List<ProtoFile> file, String toplevel) {
		if (file.size() <= 0) {
			throw new IllegalArgumentException();
		}
		this.stg = new STGroupFile("templates/DomainType.stg");
		this.memberbuilder = new StringBuilder();
		this.schema = file;
		this.toplevelName = toplevel;
		this.packagename = file.get(0).packageName();
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

	private String generateMemberCode(List<FieldElement> list, Map<String, String> messageTyp) {
		StringBuilder builder = new StringBuilder();

		for (FieldElement ele : list) {
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
		} else if ("uint32".equalsIgnoreCase(typ.toString())) {
			return "BoaInt";
		} else if ("uint64".equalsIgnoreCase(typ.toString())) {
			return "BoaInt";
		} else if ("uint".equalsIgnoreCase(typ.toString())) {
			return "BoaInt";
		} else if ("float".equalsIgnoreCase(typ.toString())) {
			return "BoaFloat";
		} else if ("double".equalsIgnoreCase(typ.toString())) {
			return "BoaDouble";
		} else if ("bool".equalsIgnoreCase(typ.toString())) {
			return "BoaBool";
		} else if ("bytes".equalsIgnoreCase(typ.toString())) {
			return "BoaString";
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
			mapTypEleToBoaTyp(map, ele.nestedElements());
		}
	}

	private void mapTypEleToBoaTyp(Map<String, String> map) {
		for (ProtoFile file : this.schema) {
			mapTypEleToBoaTyp(map, file.typeElements());
		}
	}

	/*
	 * Generates the code the Java version of Domain types
	 */
	ArrayList<GeneratedDomainType> generateCode() {
		// list containing all generated types
		ArrayList<GeneratedDomainType> generatedtyps = new ArrayList<GeneratedDomainType>();

		// a map to generatedMessageType to actual BoaType
		Map<String, String> messageTyp = new HashMap<String, String>();

		// map all the messages to Boa Types
		mapTypEleToBoaTyp(messageTyp);

		/*
		 * name of the package declared in the proto files FIXME: At present it
		 * is fixed to be boa.types.proto, fix it for any user specific package
		 * details
		 */

		StringBuilder qualifiedName = new StringBuilder();
		for (ProtoFile file : this.schema) {
			qualifiedName.delete(0, qualifiedName.length());
			qualifiedName.append(file.packageName());
			qualifiedName.append(".");
			String name = file.filePath().substring(0, file.filePath().lastIndexOf('.'));
			qualifiedName.append(name.substring(0, 1).toUpperCase() + name.substring(1));
			for (TypeElement element : file.typeElements()) {
				generatedtyps.addAll(generateCode(element, messageTyp, qualifiedName.toString()));
			}
		}
		return generatedtyps;
	}

	/**
	 * 
	 * @param element
	 *            ProtoColBufferElement for which the code to be generated
	 * @param messageTyp
	 *            mapping from ElementType to BoaTypes
	 * @param packageName
	 *            Name of the package
	 * @param fullyQualName
	 *            FullyQUalified name to be used in generatedCode
	 * @return A list of DomainTypes created
	 */
	ArrayList<GeneratedDomainType> generateCode(TypeElement element, Map<String, String> messageTyp,
			String fullyQualName) {
		// list of geenrated domain types
		ArrayList<GeneratedDomainType> generatedtyps = new ArrayList<GeneratedDomainType>();

		if (element instanceof MessageElement) {
			MessageElement ele = (MessageElement) element;

			// generate code for all fieldElements
			String code = generateMemberCode(ele.fields(), messageTyp);

			// fill template for DomainType code
			final ST st = stg.getInstanceOf("Program");
			st.add("name", ele.name());
			st.add("packagename", this.packagename + ".proto");
			st.add("nestedtypes", code);
			st.add("javatype", this.packagename + "." + this.toplevelName);

			// add generatedype in the list
			generatedtyps.add(new GeneratedDomainType(ele.name(), ele.name() + "ProtoTuple.java",
					this.packagename + ".proto", st.render()));

			// generate code for every nested message
			for (TypeElement nested : element.nestedElements()) {
				generatedtyps.addAll(generateCode(nested, messageTyp, fullyQualName + "." + element.name()));
			}

		} else if (element instanceof EnumElement) {
			EnumElement ele = (EnumElement) element;
			final ST st = stg.getInstanceOf("Enum");

			st.add("name", ele.name());
			st.add("packagename", this.packagename + ".proto");
			String type = fullyQualName + "." + ele.name() + ".class";
			st.add("clasname", type);
			generatedtyps.add(new GeneratedDomainType(ele.name(), ele.name() + "ProtoMap.java",
					this.packagename + ".proto", st.render()));

			for (TypeElement nested : element.nestedElements()) {
				generatedtyps.addAll(generateCode(nested, messageTyp, fullyQualName + "." + element.name()));
			}
		}
		return generatedtyps;
	}

	/**
	 * @return the schemaFileName
	 */
	public String getToplevelName() {
		return toplevelName;
	}

	public String getPackagename() {
		return this.packagename;
	}
}

class GeneratedDomainType {
	private String name;
	private String pckg;
	private String code;
	private String typename;

	GeneratedDomainType(String typename, String name, String pck, String code) {
		this.typename = typename;
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
	 * @return the code
	 */
	public String getType() {
		return typename;
	}

	/**
	 * @return the pckg
	 */
	public String getPckg() {
		return pckg;
	}
}
