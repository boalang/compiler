package boa.graphs.slicers.python;

import java.util.ArrayList;

import boa.functions.BoaStringIntrinsics;

public class NameResolver {
	public static String getReachableAliasMappedName(String usedIdentifierName, Integer useAstLocation) {
		return getReachableAliasMappedName(usedIdentifierName, useAstLocation, Status.getCurrentScope());
	}

	public static String getReachableAliasMappedName(String usedIdentifierName, Integer useAstLocation, String scope) {
		String targetScope = scope;
		while (!scope.equals("")) {
			String str = getReachableAliasMappedNameResolvedScope(usedIdentifierName, useAstLocation, scope,
					targetScope);
			if (str.equals("-"))
				return "-";
			if (!str.equals("")) {
				Status.nameResolveDepth = Status.getNumScope(scope);
				return str;
			}
			scope = Status.getParentScope(scope);
		}
		return "";
	}

	private static String getReachableAliasMappedNameResolvedScope(String usedIdentifierName, Integer useAstLocation,
			String sourceScope, String targetScope) {
		ArrayList<Integer> defs = SymbolTable.getDefLocations(sourceScope, usedIdentifierName);
		if (defs == null || defs.size() == 0)
			return ""; // not defined in this scope

		for (Integer loc : defs) {
			if (Status.aliasName.containsKey(loc)
					&& CfgUtil.isAstNodesReachable(loc, useAstLocation, usedIdentifierName, sourceScope, targetScope)) {
				return Status.aliasName.get(loc);
			}
		}
		return "-"; // defined in this scope, but not reachable
	}

	public static String resolveName(String usedIdentifierName, Integer useAstLocation) {

		String importName=resolveImport(usedIdentifierName, useAstLocation, Status.getCurrentScope());
		if(importName!="") return importName;
		return resolveObjectName(usedIdentifierName, useAstLocation, Status.getCurrentScope());
	}

	public static String resolveImport(String usedIdentifierName, Integer useAstLocation) {

		return resolveImport(usedIdentifierName, useAstLocation, Status.getCurrentScope());
	}

	public static String resolveImport(String usedIdentifierName, Integer useAstLocation, String scope) {

		String str = resolveImportInternal(usedIdentifierName, useAstLocation, scope);
		Status.nameResolveDepth = -1;
		if (Status.nameResolveDepth != -1) {
			Integer importDepth = Status.nameResolveDepth;
			Status.nameResolveDepth = -1;
			resolveObjectNameInternal(usedIdentifierName, useAstLocation, scope);
			if (Status.nameResolveDepth == -1)
				return str;
			if (Status.nameResolveDepth <= importDepth)
				return str;
			return "";
		}
		return str;
	}

	private static String resolveImportInternal(String usedIdentifierName, Integer useAstLocation, String scope) {
		String[] tarr = BoaStringIntrinsics.splitall(usedIdentifierName, "\\.");
		if (tarr.length == 0)
			return "";

		for (int i = tarr.length - 1; i >= 0; i--) {
			String str = "";
			for (int j = 0; j <= i; j++)
				str = str + "." + tarr[j];
			str = BoaStringIntrinsics.substring(str, 1);

			String mt1 = getReachableAliasMappedName(str, useAstLocation, scope);

			if (mt1.equals("-"))
				return "";

			if (!mt1.equals("")) {
				str = mt1;
				for (int j = i + 1; j < tarr.length; j++) {
					str = str + "." + tarr[j];
				}

				return constuctAliasNameFromBaseImport(str);
			}
		}
		return constuctAliasNameFromBaseImport(usedIdentifierName);
	}

	static String constuctAliasNameFromBaseImport(String str) {
		String[] narr = BoaStringIntrinsics.splitall(str, "\\.");
		str = "";
		String ret = "";
		int pos = 0;
		for (int j = 0; j < narr.length; j++) {
			if (j == 0)
				str = narr[j];
			else
				str = str + "." + narr[j];

			if (Status.importMap.containsKey(str)) {
				ret = Status.importMap.get(str);
				pos = j;
			}
		}
		if (ret == "")
			return "";

		for (int j = pos + 1; j < narr.length; j++)
			ret = ret + "." + narr[j];
		return ret;
	}

	public static String resolveObjectName(String usedIdentifierName, Integer useAstLocation) {

		return resolveObjectName(usedIdentifierName, useAstLocation, Status.getCurrentScope());
	}

	public static String resolveObjectName(String usedIdentifierName, Integer useAstLocation, String scope) {

		String str = resolveObjectNameInternal(usedIdentifierName, useAstLocation, scope);
		Status.nameResolveDepth = -1;
		if (Status.nameResolveDepth != -1) {
			Integer objectDepth = Status.nameResolveDepth;
			Status.nameResolveDepth = -1;
			resolveImportInternal(usedIdentifierName, useAstLocation, scope);
			if (Status.nameResolveDepth == -1)
				return str;
			if (Status.nameResolveDepth <= objectDepth)
				return str;
			return "";
		}
		return str;
	}

	private static String resolveObjectNameInternal(String usedIdentifierName, Integer useAstLocation, String scope) {
		String[] tarr = BoaStringIntrinsics.splitall(usedIdentifierName, "\\.");
		if (tarr.length == 0)
			return "";

		String str;
		boolean objectResolve = true;

		for (int i = tarr.length - 1; i >= 0; i--) {
			str = "";
			for (int j = 0; j <= i; j++)
				str = str + "." + tarr[j];
			str = BoaStringIntrinsics.substring(str, 1);

			String mt1 = getReachableAliasMappedName(str, useAstLocation, scope);

			if (mt1.equals("-"))
				return "";

			if (!mt1.equals("")) {
				str = mt1;
				objectResolve = true;
				for (int j = i + 1; j < tarr.length; j++) {
					str = str + "." + tarr[j];
					if (!Status.objectNameMap.containsKey(str)) {
						objectResolve = false;
						break;
					}
				}
				if (objectResolve)
					return str;

			}
		}
		str = "";
		objectResolve = true;
		for (int j = 0; j < tarr.length; j++) {
			if (j == 0)
				str = tarr[j];
			else
				str = str + "." + tarr[j];
			if (!Status.objectNameMap.containsKey(str)) {
				objectResolve = false;
			}
		}
		if (objectResolve)
			return str;
		return "";
	}


}
