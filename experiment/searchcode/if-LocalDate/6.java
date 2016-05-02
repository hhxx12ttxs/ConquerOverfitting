package com.netease.pris.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import com.netease.framework.ActivityEx;
import com.netease.l.b.a;
import com.netease.pris.atom.PRISNotification.PrisMessage;
import java.util.Date;

public class SystemPushMessageActivity extends ActivityEx
{
  public static final String a = "extra_message";
  private PRISNotification.PrisMessage b = null;
  private TextView c = null;
  private TextView d = null;
  private WebView e = null;
  private Button f = null;
  private String g = null;
  private View.OnClickListener h = new au(this);

  public static Intent a(Context paramContext, Parcelable paramParcelable)
  {
    Intent localIntent = new Intent(paramContext, SystemPushMessageActivity.class);
    localIntent.putExtra("extra_message", paramParcelable);
    return localIntent;
  }

  public static void a(Context paramContext)
  {
    paramContext.startActivity(new Intent(paramContext, SystemPushMessageActivity.class));
  }

  private void b()
  {
    this.b = ((PRISNotification.PrisMessage)getIntent().getParcelableExtra("extra_message"));
    this.g = getString(2131361850);
    this.c = ((TextView)findViewById(2131558585));
    this.d = ((TextView)findViewById(2131558586));
    this.e = ((WebView)findViewById(2131558587));
    this.f = ((Button)findViewById(2131558588));
    this.f.setOnClickListener(this.h);
    setTitle(this.b.a());
    Date localDate = this.b.f();
    if (localDate != null)
    {
      this.c.setText(a.a(localDate, this.g));
      if (!this.b.e())
        break label193;
      this.d.setVisibility(0);
      this.d.setText(this.b.b());
    }
    while (true)
    {
      if (!TextUtils.isEmpty(this.b.d()))
        this.f.setVisibility(0);
      return;
      this.c.setVisibility(8);
      break;
      label193: this.e.setVisibility(0);
      this.e.loadDataWithBaseURL(null, this.b.b(), "text/html", "UTF-8", null);
    }
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(2130903097);
    b();
  }

  protected void onDestroy()
  {
    super.onDestroy();
    this.b = null;
    this.c = null;
    this.d = null;
    if (this.e != null)
    {
      this.e.destroy();
      this.e = null;
    }
    this.f = null;
    this.g = null;
  }
}

/* Location:           D:\android\hack\dex2jar-0.0.9.8\classes_dex2jar.jar
 * Qualified Name:     com.netease.pris.activity.SystemPushMessageActivity
 * JD-Core Version:    0.6.0
 */
