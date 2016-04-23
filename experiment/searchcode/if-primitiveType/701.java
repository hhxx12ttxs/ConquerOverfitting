package net.arctics.clonk.c4script.typing;

import static net.arctics.clonk.util.ArrayUtil.iterable;
import static net.arctics.clonk.util.ArrayUtil.map;

import java.util.Iterator;

import net.arctics.clonk.Core;
import net.arctics.clonk.c4script.Function;
import net.arctics.clonk.util.StringUtil;

public class FunctionType implements IRefinedPrimitiveType {
	private static final long serialVersionUID = Core.SERIAL_VERSION_UID;
	private final Function prototype;
	public FunctionType(final Function function) { this.prototype = function; }
	public Function prototype() { return prototype; }
	@Override
	public Iterator<IType> iterator() { return iterable(this, PrimitiveType.FUNCTION).iterator(); }
	@Override
	public IType simpleType() { return PrimitiveType.FUNCTION; }
	@Override
	public PrimitiveType primitiveType() { return PrimitiveType.FUNCTION; }
	@Override
	public String typeName(final boolean special) {
		if (!special)
			return PrimitiveType.FUNCTION.typeName(false);
		final StringBuilder builder = new StringBuilder();
		builder.append(PrimitiveType.FUNCTION.typeName(false));
		StringUtil.writeBlock(builder, "(", ")", ", ", map(this.prototype.parameters(), from -> from.type().typeName(false)));
		return builder.toString();
	}
}

