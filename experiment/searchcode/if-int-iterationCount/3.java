public static void main(String[] args)
{
int iterationCount = 5;
if (args.length > 0)
{
if (isNumeric(args[0]))
{
iterationCount = Integer.parseInt(args[0]);

