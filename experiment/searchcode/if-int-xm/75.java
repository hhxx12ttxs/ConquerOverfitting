* Window Manager honours all modes.
*/
int dialogStyle = OS.XmDIALOG_MODELESS;
if ((style &amp; SWT.PRIMARY_MODAL) != 0) dialogStyle = OS.XmDIALOG_PRIMARY_APPLICATION_MODAL;
int label = OS.XmMessageBoxGetChild (dialogHandle, OS.XmDIALOG_MESSAGE_LABEL);
if (label != 0) {
int [] argList = {OS.XmNfontList, 0};
OS.XtGetValues (label, argList, argList.length / 2);

