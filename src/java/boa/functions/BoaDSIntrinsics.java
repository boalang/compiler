package boa.functions;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.*;
import boa.types.Ast.Modifier.ModifierKind;
import boa.types.Code.CodeRepository;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

import static boa.functions.BoaIntrinsics.getSnapshot;
import static boa.functions.BoaAstIntrinsics.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoaDSIntrinsics {

	@FunctionSpec(name = "test", returnType = "string", formalParameters = { "Project" })
	public static String test(final Project p) throws Exception {
		CodeRepository cr = p.getCodeRepositories(0);
		ChangedFile[] snapshot = getSnapshot(cr);
		Set<String> nodes = collectNodes(snapshot);
		List<Features> featureList = collectClassFeatures(snapshot, nodes);
//		for (Features f : featureList)
//			System.out.println(f.name);
		return null;
	}

	private static Set<String> collectNodes(ChangedFile[] snapshot) throws Exception {
		Set<String> nodes = new HashSet<>();
		BoaAbstractVisitor visitor = new BoaAbstractVisitor() {
			@Override
			protected boolean preVisit(Declaration d) throws Exception {
				for (Variable v : d.getFieldsList())
					if (v.getVariableType().getName().equals(d.getName()))
						nodes.add(d.getName());
				for (Declaration next : d.getNestedDeclarationsList())
					visit(next);
				return false;
			}
		};
		for (ChangedFile cf : snapshot)
			visitor.visit(cf);
		return nodes;
	}

	private static List<Features> collectClassFeatures(ChangedFile[] snapshot, Set<String> nodes) throws Exception {
		List<Features> res = new ArrayList<>();

		BoaAbstractVisitor visitor = new BoaAbstractVisitor() {
			@Override
			protected boolean preVisit(Declaration d) throws Exception {
				Features features = new Features(d.getName());
				for (Variable v : d.getFieldsList()) {
					// ignore primitive and immutable
					if (isPrimitive(v) || isImmutable(v))
						continue;

					String type = v.getVariableType().getName();

					if (type.contains("List<")) {
						features.list++;
					}
					if (type.contains("Map<")) {
						features.map++;
					}
					if (type.contains("[]")) {
						features.array++;
					}
					if (nodes.contains(type) && type != d.getName()) {
						features.node++;
					}
				}
				if (!features.isEmpty())
					res.add(features);
				for (Declaration next : d.getNestedDeclarationsList())
					visit(next);
				return false;
			}

		};
		for (ChangedFile cf : snapshot)
			visitor.visit(cf);
		return res;
	}

	@FunctionSpec(name = "immutable", returnType = "bool", formalParameters = { "Variable" })
	public static boolean isImmutable(Variable v) {
		if (v.getModifiersList().stream().anyMatch(m -> m.getKind() == ModifierKind.FINAL))
			return true;
		return v.getVariableType().getName().equals("String");
	}

	private static Set<String> primitiveSet = new HashSet<>(
			Arrays.asList("byte", "short", "int", "long", "float", "double", "boolean", "char", "Byte", "Short",
					"Integer", "Long", "Float", "Double", "Boolean", "Character"));

	@FunctionSpec(name = "primitive", returnType = "bool", formalParameters = { "Variable" })
	public static boolean isPrimitive(Variable v) {
		return primitiveSet.contains(v.getVariableType().getName());
	}

}

class Features {
	String name;
	// organization of data
	int array, list, set, map, object, node;

	// Accessing Methods
	// Operations
	public boolean isEmpty() {
		return array + list + set + map + object + node == 0;
	}

	public Features(String name) {
		this.name = name;
	}
}