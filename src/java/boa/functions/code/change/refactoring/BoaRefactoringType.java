package boa.functions.code.change.refactoring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum BoaRefactoringType {

		EXTRACT_OPERATION("Extract Method", "Extract Method (.+) extracted from (.+) in class (.+)", 2),
		RENAME_CLASS("Rename Class", "Rename Class (.+) renamed to (.+)"),
		MOVE_ATTRIBUTE("Move Attribute", "Move Attribute (.+) from class (.+) to (.+) from class (.+)"),
		MOVE_RENAME_ATTRIBUTE("Move And Rename Attribute", "Move And Rename Attribute (.+) renamed to (.+) and moved from class (.+) to class (.+)"),
		REPLACE_ATTRIBUTE("Replace Attribute", "Replace Attribute (.+) from class (.+) with (.+) from class (.+)"),
		RENAME_METHOD("Rename Method", "Rename Method (.+) renamed to (.+) in class (.+)"),
		INLINE_OPERATION("Inline Method", "Inline Method (.+) inlined to (.+) in class (.+)", 2),
		MOVE_OPERATION("Move Method", "Move Method (.+) from class (.+) to (.+) from class (.+)"),
		PULL_UP_OPERATION("Pull Up Method", "Pull Up Method (.+) from class (.+) to (.+) from class (.+)", 1, 2),
		MOVE_CLASS("Move Class", "Move Class (.+) moved to (.+)"),
		MOVE_RENAME_CLASS("Move And Rename Class", "Move And Rename Class (.+) moved and renamed to (.+)"),
		MOVE_SOURCE_FOLDER("Move Source Folder", "Move Source Folder (.+) to (.+)"),
		PULL_UP_ATTRIBUTE("Pull Up Attribute", "Pull Up Attribute (.+) from class (.+) to (.+) from class (.+)", 2),
		PUSH_DOWN_ATTRIBUTE("Push Down Attribute", "Push Down Attribute (.+) from class (.+) to (.+) from class (.+)", 3),
		PUSH_DOWN_OPERATION("Push Down Method", "Push Down Method (.+) from class (.+) to (.+) from class (.+)", 3, 4),
		EXTRACT_INTERFACE("Extract Interface", "Extract Interface (.+) from classes \\[(.+)\\]", 2),
		EXTRACT_SUPERCLASS("Extract Superclass", "Extract Superclass (.+) from classes \\[(.+)\\]", 2),
		EXTRACT_SUBCLASS("Extract Subclass", "Extract Subclass (.+) from class (.+)"),
		EXTRACT_CLASS("Extract Class", "Extract Class (.+) from class (.+)"),
		MERGE_OPERATION("Merge Method", ".+"),
		EXTRACT_AND_MOVE_OPERATION("Extract And Move Method", "Extract And Move Method (.+) extracted from (.+) in class (.+) & moved to class (.+)"),
		CONVERT_ANONYMOUS_CLASS_TO_TYPE("Convert Anonymous Class to Type", ".+"),
		INTRODUCE_POLYMORPHISM("Introduce Polymorphism", ".+"),
		RENAME_PACKAGE("Change Package", "Change Package (.+) to (.+)"),
		CHANGE_METHOD_SIGNATURE("Change Method Signature", "Change Method Signature (.+) to (.+) in class (.+)"),
		EXTRACT_VARIABLE("Extract Variable", "Extract Variable (.+) in method (.+) from class (.+)"),
		INLINE_VARIABLE("Inline Variable", "Inline Variable (.+) in method (.+) from class (.+)"),
		RENAME_VARIABLE("Rename Variable", "Rename Variable (.+) to (.+) in method (.+) from class (.+)"),
		RENAME_PARAMETER("Rename Parameter", "Rename Parameter (.+) to (.+) in method (.+) from class (.+)"),
		RENAME_ATTRIBUTE("Rename Attribute", "Rename Attribute (.+) to (.+) in class (.+)"),
		MERGE_VARIABLE("Merge Variable", "Merge Variable \\[(.+)\\] to (.+) in method (.+) from class (.+)"),
		MERGE_PARAMETER("Merge Parameter", "Merge Parameter \\[(.+)\\] to (.+) in method (.+) from class (.+)"),
		MERGE_ATTRIBUTE("Merge Attribute", "Merge Attribute \\[(.+)\\] to (.+) in class (.+)"),
		SPLIT_VARIABLE("Split Variable", "Split Variable (.+) to \\[(.+)\\] in method (.+) from class (.+)"),
		SPLIT_PARAMETER("Split Parameter", "Split Parameter (.+) to \\[(.+)\\] in method (.+) from class (.+)"),
		SPLIT_ATTRIBUTE("Split Attribute", "Split Attribute (.+) to \\[(.+)\\] in class (.+)"),
		REPLACE_VARIABLE_WITH_ATTRIBUTE("Replace Variable With Attribute", "Replace Variable With Attribute (.+) to (.+) in method (.+) from class (.+)"),
		PARAMETERIZE_VARIABLE("Parameterize Variable", "Parameterize Variable (.+) to (.+) in method (.+) from class (.+)"),
		CHANGE_RETURN_TYPE("Change Return Type", "Change Return Type (.+) to (.+) in method (.+) from class (.+)"),
		CHANGE_VARIABLE_TYPE("Change Variable Type", "Change Variable Type (.+) to (.+) in method (.+) from class (.+)"),
		CHANGE_PARAMETER_TYPE("Change Parameter Type", "Change Parameter Type (.+) to (.+) in method (.+) from class (.+)"),
		CHANGE_ATTRIBUTE_TYPE("Change Attribute Type", "Change Attribute Type (.+) to (.+) in class (.+)");

		private String displayName;
		private Pattern regex;
		private int[] aggregateGroups;

		private BoaRefactoringType(String displayName, String regex, int ... aggregateGroups) {
			this.displayName = displayName;
			this.regex = Pattern.compile(regex);
			this.aggregateGroups = aggregateGroups;
		}

		public Pattern getRegex() {
	        return regex;
	    }

	    public String getDisplayName() {
			return this.displayName;
		}
	    
	    public static String getPackageBefore(String description) {
	    	BoaRefactoringType type = BoaRefactoringType.extractFromDescription(description);
//			System.out.println(type.getDisplayName());
			Matcher m = type.getRegex().matcher(description);
			if (m.matches() && type == RENAME_PACKAGE) {
				return m.group(1);
			}
			return null;
	    }

	    public static String[] getBeforeClasses(String description) {
	    	BoaRefactoringType type = BoaRefactoringType.extractFromDescription(description);
//			System.out.println(type.getDisplayName());
			Matcher m = type.getRegex().matcher(description);
			String[] classes = new String[1];
			if (m.matches()) {
				switch (type) {
				case RENAME_CLASS:
	            case MOVE_CLASS: 
	            case MOVE_RENAME_CLASS: {
//	            	System.out.println("1 " + description);
	            	classes[0] = m.group(1);
	            	break;
	            }
	            case MOVE_OPERATION:
	            case PULL_UP_OPERATION:
	            case PUSH_DOWN_OPERATION:
	            case MOVE_ATTRIBUTE:
	            case PULL_UP_ATTRIBUTE:
	            case PUSH_DOWN_ATTRIBUTE: {
	            	classes[0] = m.group(2);
	            	break;
	            }
	            case RENAME_METHOD:
	            case INLINE_OPERATION:
	            case EXTRACT_OPERATION:
	            case EXTRACT_AND_MOVE_OPERATION: {
//	            	System.out.println("3 " + description);
	            	classes[0]  = m.group(3);
	            	break;
	            }
	            case EXTRACT_INTERFACE:
	            case EXTRACT_SUPERCLASS: {
	            	classes = m.group(2).split(" *, *");
	            	break;
	            }
	            default:
	            	return new String[0];
				}
			}
			return classes;
	    }

	    public static BoaRefactoringType extractFromDescription(String refactoringDescription) {
	        for (BoaRefactoringType refType : BoaRefactoringType.values()) {
	            if (refactoringDescription.startsWith(refType.getDisplayName())) {
	                return refType;
	            }
	        }
	        throw new RuntimeException("Unknown refactoring type: " + refactoringDescription);
	    }

	    public String getGroup(String refactoringDescription, int group) {
	        Matcher m = regex.matcher(refactoringDescription);
	        if (m.matches()) {
	            return m.group(group);
	        } else {
	            throw new RuntimeException("Pattern not matched: " + refactoringDescription);
	        }
	    }
	    
	    public static BoaRefactoringType fromName(String name) {
	      String lcName = name.toLowerCase();
	      for (BoaRefactoringType rt : BoaRefactoringType.values()) {
	        if (lcName.equals(rt.getDisplayName().toLowerCase())) {
	          return rt;
	        }
	      }
	      throw new IllegalArgumentException("refactoring type not known " + name);
	    }
	}
