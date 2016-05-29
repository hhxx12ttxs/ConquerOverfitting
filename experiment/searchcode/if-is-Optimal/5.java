return OptimalResult.success(null_p().unpickle(source));
} else if (source instanceof BooleanNode) {
} else if (NumericNode.class.isAssignableFrom(source.getClass())) {
return OptimalResult.success(double_p().unpickle(source));

