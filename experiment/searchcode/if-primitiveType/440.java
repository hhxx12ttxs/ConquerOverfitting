package org.opu_newbee_education.java_oop_2015.stream_typed_read;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by salterok on 01.10.2015.
 */
class InputTypeBinder<T> {
    private Class<T> m_type;
    private String m_error;
    private Map<PrimitiveType, Field> contexts = new HashMap<>();

    public InputTypeBinder(Class<T> type) {
        m_type = type;
    }

    public boolean prepare() {
        Field[] fields = m_type.getDeclaredFields();
        for (Field field : fields) {
            BoundField boundField = field.getAnnotation(BoundField.class);
            if (boundField == null)
                continue;
            Class type = field.getType();
            PrimitiveType bindContext = createContext(type);
            if (bindContext == null) {
                m_error = String.format("Field %s of type %s is not primitive",
                        field.getName(), type.getSimpleName());
                return false;
            }
            contexts.put(bindContext, field);
        }
        return true;
    }

    private PrimitiveType createContext(Class type) {
        switch (type.getSimpleName()) {
            case "byte":
            case "Byte":
                return PrimitiveType.Byte;
            case "short":
            case "Short":
                return PrimitiveType.Short;
            case "int":
            case "Integer":
                return PrimitiveType.Int;
            case "long":
            case "Long":
                return PrimitiveType.Long;
            case "float":
            case "Float":
                return PrimitiveType.Float;
            case "double":
            case "Double":
                return PrimitiveType.Double;
            case "char":
            case "Char":
                return PrimitiveType.Char;
            case "String":
                return PrimitiveType.String;
            case "boolean":
            case "Boolean":
                return PrimitiveType.Boolean;

            default:
                return null;
        }
    }

    public String getError() {
        return m_error;
    }

    public T tryBind(Scanner scanner) throws IllegalAccessException, InstantiationException {
        T obj = m_type.newInstance();
        for (PrimitiveType getter : contexts.keySet()) {
            if (!getter.check.apply(scanner)) {
                m_error = String.format("Type %s expected but found '%s'",
                        getter.toString(), scanner.next("[^ ]+"));
                return null;
            }
            Object value = getter.next.apply(scanner);
            Field context = contexts.get(getter);
            if (!context.isAccessible())
                context.setAccessible(true);
            context.set(obj, value);
        }
        return obj;
    }

    enum PrimitiveType {
        Byte (Scanner::hasNextByte, Scanner::nextByte),
        Short (Scanner::hasNextShort, Scanner::nextShort),
        Int (Scanner::hasNextInt, Scanner::nextInt),
        Long (Scanner::hasNextLong, Scanner::nextLong),
        Float (Scanner::hasNextFloat, Scanner::nextFloat),
        Double (Scanner::hasNextDouble, Scanner::nextDouble),
        Char ((scanner) -> scanner.hasNext("\\w"), (scanner) -> scanner.next("\\w").charAt(0)),
        String ((scanner) -> scanner.hasNext("\\w+"), (scanner) -> scanner.next("\\w+")),
        Boolean (Scanner::hasNextBoolean, Scanner::nextBoolean);

        public Function<Scanner, Boolean> check;
        public Function<Scanner, ?> next;

        PrimitiveType(Function<Scanner, Boolean> check, Function<Scanner, ?> next) {
            this.check = check;
            this.next = next;
        }
    }
}

