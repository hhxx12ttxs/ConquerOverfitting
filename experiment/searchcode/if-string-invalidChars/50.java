public class StringConstraints implements Validator<String> {

public StringConstraints(int maxLength, CharacterSet validChars, CharacterSet invalidChars, boolean alwaysUppercase) {
public StringConstraints(int maxLength, CharacterSet validChars, CharacterSet invalidChars) {
this(maxLength, validChars, invalidChars, false);

