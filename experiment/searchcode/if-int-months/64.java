if(instance == null && !getIntent().hasExtra(\"savedProcedureUri\")) {
//First check to make sure procedure has not already been uploaded
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
try {
// the Uri specified in the intent.
// We are a new activity, so get the procedure prototype's URI from the Intent.
showDialog(DIALOG_ALREADY_UPLOADED);
protected void onNewIntent(Intent intent) {
if (MDSInterface.isProcedureAlreadyUploaded(thisSavedProcedure, getBaseContext())) {
// It then launches the native camera app, which stores an image at
import org.moca.net.MDSInterface;
import org.moca.procedure.PictureElement;
import org.moca.net.APIException;

