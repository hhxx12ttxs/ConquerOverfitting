this(viewer.getDocument(), viewer.getTextWidget().getTabs());
if (viewer instanceof ITextViewerExtension) {
ITextViewerExtension ext= (ITextViewerExtension)viewer;
} finally {
if (complex && fRewriteTarget != null)
fRewriteTarget.endCompoundChange();
fRewriteTarget.endCompoundChange();
boolean complex= edit.hasChildren();
if (complex && fRewriteTarget != null)
fRewriteTarget.beginCompoundChange();
if (complex && fRewriteTarget != null)
} finally {
boolean complex= edit.hasChildren();
if (complex && fRewriteTarget != null)
fRewriteTarget.beginCompoundChange();

