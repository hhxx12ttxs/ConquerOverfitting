public static void assertJSONEqual(String control, String test)
{
Diff diff = new Diff(control, test);
if (!diff.similar())
{
Assert.fail(diff.getMessage());
public static void assertJSONNotEqual(String control, String test)
{
Diff diff = new Diff(control, test);
if (diff.similar())
{
Assert.fail(diff.getMessage());
}
}
}

