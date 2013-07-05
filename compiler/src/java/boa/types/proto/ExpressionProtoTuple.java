package boa.types.proto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import boa.types.BoaBool;
import boa.types.BoaProtoList;
import boa.types.BoaProtoTuple;
import boa.types.BoaString;
import boa.types.BoaType;
import boa.types.proto.enums.ExpressionKindProtoMap;

/**
 * A {@link ExpressionProtoTuple}.
 * 
 * @author rdyer
 */
public class ExpressionProtoTuple extends BoaProtoTuple {
	private final static List<BoaType> members = new ArrayList<BoaType>();
	private final static Map<String, Integer> names = new HashMap<String, Integer>();

	static {
		int counter = 0;

		names.put("kind", counter++);
		members.add(new ExpressionKindProtoMap());

		names.put("expressions", counter++);
		members.add(new BoaProtoList(new ExpressionProtoTuple()));

		names.put("variable_decls", counter++);
		members.add(new BoaProtoList(new VariableProtoTuple()));

		names.put("new_type", counter++);
		members.add(new TypeProtoTuple());

		names.put("generic_parameters", counter++);
		members.add(new BoaProtoList(new TypeProtoTuple()));

		names.put("is_postfix", counter++);
		members.add(new BoaBool());

		names.put("literal", counter++);
		members.add(new BoaString());

		names.put("variable", counter++);
		members.add(new BoaString());

		names.put("method", counter++);
		members.add(new BoaString());

		names.put("method_args", counter++);
		members.add(new BoaProtoList(new ExpressionProtoTuple()));

		names.put("anon_declaration", counter++);
		members.add(new DeclarationProtoTuple());

		names.put("annotation", counter++);
		members.add(new ModifierProtoTuple());
	}

	/**
	 * Construct a {@link ExpressionProtoTuple}.
	 */
	public ExpressionProtoTuple() {
		super(members, names);
	}

	/** @{inheritDoc} */
	@Override
	public String toJavaType() {
		return "boa.types.Ast.Expression";
	}
}
