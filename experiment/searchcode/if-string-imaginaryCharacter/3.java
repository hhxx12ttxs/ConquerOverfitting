* @param imaginaryCharacter The custom imaginary character.
*/
public ComplexFormat(String imaginaryCharacter) {
this(imaginaryCharacter, getDefaultNumberFormat());
public void setImaginaryCharacter(String imaginaryCharacter) {
if (imaginaryCharacter == null || imaginaryCharacter.length() == 0) {

