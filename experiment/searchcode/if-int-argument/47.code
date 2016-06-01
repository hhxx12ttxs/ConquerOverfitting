public void checkNumberOfArgumentsEqualTo(int expecting, int actual)
throws InvalidBuiltInArgumentNumberException
{
if (expecting != actual) throw new InvalidBuiltInArgumentNumberException(expecting, actual);
for (int argumentNumber = 0; argumentNumber < arguments.size(); argumentNumber++)
if (!isArgumentAShort(argumentNumber, arguments)) return false;

