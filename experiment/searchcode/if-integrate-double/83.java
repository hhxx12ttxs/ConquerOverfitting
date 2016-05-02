package pfc.uma;

/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * <p>A utility class which helps ease integration with Barcode Scanner via {@link Intent}s. This is a simple
 * way to invoke barcode scanning and receive the result, without any need to integrate, modify, or learn the
 * project's source code.</p>
 *
 * <h2>Initiating a barcode scan</h2>
 *
 * <p>To integrate, create an instance of {@code IntentIntegrator} and call {@link #initiateScan()} and wait
 * for the result in your app.</p>
 *
 * <p>It does require that the Barcode Scanner (or work-alike) application is installed. The
 * {@link #initiateScan()} method will prompt the user to download the application, if needed.</p>
 *
 * <p>There are a few steps to using this integration. First, your {@link Activity} must implement
 * the method {@link Activity#onActivityResult(int, int, Intent)} and include a line of code like this:</p>
 *
 * <pre>{@code
 * public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 *   IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 *   if (scanResult != null) {
 *     // handle scan result
 *   }
 *   // else continue with any other code you need in the method
 *   ...
 * }
 * }</pre>
 *
 * <p>This is where you will handle a scan result.</p>
 *
 * <p>Second, just call this in response to a user action somewhere to begin the scan process:</p>
 *
 * <pre>{@code
 * IntentIntegrator integrator = new IntentIntegrator(yourActivity);
 * integrator.initiateScan();
 * }</pre>
 *
 * <p>Note that {@link #initiateScan()} returns an {@link AlertDialog} which is non-null if the
 * user was prompted to download the application. This lets the calling app potentially manage the dialog.
 * In particular, ideally, the app dismisses the dialog if it's still active in its {@link Activity#onPause()}
 * method.</p>
 * 
 * <p>You can use {@link #setTitle(String)} to customize the title of this download prompt dialog (or, use
 * {@link #setTitleByID(int)} to set the title by string resource ID.) Likewise, the prompt message, and
 * yes/no button labels can be changed.</p>
 *
 * <p>Finally, you can use {@link #addExtra(String, Object)} to add more parameters to the Intent used
 * to invoke the scanner. This can be used to set additional options not directly exposed by this
 * simplified API.</p>
 * 
 * <p>By default, this will only allow applications that are known to respond to this intent correctly
 * do so. The apps that are allowed to response can be set with {@link #setTargetApplications(Collection)}.
 * For example, set to {@link #TARGET_BARCODE_SCANNER_ONLY} to only target the Barcode Scanner app itself.</p>
 *
 * <h2>Sharing text via barcode</h2>
 *
 * <p>To share text, encoded as a QR Code on-screen, similarly, see {@link #shareText(CharSequence)}.</p>
 *
 * <p>Some code, particularly download integration, was contributed from the Anobiit application.</p>
 *
 * <h2>Enabling experimental barcode formats</h2>
 *
 * <p>Some formats are not enabled by default even when scanning with {@link #ALL_CODE_TYPES}, such as
 * {@link com.google.zxing.BarcodeFormat#PDF_417}. Use {@link #initiateScan(java.util.Collection)} with
 * a collection containing the names of formats to scan for explicitly, like "PDF_417", to use such
 * formats.</p>
 *
 * @author Sean Owen
 * @author Fred Lin
 * @author Isaac Potoczny-Jones
 * @author Brad Drehmer
 * @author gcstang
 */
public class IntentIntegrator {

  public static final int REQUEST_CODE = 0x0000c0de; // Only use bottom 16 bits
  private static final String TAG = IntentIntegrator.class.getSimpleName();

  public static final String DEFAULT_TITLE = "Install Barcode Scanner?";
  public static final String DEFAULT_MESSAGE =
      "This application requires Barcode Scanner. Would you like to install it?";
  public static final String DEFAULT_YES = "Yes";
  public static final String DEFAULT_NO = "No";

  private static final String BS_PACKAGE = "com.google.zxing.client.android";
  private static final String BSPLUS_PACKAGE = "com.srowen.bs.android";

  // supported barcode formats
  public static final Collection<String> PRODUCT_CODE_TYPES = list("UPC_A", "UPC_E", "EAN_8", "EAN_13", "RSS_14");
  public static final Collection<String> ONE_D_CODE_TYPES =
      list("UPC_A", "UPC_E", "EAN_8", "EAN_13", "CODE_39", "CODE_93", "CODE_128",
           "ITF", "RSS_14", "RSS_EXPANDED");
  public static final Collection<String> QR_CODE_TYPES = Collections.singleton("QR_CODE");
  public static final Collection<String> DATA_MATRIX_TYPES = Collections.singleton("DATA_MATRIX");

  public static final Collection<String> ALL_CODE_TYPES = null;
  
  public static final Collection<String> TARGET_BARCODE_SCANNER_ONLY = Collections.singleton(BS_PACKAGE);
  public static final Collection<String> TARGET_ALL_KNOWN = list(
          BS_PACKAGE, // Barcode Scanner
          BSPLUS_PACKAGE, // Barcode Scanner+
          BSPLUS_PACKAGE + ".simple" // Barcode Scanner+ Simple
          // What else supports this intent?
      );
  
  private final Activity activity;
  private String title;
  private String message;
  private String buttonYes;
  private String buttonNo;
  private Collection<String> targetApplications;
  private final Map<String,Object> moreExtras;
  
  public IntentIntegrator(Activity activity) {
    this.activity = activity;
    title = DEFAULT_TITLE;
    message = DEFAULT_MESSAGE;
    buttonYes = DEFAULT_YES;
    buttonNo = DEFAULT_NO;
    targetApplications = TARGET_ALL_KNOWN;
    moreExtras = new HashMap<String,Object>(3);
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }

  public void setTitleByID(int titleID) {
    title = activity.getString(titleID);
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setMessageByID(int messageID) {
    message = activity.getString(messageID);
  }

  public String getButtonYes() {
    return buttonYes;
  }

  public void setButtonYes(String buttonYes) {
    this.buttonYes = buttonYes;
  }

  public void setButtonYesByID(int buttonYesID) {
    buttonYes = activity.getString(buttonYesID);
  }

  public String getButtonNo() {
    return buttonNo;
  }

  public void setButtonNo(String buttonNo) {
    this.buttonNo = buttonNo;
  }

  public void setButtonNoByID(int buttonNoID) {
    buttonNo = activity.getString(buttonNoID);
  }
  
  public Collection<String> getTargetApplications() {
    return targetApplications;
  }
  
  public void setTargetApplications(Collection<String> targetApplications) {
    this.targetApplications = targetApplications;
  }
  
  public void setSingleTargetApplication(String targetApplication) {
    this.targetApplications = Collections.singleton(targetApplication);
  }

  public Map<String,?> getMoreExtras() {
    return moreExtras;
  }

  public void addExtra(String key, Object value) {
    moreExtras.put(key, value);
  }

  /**
   * Initiates a scan for all known barcode types.
   */
  public AlertDialog initiateScan() {
    return initiateScan(ALL_CODE_TYPES);
  }

  /**
   * Initiates a scan only for a certain set of barcode types, given as strings corresponding
   * to their names in ZXing's {@code BarcodeFormat} class like "UPC_A". You can supply constants
   * like {@link #PRODUCT_CODE_TYPES} for example.
   *
   * @return the {@link AlertDialog} that was shown to the user prompting them to download the app
   *   if a prompt was needed, or null otherwise
   */
  public AlertDialog initiateScan(Collection<String> desiredBarcodeFormats) {
    Intent intentScan = new Intent(BS_PACKAGE + ".SCAN");
    intentScan.addCategory(Intent.CATEGORY_DEFAULT);

    // check which types of codes to scan for
    if (desiredBarcodeFormats != null) {
      // set the desired barcode types
      StringBuilder joinedByComma = new StringBuilder();
      for (String format : desiredBarcodeFormats) {
        if (joinedByComma.length() > 0) {
          joinedByComma.append(',');
        }
        joinedByComma.append(format);
      }
      intentScan.putExtra("SCAN_FORMATS", joinedByComma.toString());
    }

    String targetAppPackage = findTargetAppPackage(intentScan);
    if (targetAppPackage == null) {
      return showDownloadDialog();
    }
    intentScan.setPackage(targetAppPackage);
    intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    attachMoreExtras(intentScan);
    startActivityForResult(intentScan, REQUEST_CODE);
    return null;
  }

  /**
   * Start an activity.<br>
   * This method is defined to allow different methods of activity starting for
   * newer versions of Android and for compatibility library.
   *
   * @param intent Intent to start.
   * @param code Request code for the activity
   * @see android.app.Activity#startActivityForResult(Intent, int)
   * @see android.app.Fragment#startActivityForResult(Intent, int)
   */
  protected void startActivityForResult(Intent intent, int code) {
    activity.startActivityForResult(intent, code);
  }
  
  private String findTargetAppPackage(Intent intent) {
    PackageManager pm = activity.getPackageManager();
    List<ResolveInfo> availableApps = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    if (availableApps != null) {
      for (ResolveInfo availableApp : availableApps) {
        String packageName = availableApp.activityInfo.packageName;
        if (targetApplications.contains(packageName)) {
          return packageName;
        }
      }
    }
    return null;
  }

  private AlertDialog showDownloadDialog() {
    AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
    downloadDialog.setTitle(title);
    downloadDialog.setMessage(message);
    downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        Uri uri = Uri.parse("market://details?id=" + BS_PACKAGE);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
          activity.startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
          // Hmm, market is not installed
          Log.w(TAG, "Android Market is not installed; cannot install Barcode Scanner");
        }
      }
    });
    downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {}
    });
    return downloadDialog.show();
  }


  /**
   * <p>Call this from your {@link Activity}'s
   * {@link Activity#onActivityResult(int, int, Intent)} method.</p>
   *
   * @return null if the event handled here was not related to this class, or
   *  else an {@link IntentResult} containing the result of the scan. If the user cancelled scanning,
   *  the fields will be null.
   */
  public static IntentResult parseActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        String contents = intent.getStringExtra("SCAN_RESULT");
        String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
        byte[] rawBytes = intent.getByteArrayExtra("SCAN_RESULT_BYTES");
        int intentOrientation = intent.getIntExtra("SCAN_RESULT_ORIENTATION", Integer.MIN_VALUE);
        Integer orientation = intentOrientation == Integer.MIN_VALUE ? null : intentOrientation;
        String errorCorrectionLevel = intent.getStringExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL");
        return new IntentResult(contents,
                                formatName,
                                rawBytes,
                                orientation,
                                errorCorrectionLevel);
      }
      return new IntentResult();
    }
    return null;
  }


  /**
   * Defaults to type "TEXT_TYPE".
   * @see #shareText(CharSequence, CharSequence)
   */
  public AlertDialog shareText(CharSequence text) {
    return shareText(text, "TEXT_TYPE");
  }

  /**
   * Shares the given text by encoding it as a barcode, such that another user can
   * scan the text off the screen of the device.
   *
   * @param text the text string to encode as a barcode
   * @param type type of data to encode. See {@code com.google.zxing.client.android.Contents.Type} constants.
   * @return the {@link AlertDialog} that was shown to the user prompting them to download the app
   *   if a prompt was needed, or null otherwise
   */
  public AlertDialog shareText(CharSequence text, CharSequence type) {
    Intent intent = new Intent();
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    intent.setAction(BS_PACKAGE + ".ENCODE");
    intent.putExtra("ENCODE_TYPE", type);
    intent.putExtra("ENCODE_DATA", text);
    String targetAppPackage = findTargetAppPackage(intent);
    if (targetAppPackage == null) {
      return showDownloadDialog();
    }
    intent.setPackage(targetAppPackage);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    attachMoreExtras(intent);
    activity.startActivity(intent);
    return null;
  }
  
  private static Collection<String> list(String... values) {
    return Collections.unmodifiableCollection(Arrays.asList(values));
  }

  private void attachMoreExtras(Intent intent) {
    for (Map.Entry<String,Object> entry : moreExtras.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      // Kind of hacky
      if (value instanceof Integer) {
        intent.putExtra(key, (Integer) value);
      } else if (value instanceof Long) {
        intent.putExtra(key, (Long) value);
      } else if (value instanceof Boolean) {
        intent.putExtra(key, (Boolean) value);
      } else if (value instanceof Double) {
        intent.putExtra(key, (Double) value);
      } else if (value instanceof Float) {
        intent.putExtra(key, (Float) value);
      } else if (value instanceof Bundle) {
        intent.putExtra(key, (Bundle) value);
      } else {
        intent.putExtra(key, value.toString());
      }
    }
  }

}








///*
// * Copyright 2009 ZXing authors
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.ActivityNotFoundException;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.net.Uri;
//import android.util.Log;
//
///**
// * <p>A utility class which helps ease integration with Barcode Scanner via {@link Intent}s. This is a simple
// * way to invoke barcode scanning and receive the result, without any need to integrate, modify, or learn the
// * project's source code.</p>
// *
// * <h2>Initiating a barcode scan</h2>
// *
// * <p>Integration is essentially as easy as calling {@link #initiateScan(Activity)} and waiting
// * for the result in your app.</p>
// *
// * <p>It does require that the Barcode Scanner application is installed. The
// * {@link #initiateScan(Activity)} method will prompt the user to download the application, if needed.</p>
// *
// * <p>There are a few steps to using this integration. First, your {@link Activity} must implement
// * the method {@link Activity#onActivityResult(int, int, Intent)} and include a line of code like this:</p>
// *
// * <pre>{@code
// * public void onActivityResult(int requestCode, int resultCode, Intent intent) {
// *   IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
// *   if (scanResult != null) {
// *     // handle scan result
// *   }
// *   // else continue with any other code you need in the method
// *   ...
// * }
// * }</pre>
// *
// * <p>This is where you will handle a scan result.
// * Second, just call this in response to a user action somewhere to begin the scan process:</p>
// *
// * <pre>{@code IntentIntegrator.initiateScan(yourActivity);}</pre>
// *
// * <p>You can use {@link #initiateScan(Activity, CharSequence, CharSequence, CharSequence, CharSequence)} or
// * {@link #initiateScan(Activity, int, int, int, int)} to customize the download prompt with
// * different text labels.</p>
// *
// * <p>Note that {@link #initiateScan(Activity)} returns an {@link AlertDialog} which is non-null if the
// * user was prompted to download the application. This lets the calling app potentially manage the dialog.
// * In particular, ideally, the app dismisses the dialog if it's still active in its {@link Activity#onPause()}
// * method.</p>
// *
// * <h2>Sharing text via barcode</h2>
// *
// * <p>To share text, encoded as a QR Code on-screen, similarly, see {@link #shareText(Activity, CharSequence)}.</p>
// *
// * <p>Some code, particularly download integration, was contributed from the Anobiit application.</p>
// *
// * @author Sean Owen
// * @author Fred Lin
// * @author Isaac Potoczny-Jones
// * @author Brad Drehmer
// * @author gcstang
// */
//public final class IntentIntegrator {
//
//  public static final int REQUEST_CODE = 0x0ba7c0de; // get it?
//  private static final String TAG = IntentIntegrator.class.getSimpleName();
//
//  public static final String DEFAULT_TITLE = "Install Barcode Scanner?";
//  public static final String DEFAULT_MESSAGE =
//      "This application requires Barcode Scanner. Would you like to install it?";
//  public static final String DEFAULT_YES = "Yes";
//  public static final String DEFAULT_NO = "No";
//
//  private static final String PACKAGE = "com.google.zxing.client.android";
//
//  // supported barcode formats
//  public static final String PRODUCT_CODE_TYPES = "UPC_A,UPC_E,EAN_8,EAN_13";
//  public static final String ONE_D_CODE_TYPES = PRODUCT_CODE_TYPES + ",CODE_39,CODE_93,CODE_128";
//  public static final String QR_CODE_TYPES = "QR_CODE";
//  public static final String ALL_CODE_TYPES = null;
//
//  public static final Method PACKAGE_SETTER;
//  static {
//    Method temp;
//    try {
//      temp = Intent.class.getMethod("setPackage", new Class[] {String.class});
//    } catch (NoSuchMethodException nsme) {
//      temp = null;
//    }
//    PACKAGE_SETTER = temp;
//  }
//  
//  private IntentIntegrator() {
//	  
//  }
//
//  /**
//   * See {@link #initiateScan(Activity, CharSequence, CharSequence, CharSequence, CharSequence)} --
//   * same, but uses default English labels.
//   */
//  public static AlertDialog initiateScan(Activity activity) {
//    return initiateScan(activity, DEFAULT_TITLE, DEFAULT_MESSAGE, DEFAULT_YES, DEFAULT_NO);
//  }
//
//  /**
//   * See {@link #initiateScan(Activity, CharSequence, CharSequence, CharSequence, CharSequence)} --
//   * same, but takes string IDs which refer
//   * to the {@link Activity}'s resource bundle entries.
//   */
//  public static AlertDialog initiateScan(Activity activity,
//                                         int stringTitle,
//                                         int stringMessage,
//                                         int stringButtonYes,
//                                         int stringButtonNo) {
//    return initiateScan(activity,
//                        activity.getString(stringTitle),
//                        activity.getString(stringMessage),
//                        activity.getString(stringButtonYes),
//                        activity.getString(stringButtonNo));
//  }
//
//  /**
//   * See {@link #initiateScan(Activity, CharSequence, CharSequence, CharSequence, CharSequence, CharSequence)} --
//   * same, but scans for all supported barcode types.
//   * @param stringTitle title of dialog prompting user to download Barcode Scanner
//   * @param stringMessage text of dialog prompting user to download Barcode Scanner
//   * @param stringButtonYes text of button user clicks when agreeing to download
//   *  Barcode Scanner (e.g. "Yes")
//   * @param stringButtonNo text of button user clicks when declining to download
//   *  Barcode Scanner (e.g. "No")
//   * @return an {@link AlertDialog} if the user was prompted to download the app,
//   *  null otherwise
//   */
//  public static AlertDialog initiateScan(Activity activity,
//                                         CharSequence stringTitle,
//                                         CharSequence stringMessage,
//                                         CharSequence stringButtonYes,
//                                         CharSequence stringButtonNo) {
//
//    return initiateScan(activity,
//                        stringTitle,
//                        stringMessage,
//                        stringButtonYes,
//                        stringButtonNo,
//                        ALL_CODE_TYPES);
//  }
//
//  /**
//   * Invokes scanning.
//   *
//   * @param stringTitle title of dialog prompting user to download Barcode Scanner
//   * @param stringMessage text of dialog prompting user to download Barcode Scanner
//   * @param stringButtonYes text of button user clicks when agreeing to download
//   *  Barcode Scanner (e.g. "Yes")
//   * @param stringButtonNo text of button user clicks when declining to download
//   *  Barcode Scanner (e.g. "No")
//   * @param stringDesiredBarcodeFormats a comma separated list of codes you would
//   *  like to scan for.
//   * @return an {@link AlertDialog} if the user was prompted to download the app,
//   *  null otherwise
//   * @throws InterruptedException if timeout expires before a scan completes
//   */
//  public static AlertDialog initiateScan(Activity activity,
//                                         CharSequence stringTitle,
//                                         CharSequence stringMessage,
//                                         CharSequence stringButtonYes,
//                                         CharSequence stringButtonNo,
//                                         CharSequence stringDesiredBarcodeFormats) {
//    Intent intentScan = new Intent(PACKAGE + ".SCAN");
//    setPackage(intentScan);
//    intentScan.addCategory(Intent.CATEGORY_DEFAULT);
//
//    // check which types of codes to scan for
//    if (stringDesiredBarcodeFormats != null) {
//      // set the desired barcode types
//      intentScan.putExtra("SCAN_FORMATS", stringDesiredBarcodeFormats);
//    }
//
//    try {
//      activity.startActivityForResult(intentScan, REQUEST_CODE);
//      return null;
//    } catch (ActivityNotFoundException e) {
//      return showDownloadDialog(activity, stringTitle, stringMessage, stringButtonYes, stringButtonNo);
//    }
//  }
//
//  private static AlertDialog showDownloadDialog(final Activity activity,
//                                                CharSequence stringTitle,
//                                                CharSequence stringMessage,
//                                                CharSequence stringButtonYes,
//                                                CharSequence stringButtonNo) {
//    AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
//    downloadDialog.setTitle(stringTitle);
//    downloadDialog.setMessage(stringMessage);
//    downloadDialog.setPositiveButton(stringButtonYes, new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialogInterface, int i) {
//        Uri uri = Uri.parse("market://search?q=pname:" + PACKAGE);
//        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//        activity.startActivity(intent);
//      }
//    });
//    downloadDialog.setNegativeButton(stringButtonNo, new DialogInterface.OnClickListener() {
//      public void onClick(DialogInterface dialogInterface, int i) {}
//    });
//    return downloadDialog.show();
//  }
//
//
//  /**
//   * <p>Call this from your {@link Activity}'s
//   * {@link Activity#onActivityResult(int, int, Intent)} method.</p>
//   *
//   * @return null if the event handled here was not related to this class, or
//   *  else an {@link IntentResult} containing the result of the scan. If the user cancelled scanning,
//   *  the fields will be null.
//   */
//  public static IntentResult parseActivityResult(int requestCode, int resultCode, Intent intent) {
//    if (requestCode == REQUEST_CODE) {
//      if (resultCode == Activity.RESULT_OK) {
//        String contents = intent.getStringExtra("SCAN_RESULT");
//        String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
//        return new IntentResult(contents, formatName);
//      } else {
//        return new IntentResult(null, null);
//      }
//    }
//    return null;
//  }
//
//  /**
//   * See {@link #shareText(Activity, CharSequence, CharSequence, CharSequence, CharSequence, CharSequence)} --
//   * same, but uses default English labels.
//   */
//  public static void shareText(Activity activity, CharSequence text) {
//    shareText(activity, text, DEFAULT_TITLE, DEFAULT_MESSAGE, DEFAULT_YES, DEFAULT_NO);
//  }
//
//  /**
//   * See {@link #shareText(Activity, CharSequence, CharSequence, CharSequence, CharSequence, CharSequence)} --
//   * same, but takes string IDs which refer to the {@link Activity}'s resource bundle entries.
//   */
//  public static void shareText(Activity activity,
//                               CharSequence text,
//                               int stringTitle,
//                               int stringMessage,
//                               int stringButtonYes,
//                               int stringButtonNo) {
//    shareText(activity,
//              text,
//              activity.getString(stringTitle),
//              activity.getString(stringMessage),
//              activity.getString(stringButtonYes),
//              activity.getString(stringButtonNo));
//  }
//
//  /**
//   * Shares the given text by encoding it as a barcode, such that another user can
//   * scan the text off the screen of the device.
//   *
//   * @param text the text string to encode as a barcode
//   * @param stringTitle title of dialog prompting user to download Barcode Scanner
//   * @param stringMessage text of dialog prompting user to download Barcode Scanner
//   * @param stringButtonYes text of button user clicks when agreeing to download
//   *  Barcode Scanner (e.g. "Yes")
//   * @param stringButtonNo text of button user clicks when declining to download
//   *  Barcode Scanner (e.g. "No")
//   */
//  public static void shareText(Activity activity,
//                               CharSequence text,
//                               CharSequence stringTitle,
//                               CharSequence stringMessage,
//                               CharSequence stringButtonYes,
//                               CharSequence stringButtonNo) {
//    Intent intent = new Intent();
//    intent.setAction(PACKAGE + ".ENCODE");
//    setPackage(intent);
//    intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
//    intent.putExtra("ENCODE_DATA", text);
//    try {
//      activity.startActivity(intent);
//    } catch (ActivityNotFoundException e) {
//      showDownloadDialog(activity, stringTitle, stringMessage, stringButtonYes, stringButtonNo);
//    }
//  }
//
//  private static void setPackage(Intent intent) {
//    if (PACKAGE_SETTER != null) {
//      try {
//        PACKAGE_SETTER.invoke(intent, PACKAGE);
//      } catch (InvocationTargetException ite) {
//        Log.w(TAG, ite.getTargetException());
//      } catch (IllegalAccessException iae) {
//        Log.w(TAG, iae);
//      }
//    }
//  }
//
//}
