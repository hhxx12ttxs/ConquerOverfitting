public static final int SEEN_GOTO = 4;

public static final int SEEN_IF = 5;

private final BugReporter bugReporter;

private int state;
bug = null;
}

@Override
public void sawOpcode(int seen) {
if (seen == GOTO &amp;&amp; getBranchOffset() == 4) {

