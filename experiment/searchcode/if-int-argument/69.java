checkArgumentExists(parameterArgument);
return parameterArgument.stringVar();
}

public int getIntArgument(String argumentName, int defaultInt) {
Parameter.Argument parameterArgument = getArgument(argumentName);

