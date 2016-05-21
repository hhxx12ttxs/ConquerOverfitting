if (MDSInterface.isProcedureAlreadyUploaded(thisSavedProcedure, getBaseContext())) {
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
showDialog(DIALOG_ALREADY_UPLOADED);
// It then launches the native camera app, which stores an image at
// the Uri specified in the intent.
protected void onNewIntent(Intent intent) {
try {
if(instance == null && !getIntent().hasExtra(\"savedProcedureUri\")) {
// We are a new activity, so get the procedure prototype's URI from the Intent.
import org.moca.net.APIException;
import org.moca.net.MDSInterface;
import org.moca.procedure.PictureElement;
//First check to make sure procedure has not already been uploaded

