public boolean areAllArgumentsShorts(List<BuiltInArgument> arguments) throws BuiltInException
{
for (int argumentNumber = 0; argumentNumber < arguments.size(); argumentNumber++)
if (!isArgumentAShort(argumentNumber, arguments)) return false;

