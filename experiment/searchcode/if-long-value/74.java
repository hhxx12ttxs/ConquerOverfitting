public Long newInstance(Object value) {
if ( value instanceof Long ) {
return (Long) value;
}
return Long.parseLong(String.valueOf(value));
}

@Override
public Class<Long> getComponentType() {
return Long.class;
}

}

