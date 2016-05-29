assertTrue(controllerNormalize.checkIfRightNumber(normalized));
}*/
@Test
public void testNormalizeNumberWithNonCorrectTelephone() throws Exception {
String normalized = controllerNormalize.normalizeNumber(tel);
assertFalse(controllerNormalize.checkIfRightNumber(normalized));

