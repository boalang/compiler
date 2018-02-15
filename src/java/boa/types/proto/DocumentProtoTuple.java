package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;

public class DocumentProtoTuple extends BoaProtoTuple {

	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("title", counter++);
		members.add(new BoaString());
		
		names.put("elements", counter++);
		members.add(new BoaProtoList(new ElementProtoTuple()));
		
		names.put("doc_type", counter++);
		members.add(new ElementProtoTuple());
		
		names.put("processing_instruction", counter++);
		members.add(new BoaProtoList(new AttributeProtoTuple()));

	}

	public DocumentProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Document";
	}
}
