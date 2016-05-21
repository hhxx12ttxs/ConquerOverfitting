//First check to make sure procedure has not already been uploaded
// the Uri specified in the intent.
protected void onNewIntent(Intent intent) {
import android.content.DialogInterface;
import android.content.Intent;
try {
if(instance == null && !getIntent().hasExtra(\"savedProcedureUri\")) {
// We are a new activity, so get the procedure prototype's URI from the Intent.
showDialog(DIALOG_ALREADY_UPLOADED);
// It then launches the native camera app, which stores an image at
import org.moca.db.MocaDB.SavedProcedureSQLFormat;
import org.moca.net.MDSInterface;
import org.moca.procedure.PictureElement;
if (MDSInterface.isProcedureAlreadyUploaded(thisSavedProcedure, getBaseContext())) {
import android.content.Context;

