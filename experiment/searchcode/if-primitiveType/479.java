package com.bookofbrilliantthings.mustache4j;

import com.bookofbrilliantthings.mustache4j.util.SwitchableWriter;


public abstract class VariableRenderer
    extends StackingRenderer
{
    private final boolean escaped;

    protected VariableRenderer(int objectDepth, boolean escaped, Class<?> forClass)
    {
        super(objectDepth);

        // this constructor is really more for type checking than anything else
        final PrimitiveType primitiveType = PrimitiveType.getSwitchType(forClass);
        if (primitiveType == PrimitiveType.OBJECT)
            throw new RuntimeException("only primitive type and String variables are supported (" +
                    forClass.getName() + ")");

        this.escaped = escaped;
    }

    public abstract Object getValue(Object o)
        throws Exception;

    @Override
    public void render(final SwitchableWriter writer, final ObjectStack objectStack)
        throws Exception
    {
        final Object value = getValue(objectStack.peekAt(objectDepth));
        if (value != null)
        {
            final boolean on = writer.setFiltered(escaped);
            writer.write(value.toString());
            writer.setFiltered(on);
        }
    }
}

