package timezra.eclipse.erlang.ui.editor.completion;

import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;

import timezra.eclipse.erlang.ui.editor.ErlangEditorPlugin;

public class ErlangCompletionProposal extends ScriptCompletionProposal {
	public ErlangCompletionProposal(final String replacementString, final int replacementOffset,
			final int replacementLength, final Image image, final String displayString, final int relevance) {
		super(replacementString, replacementOffset, replacementLength, image, displayString, relevance);
	}

	public ErlangCompletionProposal(final String replacementString, final int replacementOffset,
			final int replacementLength, final Image image, final String displayString, final int relevance,
			final boolean isInDoc) {
		super(replacementString, replacementOffset, replacementLength, image, displayString, relevance, isInDoc);
	}

	@Override
	protected boolean isSmartTrigger(final char trigger) {
		if (trigger == '.') {
			return true;
		}
		return false;
	}

	@Override
	protected boolean insertCompletion() {
		final IPreferenceStore preference = ErlangEditorPlugin.getPlugin().getPreferenceStore();
		return preference.getBoolean(PreferenceConstants.CODEASSIST_INSERT_COMPLETION);
	}
}

