private CharSet invalidChars;

public InvalidCharFieldValidator(final String fieldName, final String invalidChars) {
final List<Character> toReturn = new ArrayList<Character>();

for (char c : charArray) {
if (invalidChars.contains(c))
{
toReturn.add(new Character(c));

