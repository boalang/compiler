package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaBool;
import boa.types.BoaInt;
import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;

/**
 * A {@link ExpressionProtoTuple}.
 * 
 * @author rdyer
 */
public class ExpressionProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		names.put("kind", 0);
		members.add(new BoaInt());

		names.put("expressions", 1);
		members.add(new BoaProtoList(new ExpressionProtoTuple()));

		names.put("variable_decls", 2);
		members.add(new BoaProtoList(new VariableProtoTuple()));

		names.put("new_type", 3);
		members.add(new TypeProtoTuple());

		names.put("generic_parameters", 4);
		members.add(new BoaProtoList(new TypeProtoTuple()));

		names.put("is_postfix", 5);
		members.add(new BoaBool());

		names.put("literal", 6);
		members.add(new BoaString());

		names.put("variable", 7);
		members.add(new BoaString());

		names.put("method", 8);
		members.add(new BoaString());

		names.put("method_args", 9);
		members.add(new BoaProtoList(new ExpressionProtoTuple()));

		names.put("anon_declaration", 10);
		members.add(new DeclarationProtoTuple());

		names.put("annotation", 11);
		members.add(new ModifierProtoTuple());
	}

	/**
	 * Construct a {@link ExpressionProtoTuple}.
	 */
	public ExpressionProtoTuple() {
		super(members, names);
	}

	@Override
	public String toJavaType() {
		return "boa.types.Ast.Expression";
	}
}
