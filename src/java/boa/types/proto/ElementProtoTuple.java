package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.ElementKindProtoMap;

public class ElementProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();
	
	static {
		int counter = 0;

		names.put("tag", counter++);
		members.add(new BoaString());
	
		names.put("kind", counter++);
		members.add(new ElementKindProtoMap());
		
		names.put("elements", counter++);
		members.add(new BoaProtoList(new ElementProtoTuple()));
		
		names.put("text", counter++);
		members.add(new BoaProtoList(new BoaString()));
		
		names.put("data", counter++);
		members.add(new BoaProtoList(new BoaString()));
		
		names.put("attributes", counter++);
		members.add(new BoaProtoList(new AttributeProtoTuple()));
		
		names.put("script", counter++);
		members.add(new NamespaceProtoTuple());
		
		names.put("php", counter++);
		members.add(new NamespaceProtoTuple());
		
		names.put("var_decl", counter++);
		members.add(new BoaProtoList(new VariableProtoTuple()));
		
		names.put("processing_instruction", counter++);
		members.add(new BoaProtoList(new AttributeProtoTuple()));
	}
	
	public ElementProtoTuple() {
		super(members, names);
		// TODO Auto-generated constructor stub
	}
	
	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Element";
	}
}


