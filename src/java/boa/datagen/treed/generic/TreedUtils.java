package boa.datagen.treed.generic;

import java.util.ArrayList;
import java.util.HashMap;

import boa.types.Ast.Declaration;
import boa.types.Ast.Expression;
import boa.types.Ast.Expression.ExpressionKind;
import boa.types.Ast.Method;
import boa.types.Ast.Namespace;
import boa.types.Ast.Statement;
import boa.types.Ast.Statement.StatementKind;
import boa.types.Ast.Type;
import boa.types.Ast.Variable;

public final class TreedUtils {

	public static char buildLabelForVector(Object node, HashMap<String,Integer> nodeTypes) {
		
		String nodeType=TreedUtils.getNodeType(node);
		String nodeName=TreedUtils.getNodeName(node);
		
		char label = (char) ((int) nodeTypes.get(nodeType));
		
		if(nodeName!="")
		{
			return (char) (label | (nodeName.hashCode() << 7));
		}
		return label;
	}

	public static String buildASTLabel(Object node) {				
		return TreedUtils.getNodeType(node)+"["+TreedUtils.getNodeName(node)+"]";
	}
	
	public static String getNodeType(Object node)
	{		
		String nodeName=node.getClass().getSimpleName();
		if(node instanceof Statement)
		{
			nodeName=((Statement) node).getKind().toString();
		}
		else if(node instanceof boa.types.Ast.Expression)
		{
			nodeName=((boa.types.Ast.Expression) node).getKind().toString();
		}
		return nodeName;
	}
	
	public static String getNodeName(Object node)
	{		
		String nodeName="";
		
		if(node instanceof Statement)
		{
		}
		else if(node instanceof boa.types.Ast.Expression)
		{
			if(nodeName.equalsIgnoreCase("METHODCALL"))
				nodeName=((boa.types.Ast.Expression) node).getMethod();
			else if(nodeName.equalsIgnoreCase("VARACCESS"))
				nodeName=((boa.types.Ast.Expression) node).getVariable();
			else if(nodeName.equalsIgnoreCase("literal"))
				nodeName=((boa.types.Ast.Expression) node).getLiteral();
		}
		else if(node instanceof Variable)
		{
			nodeName=((Variable) node).getName();
			if(((Variable) node).getInitializer()!=null)
				nodeName+="Initialization";
		}
		else if(node instanceof Type)
		{
			nodeName=((Type) node).getName();
		}
		else if(node instanceof Declaration && ((Declaration) node).getName()!=null)
		{
			nodeName=((Declaration) node).getName();
		}
		else if(node instanceof Method && ((Method) node).getName()!=null)
		{
			nodeName=((Method) node).getName();
		}
		else if(node instanceof Namespace && ((Namespace) node).getName()!=null)
		{
			nodeName=((Namespace) node).getName();
		}
		return nodeName;
	}
	
	public static boolean isLiteral(Object n)
	{
		return (n instanceof Expression && 
				((Expression)n).getKind()==ExpressionKind.LITERAL);
	}
	public static boolean isReturnStatement(Object n)
	{
		return (n instanceof Statement && 
				((Statement)n).getKind()==StatementKind.RETURN);
	}
	public static boolean isBreakStatement(Object n)
	{
		return (n instanceof Statement && 
				((Statement)n).getKind()==StatementKind.BREAK);
	}
	public static boolean isContinueStatement(Object n)
	{
		return (n instanceof Statement && 
				((Statement)n).getKind()==StatementKind.CONTINUE);
	}
	public static boolean isWhileStatement(Object n)
	{
		return (n instanceof Statement && 
				((Statement)n).getKind()==StatementKind.WHILE);
	}
	public static boolean isForStatement(Object n)
	{
		return (n instanceof Statement && 
				((Statement)n).getKind()==StatementKind.FOREACH);
	}
	public static boolean isBlockStatement(Object n)
	{
		return (n instanceof Statement && 
				((Statement)n).getKind()==StatementKind.BLOCK);
	}

}
