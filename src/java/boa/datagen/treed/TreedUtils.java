package boa.datagen.treed;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;

public final class TreedUtils {

	public static char buildLabelForVector(ASTNode node) {
		char label = (char) node.getNodeType();
		System.out.println(node.toString());

		if (node instanceof Expression) {
			if (node.getClass().getSimpleName().endsWith("Literal")) {
				return (char) (label | (node.toString().hashCode() << 7));
			}
			int type = node.getNodeType();
			switch (type) {
			case ASTNode.INFIX_EXPRESSION:
				return (char) (label | (((InfixExpression) node).getOperator().toString().hashCode() << 7));
			case ASTNode.SIMPLE_NAME:
				return (char) (label | (node.toString().hashCode() << 7));
			case ASTNode.POSTFIX_EXPRESSION:
				return (char) (label | (((PostfixExpression) node).getOperator().toString().hashCode() << 7));
			case ASTNode.PREFIX_EXPRESSION:
				return (char) (label | (((PrefixExpression) node).getOperator().toString().hashCode() << 7));
			default:
				break;
			}
		} else if (node instanceof Modifier) {
			return (char) (label | (node.toString().hashCode() << 7));
		} else if (node instanceof Type) {
			if (node instanceof PrimitiveType)
				return (char) (label | (node.toString().hashCode() << 7));
		} else if (node instanceof TextElement) {
			return (char) (label | (node.toString().hashCode() << 7));
		} else if (node instanceof TagElement) {
			String tag = ((TagElement) node).getTagName();
			if (tag != null)
				return (char) (label | (((TagElement) node).getTagName().hashCode() << 7));
		}
		return label;
	}

	public static String buildASTLabel(ASTNode node) {
		String label = node.getClass().getSimpleName();
		if (node instanceof Expression) {
			if (node.getClass().getSimpleName().endsWith("Literal")) {
				return label + "(" + node.toString() + ")";
			}
			int type = node.getNodeType();
			switch (type) {
			case ASTNode.INFIX_EXPRESSION:
				return label + "(" + ((InfixExpression) node).getOperator().toString() + ")";
			case ASTNode.SIMPLE_NAME:
				return label + "(" + node.toString() + ")";
			case ASTNode.POSTFIX_EXPRESSION:
				return label + "(" + ((PostfixExpression) node).getOperator().toString() + ")";
			case ASTNode.PREFIX_EXPRESSION:
				return label + "(" + ((PrefixExpression) node).getOperator().toString() + ")";
			default:
				break;
			}
		} else if (node instanceof Modifier) {
			return label + "(" + node.toString() + ")";
		} else if (node instanceof Type) {
			if (node instanceof PrimitiveType)
				return label + "(" + node.toString() + ")";
		} else if (node instanceof TextElement) {
			return label + "(" + node.toString() + ")";
		} else if (node instanceof TagElement) {
			String tag = ((TagElement) node).getTagName();
			if (tag == null)
				return label;
			return label + "(" + tag + ")";
		}
		return label;
	}

}
