package org.bitbucket.bradleysmithllc.etlunit.maven.util;

import com.sun.codemodel.*;
import org.bitbucket.bradleysmithllc.json.validator.JsonSchemaObjectNode;

import java.util.List;
import java.util.Map;

public class JavaProperty
{
	private final String name;

	private final List<String> enumValues;

	private final Class dataTypeClass;
	private final boolean required;

	public JavaProperty(String name, JsonSchemaObjectNode.valid_type type, List<String> enumValues, boolean required)
	{
		this.name = name;
		this.required = required;

		this.enumValues = enumValues;

		if (enumValues == null)
		{
			switch (type)
			{
				case t_string:
					dataTypeClass = String.class;
					break;
				case t_number:
					dataTypeClass = String.class;
					break;
				case t_integer:
					dataTypeClass = int.class;
					break;
				case t_boolean:
					dataTypeClass = boolean.class;
					break;
				case t_object:
					dataTypeClass = Map.class;
					break;
				case t_array:
					dataTypeClass = List.class;
					break;
				case t_any:
					dataTypeClass = Object.class;
					break;
				case t_null:
					throw new IllegalArgumentException("Data type null not supported");
				default:
					throw new IllegalArgumentException("Data type not supported");
			}
		}
		else
		{
			this.dataTypeClass = null;
		}
	}

	public void append(JDefinedClass buffer) throws JClassAlreadyExistsException
	{
		JFieldVar field = buffer.field(JMod.FINAL | JMod.PUBLIC | JMod.STATIC, String.class, getProperPropertyName().toUpperCase() + "_JSON_NAME");

		field.init(JExpr.lit(name));

		JType dt = null;

		if (dataTypeClass == null)
		{
			// this is an enum
			JDefinedClass en = buffer._enum(getPropertyName() + "_enum");

			for (int i = 0; i < enumValues.size(); i++)
			{
				String e = enumValues.get(i);

				en.enumConstant(makePropertyName(e));
			}

			dt = en;
		}
		else
		{
			dt = buffer.getPackage().owner()._ref(dataTypeClass);
		}

		JFieldVar pfield = buffer.field(JMod.PRIVATE, dt, getPropertyName());

		JMethod meth = buffer.method(JMod.PUBLIC | JMod.FINAL, boolean.class, "has" + getProperPropertyName());
		JBlock bod = meth.body();
		bod.directStatement("return " + getPropertyName() + " != null;");

		JMethod getMth = buffer.method(JMod.PUBLIC | JMod.FINAL, dt, "get" + getProperPropertyName());

		if (required)
		{
			getMth.body().block().directStatement("if (!has" + getProperPropertyName() + "()) {throw new IllegalStateException(\"Property '" + getPropertyName() + "' never assigned\");}");
		}

		getMth.body().block()._return(pfield);
	}

	public String getProperPropertyName()
	{
		return makeProperPropertyName(name);
	}

	public static String makeProperPropertyName(String property)
	{
		String base = makePropertyName(property);

		base = Character.toUpperCase(base.charAt(0)) + base.substring(1);

		return base;
	}

	public String getPropertyName()
	{
		return makePropertyName(name);
	}

	public static String makePropertyName(String property)
	{
		StringBuffer buffer = new StringBuffer();

		char[] array = property.toCharArray();

		boolean capNext = false;

		for (char ch : array)
		{
			if (ch == '-' || ch == '_' || ch == '.')
			{
				capNext = true;
			}
			else
			{
				if (capNext)
				{
					buffer.append(Character.toUpperCase(ch));
					capNext = false;
				}
				else
				{
					buffer.append(ch);
				}
			}
		}

		return buffer.toString();
	}
}
