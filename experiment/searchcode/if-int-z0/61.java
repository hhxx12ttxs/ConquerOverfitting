public class EmailValidator implements SubmissionValueVerifier {

public static int MAX_LENGTH = 50;

public boolean isValid(final String email) {
if( email != null &amp;&amp; email.length() <= MAX_LENGTH &amp;&amp; email.trim().toLowerCase().matches(&quot;[a-z0-9_.-]+@[a-z0-9_-]+\\.[a-z0-9_.-]+&quot;)) {

