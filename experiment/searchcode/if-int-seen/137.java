package fr.xebia.findbugs;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

public class XebiaDeprecatedUseDetector extends BytecodeScanningDetector {

	private BugReporter bugReporter;

	public XebiaDeprecatedUseDetector(BugReporter bugReporter) {
		this.bugReporter = bugReporter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sawOpcode(int seen) {

		if (seen == INVOKESTATIC || seen == NEW || seen == NEWARRAY
				|| seen == GETFIELD || seen == PUTFIELD
				|| seen == GETSTATIC || seen == PUTSTATIC
		) {

			try {
				Class clazz = Class.forName(getClassDescriptorOperand().getDottedClassName());
				if (clazz.getAnnotation(Deprecated.class) != null) {
					bugReporter.reportBug(new BugInstance("XEBIA_DEPRECATED", NORMAL_PRIORITY).addClassAndMethod(this).addSourceLine(this));
				}
			} catch (Throwable t) {

			}
		}
	}

}

