package boa.graphs.slicers.python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Statement;
import boa.types.Ast.Variable;

public class ForwardSlicerUtil {

	public static boolean isAssignKind(Expression node) {
		if (node == null)
			return false;
		String kind = node.getKind().toString().toLowerCase();
		return kind.startsWith("assign");
	}

	public static boolean isProperAssignKind(Expression node) {
		if (isAssignKind(node) == false)
			return false;

		return node.getExpressionsCount() == 2;
	}
	
	public static boolean isMethodCallKind(Expression node) {
		return node.getKind()==ExpressionKind.METHODCALL;
	}
	
	public static boolean isCfgDefined(String scope) {
		if(!Status.cfgMap.containsKey(scope)) return false;
		if(Status.cfgMap.get(scope)==null) return false;
		if(Status.cfgMap.get(scope).getNodes()==null ||
				Status.cfgMap.get(scope).getNodes().size()<1)
			return false;
		return true;
	}

	// returns map of variable names: for example, a,b=2,3 -> return [(a, id),(b,id)]
	public static HashMap<String, Integer> getIdentiferNames(Expression node) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();

		if (node == null)
			return ret;

		if (node.getKind() == ExpressionKind.OTHER || node.getKind() == ExpressionKind.TUPLE) {
			for (Expression e : node.getExpressionsList()) {
				ret.put(convertExpressionToString(e), e.getId());
			}
		} else
			ret.put(convertExpressionToString(node), node.getId());

		return ret;
	}

	// Example: a.b().c[] -> a.b.c
	public static String convertExpressionToString(Expression node) {
		String str = "";
		String tmp = "";

		for (Expression e : node.getExpressionsList()) {
			tmp = "";
			if (e.getKind() == ExpressionKind.ARRAYACCESS) {
				if (e.getExpressionsCount() > 0) {
					tmp = getExpressionKeyText(e.getExpressions(0));
				}
			} else
				tmp = getExpressionKeyText(e);

			if (tmp != "")
				str = str + "." + tmp;
		}

		tmp = getExpressionKeyText(node);
		if (tmp != "")
			str = str + "." + tmp;

		if (str != "")
			str = str.substring(1);

		return str;
	}

	public static String getExpressionKeyText(Expression node) {
		String str = "";

		if (node.getKind() == ExpressionKind.VARACCESS) {
			if (node.hasVariable())
				str = node.getVariable();
		} else if (node.getKind() == ExpressionKind.LITERAL) {
			if (node.hasLiteral())
				str = node.getLiteral();
		} else if (node.getKind() == ExpressionKind.METHODCALL) {
			if (node.hasMethod())
				str = node.getMethod();
		}

		return str;
	}
	
	// returns map of variable names: for example, with open() as pd -> return [(pd, id)]
		public static HashMap<String, Integer> getIdentiferNames(Statement node) {
			HashMap<String, Integer> ret = new HashMap<String, Integer>();

			if (node == null)
				return ret;

			for (Variable e : node.getVariableDeclarationsList()) {
				if(e.hasName())
					ret.put(e.getName(), e.getId());
				else if(e.hasComputedName() && e.getComputedName().hasVariable())
					ret.put( e.getComputedName().getVariable(), e.getComputedName().getId());
			}

			return ret;
		}

}
