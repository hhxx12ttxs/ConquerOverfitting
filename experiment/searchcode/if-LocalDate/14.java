package com.chase.sig.android.view;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chase.sig.android.util.l;
import com.chase.sig.android.util.s;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public final class b extends Dialog
  implements View.OnFocusChangeListener
{
  private static String e;
  private Calendar a = Calendar.getInstance();
  private Calendar b;
  private final TextView c;
  private final TextView d;
  private a f;
  private SimpleDateFormat g = new SimpleDateFormat("MMM dd, yyyy");
  private Calendar h;
  private Date i;
  private boolean j;
  private boolean k;
  private Boolean l;
  private Calendar m;
  private Calendar n;

  public b(Context paramContext, a parama, boolean paramBoolean1, boolean paramBoolean2, Calendar paramCalendar1, Calendar paramCalendar2)
  {
    this(paramContext, parama, paramBoolean1, paramBoolean2, paramCalendar1, paramCalendar2, null);
  }

  public b(Context paramContext, a parama, boolean paramBoolean1, boolean paramBoolean2, Calendar paramCalendar1, Calendar paramCalendar2, Calendar paramCalendar3)
  {
    super(paramContext);
    this.b = paramCalendar2;
    this.j = paramBoolean1;
    this.k = paramBoolean2;
    this.i = paramCalendar1.getTime();
    this.n = paramCalendar3;
    this.f = parama;
    this.l = Boolean.valueOf(false);
    requestWindowFeature(1);
    setContentView(2130903065);
    e = paramContext.getResources().getString(2131165844);
    this.d = ((TextView)findViewById(2131296357));
    this.c = ((TextView)findViewById(2131296352));
    this.h = Calendar.getInstance();
    this.h.set(5, 1);
    this.m = paramCalendar1;
    b(this.m.getTime());
    findViewById(2131296351).setOnClickListener(new c(this));
    findViewById(2131296353).setOnClickListener(new d(this));
    setOnKeyListener(new e(this));
  }

  private <T extends View> List<T> a(Class<T> paramClass, ViewGroup paramViewGroup)
  {
    ArrayList localArrayList = new ArrayList();
    for (int i1 = 0; i1 < paramViewGroup.getChildCount(); i1++)
    {
      View localView = paramViewGroup.getChildAt(i1);
      if (localView.getClass().equals(paramClass))
        localArrayList.add(localView);
      if ((localView instanceof ViewGroup))
        localArrayList.addAll(a(paramClass, (ViewGroup)localView));
    }
    return localArrayList;
  }

  private boolean a(Calendar paramCalendar)
  {
    if (((this.m != null) && (paramCalendar.before(this.m))) || (paramCalendar.after(this.b)));
    do
    {
      int i1;
      do
      {
        return false;
        if (!this.k)
          break;
        i1 = paramCalendar.get(7);
      }
      while ((i1 == 7) || (i1 == 1));
    }
    while ((this.j) && (l.a(paramCalendar)));
    return true;
  }

  private static Date b(TextView paramTextView)
  {
    if (paramTextView.getTag() == null)
      return null;
    return (Date)paramTextView.getTag();
  }

  private void b()
  {
    Calendar localCalendar = Calendar.getInstance();
    localCalendar.set(2, this.h.get(2));
    localCalendar.set(5, 1);
    localCalendar.set(1, this.h.get(1));
    localCalendar.add(5, -((-1 + (7 + localCalendar.get(7))) % 7));
    Iterator localIterator = a(TextView.class, (ViewGroup)findViewById(2131296355)).iterator();
    while (localIterator.hasNext())
    {
      TextView localTextView = (TextView)localIterator.next();
      localTextView.setText(Integer.valueOf(localCalendar.get(5)).toString());
      localTextView.setTag(localCalendar.getTime());
      localTextView.setFocusable(a(localCalendar));
      c(localTextView);
      localCalendar.add(5, 1);
    }
  }

  private void b(Date paramDate)
  {
    this.d.setText(e + " " + s.a(paramDate));
  }

  private String c(Date paramDate)
  {
    Calendar localCalendar = Calendar.getInstance();
    localCalendar.setTime(paramDate);
    if (!a(localCalendar))
      return "BLACK_OUT";
    if ((this.i != null) && (l.a(this.i, localCalendar)))
      return "LAST_SELECTED";
    if ((this.n != null) && (l.a(this.n, localCalendar)))
      return "DUE_DATE";
    if (this.h.get(2) != localCalendar.get(2))
      return "OTHER_MONTH";
    return "AVAILABLE";
  }

  private void c(TextView paramTextView)
  {
    Date localDate = b(paramTextView);
    if (localDate == null);
    String str;
    do
    {
      return;
      str = c(localDate);
      paramTextView.setTextColor(getContext().getResources().getColor(2131099703));
      if (str.equals("BLACK_OUT"))
        paramTextView.setBackgroundResource(2130837608);
      if (str.equals("AVAILABLE"))
        paramTextView.setBackgroundResource(2130837606);
      if (str.equals("OTHER_MONTH"))
        paramTextView.setBackgroundResource(2130837605);
      if (str.equals("LAST_SELECTED"))
        paramTextView.setBackgroundResource(2130837615);
    }
    while (!str.equals("DUE_DATE"));
    paramTextView.setTextColor(getContext().getResources().getColor(2131099716));
    paramTextView.setBackgroundResource(2130837610);
  }

  private void d(Date paramDate)
  {
    String str = new SimpleDateFormat("MMMM yyyy").format(paramDate);
    this.c.setText(str);
  }

  public final b a()
  {
    findViewById(2131296356).setVisibility(8);
    this.d.setVisibility(8);
    return this;
  }

  public final b a(Date paramDate)
  {
    if (paramDate == null)
      paramDate = this.m.getTime();
    this.i = paramDate;
    b(this.i);
    return this;
  }

  public final void a(TextView paramTextView)
  {
    this.i = ((Date)paramTextView.getTag());
    this.d.setText(e + " " + s.a(this.i));
    this.f.a(this.i);
    dismiss();
  }

  protected final void onCreate(Bundle paramBundle)
  {
    if ((paramBundle != null) && (paramBundle.containsKey("LAST_DISPLAYED")))
    {
      this.h = ((Calendar)paramBundle.getSerializable("LAST_DISPLAYED"));
      this.l = Boolean.valueOf(true);
    }
    super.onCreate(paramBundle);
  }

  public final void onFocusChange(View paramView, boolean paramBoolean)
  {
    Date localDate = (Date)paramView.getTag();
    StringBuffer localStringBuffer = new StringBuffer(e);
    localStringBuffer.append(" ");
    localStringBuffer.append(this.g.format(localDate));
    this.d.setText(localStringBuffer.toString());
    d(localDate);
  }

  public final Bundle onSaveInstanceState()
  {
    Bundle localBundle = super.onSaveInstanceState();
    localBundle.putSerializable("LAST_DISPLAYED", this.h);
    return localBundle;
  }

  public final boolean onTouchEvent(MotionEvent paramMotionEvent)
  {
    List localList = a(TextView.class, (ViewGroup)findViewById(2131296355));
    Object localObject1 = null;
    Iterator localIterator = localList.iterator();
    Object localObject2;
    if (localIterator.hasNext())
    {
      localObject2 = (TextView)localIterator.next();
      Rect localRect = new Rect();
      ((TextView)localObject2).getGlobalVisibleRect(localRect);
      c((TextView)localObject2);
      if (!localRect.contains((int)paramMotionEvent.getX(), (int)paramMotionEvent.getY()))
        break label140;
    }
    while (true)
    {
      localObject1 = localObject2;
      break;
      if ((localObject1 == null) || (c(b(localObject1)).equals("BLACK_OUT")));
      do
      {
        return true;
        localObject1.setBackgroundResource(2131099706);
      }
      while (paramMotionEvent.getAction() != 1);
      a(localObject1);
      return true;
      label140: localObject2 = localObject1;
    }
  }

  public final void show()
  {
    if (this.l.booleanValue())
      this.l = Boolean.valueOf(false);
    while (true)
    {
      d(this.h.getTime());
      b();
      super.show();
      return;
      this.h.set(1, 1900 + this.i.getYear());
      this.h.set(2, this.i.getMonth());
    }
  }

  public static abstract interface a
  {
    public abstract void a(Date paramDate);
  }
}

/* Location:           D:\code\Research\Android\apks\com.chase.sig.android-14\com.chase.sig.android-14_dex2jar.jar
 * Qualified Name:     com.chase.sig.android.view.b
 * JD-Core Version:    0.6.2
 */
