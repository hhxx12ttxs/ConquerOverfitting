package com.olinasc.jsourcetemplate.processor;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.TypeDeclaration;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.olinasc.jsourcetemplate.processor.api.FieldByFieldProcessAction;
import com.olinasc.jsourcetemplate.util.CompilationUnitUtils;

/**
 * Reference processor implementation. It adds chain methods for all the non
 * static non final fields of classes. It uses a "withXxxx" template for fields
 * that are not a collection and an "addXxxx" otherwise.
 * 
 * @author victorolinasc
 * 
 */
public final class ChainMethodProcessor extends FieldByFieldProcessAction {

	private static final String WITH_TEMPLATE = "public ${enclosing_type} ${method_name}(${param_type} ${arg_name}) { this.${arg_name} = ${arg_name}; return this; }";
	private static final String ADD_TEMPLATE = "public ${enclosing_type} ${method_name}(${coll_type} ${arg_name}) { this.${field_name}.add(${arg_name}); return this; }";

	@Override
	public Set<FieldDeclaration> filterFields(CompilationUnit unit) {

		final Set<FieldDeclaration> fields = new HashSet<FieldDeclaration>();
		final List<TypeDeclaration> types = unit.getTypes();

		for (TypeDeclaration typeDeclaration : types) {

			final List<BodyDeclaration> members = typeDeclaration.getMembers();

			for (BodyDeclaration bodyDeclaration : members) {

				if (bodyDeclaration instanceof FieldDeclaration) {

					int modifiers = ((FieldDeclaration) bodyDeclaration).getModifiers();

					if (ModifierSet.hasModifier(modifiers, Modifier.STATIC)
							|| ModifierSet.hasModifier(modifiers, Modifier.FINAL))
						continue;

					fields.add((FieldDeclaration) bodyDeclaration);
				}
			}
		}

		return fields;
	}

	@Override
	public String processField(FieldDeclaration field, CompilationUnit unit) {

		if (isCollection(field))
			return processAddTemplate(field, unit);

		else
			return processWithTemplate(field, unit);
	}

	private String processWithTemplate(FieldDeclaration fieldDeclaration,
			CompilationUnit unit) {

		final String enclosingType = CompilationUnitUtils.getTypeName(unit);
		final String paramType = fieldDeclaration.getType().toString();
		final String argName = fieldDeclaration.getVariables().get(0).getId()
				.getName();

		final String methodName = "with" + Character.toUpperCase(argName.charAt(0)) + argName.substring(1, argName.length());

		if (CompilationUnitUtils.hasMethod(unit, methodName, enclosingType, Arrays.asList(paramType)))
			return null;

		return WITH_TEMPLATE
				.replace("${enclosing_type}", enclosingType)
				.replace("${param_type}", paramType)
				.replace("${arg_name}", argName)
				.replace("${method_name}", methodName);
	}

	private String processAddTemplate(FieldDeclaration fieldDeclaration,
			CompilationUnit unit) {

		final String enclosingType = CompilationUnitUtils.getTypeName(unit);

		final String argType = fieldDeclaration.getType().toString();
		final String collType = argType.substring(argType.indexOf('<') + 1, argType.lastIndexOf('>'));

		final String fieldName = fieldDeclaration.getVariables().get(0).getId()
				.getName();

		final String nonPluralFieldName = removePlural(fieldName);

		final String methodName = "add"
				// upperCase
				+ Character.toUpperCase(nonPluralFieldName.charAt(0))
				// rest of name
				+ nonPluralFieldName.substring(1, nonPluralFieldName.length());

		if (CompilationUnitUtils.hasMethod(unit, methodName, enclosingType, Arrays.asList(collType)))
			return null;

		return ADD_TEMPLATE
				.replace("${enclosing_type}", enclosingType)
				.replace("${method_name}", methodName)
				.replace("${coll_type}", collType)
				.replace("${arg_name}", nonPluralFieldName)
				.replace("${field_name}", fieldName);
	}

	private String removePlural(String fieldName) {

		if (fieldName.endsWith("ies"))
			return fieldName.substring(0, fieldName.length() - 3) + "y";

		if (fieldName.endsWith("ches") || fieldName.endsWith("xes") || fieldName.endsWith("ses"))
			return fieldName.substring(0, fieldName.length() - 2);

		if (fieldName.endsWith("s"))
			return fieldName.substring(0, fieldName.length() - 1);

		return fieldName;
	}

	private boolean isCollection(FieldDeclaration fieldDeclaration) {

		final String typeDeclaration = fieldDeclaration.getType().toString();

		final boolean set = typeDeclaration.contains("Set");
		final boolean list = typeDeclaration.contains("List");

		return set || list;
	}
}
