PushNotificationPayload complexPayload = PushNotificationPayload.complex();
} else if (complex) {
/* Verify that the test is being invoked  */
if (!verifyCorrectUsage(NotificationTest.class, args, \"keystore-path\", \"keystore-password\", \"device-token\", \"[production|sandbox]\", \"[complex|simple|threads]\", \"[#devices]\", \"[#threads]\")) return;
private static Payload createComplexPayload() {
 * 
try {
 * 
 * <p>Also by default, this test pushes a simple alert.  To send a complex payload, add \"complex\" as a fifth parameter:<\/p>
 * <p>Example:  <code>java -cp \"[required libraries]\" javapns.test.NotificationTest keystore.p12 mypass 2ed202ac08ea9033665e853a3dc8bc4c5e78f7a6cf8d55910df230567037dcc4 production complex<\/code><\/p>
/* Push a more complex payload */
List<PushedNotification> notifications = Push.payload(createComplexPayload(), keystore, password, production, token);
printPushedNotifications(notifications);

