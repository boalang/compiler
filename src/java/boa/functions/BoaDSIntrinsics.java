package boa.functions;

import boa.runtime.BoaAbstractVisitor;
import boa.types.Ast.*;
import boa.types.Ast.Expression.ExpressionKind;
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

	@FunctionSpec(name = "test", returnType = "array of string", formalParameters = { "Project" })
	public static String[] test(final Project p) throws Exception {
		CodeRepository cr = p.getCodeRepositories(0);
		ChangedFile[] snapshot = getSnapshot(cr);
		Set<String> nodes = collectNodes(snapshot);
		List<Features> featureList = collectClassFeatures(snapshot, nodes);
		String[] res = new String[featureList.size()];
		int idx = 0;
		for (Features f : featureList)
			res[idx++] = f.toString();
		return res;
	}

	private static Set<String> collectNodes(ChangedFile[] snapshot) throws Exception {
		Set<String> nodes = new HashSet<>();
		BoaAbstractVisitor visitor = new BoaAbstractVisitor() {
			@Override
			protected boolean preVisit(Declaration d) throws Exception {
				if (declFilter(d))
					return false;
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
				if (declFilter(d))
					return false;
				Features features = new Features(d.getName());
				for (Variable v : d.getFieldsList()) {
					// ignore primitive and immutable
					if (isPrimitive(v) || isImmutable(v))
						continue;
					updateFeatures(d, v, features, nodes);
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

	private static void updateFeatures(Declaration d, Variable v, Features features, Set<String> nodes) {
		String type = v.getVariableType().getName();
		if (type.endsWith("[]")) {
			// String[] options = {"Send Feedback", "Close"}
			if (v.hasInitializer() && v.getInitializer().getKind() == ExpressionKind.ARRAYINIT) {
				return;
			}
			features.array++;
		} else {
			int[][] nums = new int[][] { { isList(type), 0 }, { isUion(type), 1 }, { isDictionary(type), 2 } };
			Arrays.sort(nums, (a, b) -> a[0] - b[0]);
			if (nums[0][0] != Integer.MAX_VALUE) {
				if (nums[0][1] == 0) {
					features.list++;
//					System.out.println(type);
				} else if (nums[0][1] == 1) {
					features.union++;
//					System.out.println(type);
				} else if (nums[0][1] == 2) {
					features.dic++;
//					System.out.println(type);
				}
			} else if (nodes.contains(type) && !d.getName().equals(type)) {
				features.node++;
//				System.out.println(type);
			} else {
				features.object++;
//				System.out.println(type);
			}
		}

	}

	private static int isList(String type) {
		int res = Integer.MAX_VALUE;
		List<String> tokens = Arrays.asList("Queue", "Deque", "Stack", "List");
		for (String token : tokens) {
			if (type.indexOf(token) > -1) {
				res = type.indexOf(token);
				break;
			}
		}
		return res;
	}

	private static int isUion(String type) {
		int res = Integer.MAX_VALUE;
		List<String> tokens = Arrays.asList("Set");
		for (String token : tokens) {
			if (type.indexOf(token) > -1) {
				res = type.indexOf(token);
				break;
			}
		}
		return res;
	}

	private static int isDictionary(String type) {
		int res = Integer.MAX_VALUE;
		List<String> tokens = Arrays.asList("Map", "Hashtable");
		for (String token : tokens) {
			if (type.indexOf(token) > -1) {
				res = type.indexOf(token);
				break;
			}
		}
		return res;
	}

	private static boolean declFilter(Declaration d) {
		return d.getKind() != TypeKind.CLASS;
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
	int array, list, union, dic, node, object;

	// Accessing Methods
	// Operations
	public boolean isEmpty() {
		return array + list + union + dic + node + object == 0;
	}

	public Features(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(' ');
		sb.append(array).append(' ');
		sb.append(list).append(' ');
		sb.append(union).append(' ');
		sb.append(dic).append(' ');
		sb.append(node).append(' ');
		sb.append(object).append(' ');
		return sb.toString();
	}
}