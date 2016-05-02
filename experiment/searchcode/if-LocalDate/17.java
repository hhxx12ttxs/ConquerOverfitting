package com.chase.sig.android.view;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import com.chase.sig.android.ChaseApplication;
import com.chase.sig.android.activity.QuickPayViewTodoListDetailActivity;
import com.chase.sig.android.domain.LabeledValue;
import com.chase.sig.android.domain.QuickPayAcceptMoneyTransaction;
import com.chase.sig.android.domain.QuickPayActivityItem;
import com.chase.sig.android.domain.QuickPayActivityType;
import com.chase.sig.android.domain.QuickPayDeclineTransaction;
import com.chase.sig.android.domain.QuickPayPendingTransaction;
import com.chase.sig.android.domain.QuickPayRecipient;
import com.chase.sig.android.domain.QuickPayTransaction;
import com.chase.sig.android.domain.h;
import com.chase.sig.android.domain.m;
import com.chase.sig.android.domain.o;
import com.chase.sig.android.service.quickpay.TodoListResponse;
import com.chase.sig.android.util.Dollar;
import com.chase.sig.android.util.s;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class aa extends LinearLayout
{
  private final View a;
  private LayoutInflater b;
  private final ChaseApplication c;

  private aa(Context paramContext, ChaseApplication paramChaseApplication)
  {
    super(paramContext, null);
    this.b = ((LayoutInflater)paramContext.getSystemService("layout_inflater"));
    setOrientation(0);
    setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
    this.a = this.b.inflate(2130903161, this, true);
    this.c = paramChaseApplication;
  }

  public aa(Context paramContext, QuickPayAcceptMoneyTransaction paramQuickPayAcceptMoneyTransaction, ChaseApplication paramChaseApplication)
  {
    this(paramContext, paramChaseApplication);
    if (paramQuickPayAcceptMoneyTransaction.y())
    {
      View[] arrayOfView2 = new View[8];
      arrayOfView2[0] = b(paramQuickPayAcceptMoneyTransaction, false);
      arrayOfView2[1] = a(paramQuickPayAcceptMoneyTransaction, true);
      arrayOfView2[2] = c(paramQuickPayAcceptMoneyTransaction);
      arrayOfView2[3] = d(paramQuickPayAcceptMoneyTransaction);
      arrayOfView2[4] = a("Pay to", paramQuickPayAcceptMoneyTransaction);
      arrayOfView2[5] = d(paramQuickPayAcceptMoneyTransaction);
      arrayOfView2[6] = e(paramQuickPayAcceptMoneyTransaction);
      if (paramQuickPayAcceptMoneyTransaction.n());
      for (View localView = a(paramQuickPayAcceptMoneyTransaction); ; localView = null)
      {
        arrayOfView2[7] = localView;
        a(arrayOfView2);
        return;
      }
    }
    View[] arrayOfView1 = new View[7];
    arrayOfView1[0] = a(paramQuickPayAcceptMoneyTransaction, false);
    arrayOfView1[1] = c(paramQuickPayAcceptMoneyTransaction);
    arrayOfView1[2] = d(paramQuickPayAcceptMoneyTransaction);
    arrayOfView1[3] = a("Pay to", paramQuickPayAcceptMoneyTransaction);
    arrayOfView1[4] = d(paramQuickPayAcceptMoneyTransaction);
    arrayOfView1[5] = e(paramQuickPayAcceptMoneyTransaction);
    arrayOfView1[6] = a(paramQuickPayAcceptMoneyTransaction);
    a(arrayOfView1);
  }

  public aa(Context paramContext, QuickPayActivityItem paramQuickPayActivityItem, boolean paramBoolean, ChaseApplication paramChaseApplication)
  {
    this(paramContext, paramChaseApplication);
    boolean bool5;
    boolean bool6;
    label121: y localy1;
    label144: y localy2;
    label160: y localy3;
    label197: View localView2;
    label244: boolean bool4;
    label312: boolean bool2;
    label423: View[] arrayOfView1;
    if (QuickPayActivityType.a.a(paramQuickPayActivityItem))
    {
      View[] arrayOfView4 = new View[10];
      arrayOfView4[0] = b(paramQuickPayActivityItem, paramBoolean);
      if (!paramBoolean)
      {
        bool5 = bool1;
        arrayOfView4[bool1] = a(paramQuickPayActivityItem, bool5);
        arrayOfView4[2] = c(paramQuickPayActivityItem);
        arrayOfView4[3] = a("Pay To", paramQuickPayActivityItem);
        if ((paramQuickPayActivityItem.x()) || (paramQuickPayActivityItem.s() == null) || (!s.o(paramQuickPayActivityItem.s().b())))
          break label601;
        if (s.j(paramQuickPayActivityItem.s().b()) == null)
          break label595;
        bool6 = bool1;
        if (!bool6)
          break label601;
        localy1 = a("Received On", s.j(paramQuickPayActivityItem.s().b()));
        arrayOfView4[4] = localy1;
        if (!paramQuickPayActivityItem.x())
          break label607;
        localy2 = null;
        arrayOfView4[5] = localy2;
        arrayOfView4[6] = d(paramQuickPayActivityItem);
        arrayOfView4[7] = a(paramQuickPayActivityItem);
        if (!paramBoolean)
          break label628;
        localy3 = e(paramQuickPayActivityItem);
        arrayOfView4[8] = localy3;
        if (!paramBoolean)
          break label634;
        QuickPayActivityItem[] arrayOfQuickPayActivityItem = new QuickPayActivityItem[bool1];
        arrayOfQuickPayActivityItem[0] = paramQuickPayActivityItem;
        localView2 = a((QuickPayAcceptMoneyTransaction)QuickPayViewTodoListDetailActivity.a(Arrays.asList(arrayOfQuickPayActivityItem), new QuickPayAcceptMoneyTransaction()));
        arrayOfView4[9] = localView2;
        a(arrayOfView4);
      }
    }
    else
    {
      if (QuickPayActivityType.d.a(paramQuickPayActivityItem))
      {
        View[] arrayOfView3 = new View[7];
        arrayOfView3[0] = b(paramQuickPayActivityItem, paramBoolean);
        arrayOfView3[bool1] = c(paramQuickPayActivityItem);
        arrayOfView3[2] = a(paramQuickPayActivityItem, paramBoolean);
        if (paramBoolean)
          break label640;
        bool4 = bool1;
        arrayOfView3[3] = a(paramQuickPayActivityItem, bool4);
        arrayOfView3[4] = b(paramQuickPayActivityItem);
        arrayOfView3[5] = d(paramQuickPayActivityItem);
        arrayOfView3[6] = a(paramQuickPayActivityItem);
        a(arrayOfView3);
      }
      if (QuickPayActivityType.b.a(paramQuickPayActivityItem))
      {
        View[] arrayOfView2 = new View[9];
        arrayOfView2[0] = b(paramQuickPayActivityItem, paramBoolean);
        arrayOfView2[bool1] = b(paramQuickPayActivityItem);
        arrayOfView2[2] = a(paramQuickPayActivityItem, paramBoolean);
        arrayOfView2[3] = a("Pay from", paramQuickPayActivityItem);
        if (paramBoolean)
          break label646;
        bool2 = bool1;
        arrayOfView2[4] = a(paramQuickPayActivityItem, bool2);
        arrayOfView2[5] = a(paramQuickPayActivityItem);
        boolean bool3 = paramQuickPayActivityItem.k();
        View localView1 = null;
        if (bool3)
          localView1 = this.b.inflate(2130903208, null);
        arrayOfView2[6] = localView1;
        arrayOfView2[7] = e(paramQuickPayActivityItem);
        arrayOfView2[8] = d(paramQuickPayActivityItem);
        a(arrayOfView2);
      }
      if (QuickPayActivityType.c.a(paramQuickPayActivityItem))
      {
        arrayOfView1 = new View[6];
        arrayOfView1[0] = b(paramQuickPayActivityItem, paramBoolean);
        arrayOfView1[bool1] = b(paramQuickPayActivityItem);
        arrayOfView1[2] = a(paramQuickPayActivityItem, paramBoolean);
        if (paramBoolean)
          break label652;
      }
    }
    while (true)
    {
      arrayOfView1[3] = a(paramQuickPayActivityItem, bool1);
      arrayOfView1[4] = b(paramQuickPayActivityItem);
      arrayOfView1[5] = d(paramQuickPayActivityItem);
      a(arrayOfView1);
      return;
      bool5 = false;
      break;
      label595: bool6 = false;
      break label121;
      label601: localy1 = null;
      break label144;
      label607: localy2 = a("Accepted On", s.j(paramQuickPayActivityItem.t().b()));
      break label160;
      label628: localy3 = null;
      break label197;
      label634: localView2 = null;
      break label244;
      label640: bool4 = false;
      break label312;
      label646: bool2 = false;
      break label423;
      label652: bool1 = false;
    }
  }

  public aa(Context paramContext, QuickPayDeclineTransaction paramQuickPayDeclineTransaction, boolean paramBoolean, ChaseApplication paramChaseApplication)
  {
    this(paramContext, paramChaseApplication);
    if (QuickPayActivityType.d.a(paramQuickPayDeclineTransaction.I()))
    {
      View[] arrayOfView2 = new View[5];
      arrayOfView2[0] = c(paramQuickPayDeclineTransaction);
      arrayOfView2[1] = a(paramQuickPayDeclineTransaction, false);
      arrayOfView2[2] = d(paramQuickPayDeclineTransaction);
      arrayOfView2[3] = d(paramQuickPayDeclineTransaction);
      arrayOfView2[4] = a(paramQuickPayDeclineTransaction, paramBoolean);
      a(arrayOfView2);
    }
    if (QuickPayActivityType.a.a(paramQuickPayDeclineTransaction.I()))
    {
      View[] arrayOfView1 = new View[5];
      arrayOfView1[0] = c(paramQuickPayDeclineTransaction);
      arrayOfView1[1] = a(paramQuickPayDeclineTransaction, false);
      arrayOfView1[2] = d(paramQuickPayDeclineTransaction);
      arrayOfView1[3] = d(paramQuickPayDeclineTransaction);
      arrayOfView1[4] = a(paramQuickPayDeclineTransaction, paramBoolean);
      a(arrayOfView1);
    }
  }

  public aa(Context paramContext, QuickPayPendingTransaction paramQuickPayPendingTransaction, ChaseApplication paramChaseApplication)
  {
    this(paramContext, paramChaseApplication);
    if (s.l(paramQuickPayPendingTransaction.Y()))
      paramQuickPayPendingTransaction.w(paramChaseApplication.b().c.t().a(paramQuickPayPendingTransaction.X()));
    if (!paramQuickPayPendingTransaction.N())
    {
      View[] arrayOfView2 = new View[7];
      arrayOfView2[0] = b(paramQuickPayPendingTransaction);
      if (paramQuickPayPendingTransaction.D());
      for (View localView = a(paramQuickPayPendingTransaction, false); ; localView = a(paramQuickPayPendingTransaction, true))
      {
        arrayOfView2[1] = localView;
        arrayOfView2[2] = a("Pay from", paramQuickPayPendingTransaction);
        arrayOfView2[3] = b(paramQuickPayPendingTransaction);
        arrayOfView2[4] = a(paramQuickPayPendingTransaction);
        arrayOfView2[5] = c(paramQuickPayPendingTransaction);
        arrayOfView2[6] = d(paramQuickPayPendingTransaction);
        a(arrayOfView2);
        return;
      }
    }
    View[] arrayOfView1 = new View[7];
    arrayOfView1[0] = b(paramQuickPayPendingTransaction);
    arrayOfView1[1] = a(paramQuickPayPendingTransaction, true);
    arrayOfView1[2] = a("Pay from", paramQuickPayPendingTransaction);
    arrayOfView1[3] = b(paramQuickPayPendingTransaction);
    arrayOfView1[4] = a(paramQuickPayPendingTransaction);
    arrayOfView1[5] = c(paramQuickPayPendingTransaction);
    arrayOfView1[6] = d(paramQuickPayPendingTransaction);
    a(arrayOfView1);
  }

  public aa(Context paramContext, QuickPayTransaction paramQuickPayTransaction, ChaseApplication paramChaseApplication)
  {
    this(paramContext, paramChaseApplication);
    boolean bool1 = paramQuickPayTransaction.y();
    if (s.l(paramQuickPayTransaction.Y()))
      paramQuickPayTransaction.w(paramChaseApplication.b().c.t().a(paramQuickPayTransaction.X()));
    View[] arrayOfView = new View[10];
    View localView1;
    View localView2;
    label111: View localView3;
    if (bool1)
    {
      localView1 = b(paramQuickPayTransaction, false);
      arrayOfView[0] = localView1;
      arrayOfView[1] = b(paramQuickPayTransaction);
      arrayOfView[2] = a("Pay from", paramQuickPayTransaction);
      arrayOfView[3] = a(paramQuickPayTransaction, true);
      if (!bool1)
        break label184;
      localView2 = a(paramQuickPayTransaction);
      arrayOfView[4] = localView2;
      localView3 = null;
      if (!bool1)
        break label190;
    }
    while (true)
    {
      arrayOfView[5] = localView3;
      arrayOfView[6] = c(paramQuickPayTransaction);
      arrayOfView[7] = b(paramQuickPayTransaction);
      arrayOfView[8] = a(paramQuickPayTransaction);
      arrayOfView[9] = d(paramQuickPayTransaction);
      a(arrayOfView);
      return;
      localView1 = null;
      break;
      label184: localView2 = null;
      break label111;
      label190: boolean bool2 = ChaseApplication.a().b().c.a();
      localView3 = null;
      if (bool2)
      {
        boolean bool3 = paramQuickPayTransaction.B();
        localView3 = null;
        if (!bool3)
        {
          boolean bool4 = paramQuickPayTransaction.D();
          localView3 = null;
          if (!bool4)
          {
            localView3 = this.b.inflate(2130903186, null);
            ((TextView)localView3.findViewById(2131296851)).setText(2131165710);
            localView3.findViewById(2131296850).setOnClickListener(new ad(this));
          }
        }
      }
    }
  }

  private View a(QuickPayAcceptMoneyTransaction paramQuickPayAcceptMoneyTransaction)
  {
    if ((paramQuickPayAcceptMoneyTransaction.m() == null) || (!paramQuickPayAcceptMoneyTransaction.k()))
      return null;
    View localView = this.b.inflate(2130903186, null);
    ((TextView)localView.findViewById(2131296851)).setText("Get cash at Chase ATM");
    ((TextView)localView.findViewById(2131296768)).setText("Bring your ATM, credit or debit card...");
    localView.findViewById(2131296768).setVisibility(0);
    localView.findViewById(2131296642).setVisibility(8);
    localView.findViewById(2131296725).setBackgroundResource(2130837713);
    localView.findViewById(2131296725).setOnClickListener(new ab(this, paramQuickPayAcceptMoneyTransaction));
    return localView;
  }

  private View a(QuickPayActivityItem paramQuickPayActivityItem)
  {
    if (paramQuickPayActivityItem.D() != null)
      return a("Reason for Decline", paramQuickPayActivityItem.D());
    return null;
  }

  private View a(QuickPayActivityItem paramQuickPayActivityItem, boolean paramBoolean)
  {
    if (paramQuickPayActivityItem.f())
    {
      if (paramBoolean)
        return a("Type", paramQuickPayActivityItem.w().a() + " - " + paramQuickPayActivityItem.w().b());
      return a("Invoice Number", paramQuickPayActivityItem.w().b());
    }
    return null;
  }

  private View a(QuickPayDeclineTransaction paramQuickPayDeclineTransaction, boolean paramBoolean)
  {
    if (paramBoolean)
      return a("Reason for Decline", paramQuickPayDeclineTransaction.m());
    this.b = ((LayoutInflater)getContext().getSystemService("layout_inflater"));
    return this.b.inflate(2130903171, null);
  }

  private View a(QuickPayTransaction paramQuickPayTransaction)
  {
    if ((!paramQuickPayTransaction.D()) || (paramQuickPayTransaction.H() < 0))
      return null;
    if (paramQuickPayTransaction.C())
      return a("Number of payments", "Unlimited");
    return a("Number of payments", Integer.toString(paramQuickPayTransaction.H()));
  }

  private View a(h paramh)
  {
    if (!ChaseApplication.a().b().c.a());
    while (!paramh.k())
      return null;
    View localView = this.b.inflate(2130903186, null);
    TextView localTextView1 = (TextView)localView.findViewById(2131296851);
    localTextView1.setText("ATM pick-up is Available", TextView.BufferType.SPANNABLE);
    ((Spannable)localTextView1.getText()).setSpan(new ForegroundColorSpan(getResources().getColor(2131099717)), 15, localTextView1.getText().length(), 18);
    TextView localTextView2 = (TextView)localView.findViewById(2131296768);
    localTextView2.setText("Give recipient this Sender Code: " + paramh.l(), TextView.BufferType.SPANNABLE);
    ((Spannable)localTextView2.getText()).setSpan(new ForegroundColorSpan(getResources().getColor(2131099652)), -1 + (localTextView2.getText().length() - paramh.l().length()), localTextView2.getText().length(), 18);
    localView.findViewById(2131296768).setVisibility(0);
    localView.findViewById(2131296725).setOnClickListener(new ac(this, paramh));
    return localView;
  }

  private View a(h paramh, boolean paramBoolean)
  {
    String str = paramh.h();
    if (paramh.f());
    for (y localy = a("Invoice Amount", new Dollar(paramh.d()).h()); ; localy = a("Amount", new Dollar(paramh.d()).h()))
    {
      if ((paramBoolean) && (s.o(paramh.h())))
        localy.a("(" + str + ")");
      return localy;
    }
  }

  private View a(String paramString, h paramh)
  {
    String str1 = paramh.c();
    if ((str1 == null) || (str1.length() <= 0));
    while (true)
    {
      return null;
      String[] arrayOfString;
      if (s.l(str1))
        arrayOfString = null;
      while (arrayOfString != null)
      {
        return new y(getContext(), paramString, arrayOfString[1], arrayOfString[0]);
        Matcher localMatcher = Pattern.compile("\\(\\.+\\d\\d\\d\\d\\)$").matcher(str1);
        if (localMatcher.find())
        {
          String str2 = localMatcher.group();
          arrayOfString = new String[] { str1.replace(str2, "").trim(), str2 };
        }
        else
        {
          arrayOfString = new String[] { str1, "" };
        }
      }
    }
  }

  private y a(String paramString1, String paramString2)
  {
    Context localContext = getContext();
    if ((paramString2 == null) || (paramString2.equals("")))
      paramString2 = "NA";
    return new y(localContext, paramString1, paramString2, null);
  }

  private void a(View[] paramArrayOfView)
  {
    ViewGroup localViewGroup = (ViewGroup)this.a.findViewById(2131296730);
    if (localViewGroup.getChildCount() > 0)
    {
      View localView2 = localViewGroup.getChildAt(-1 + localViewGroup.getChildCount());
      if ((localView2 instanceof y))
        ((y)localView2).a.findViewById(2131296642).setVisibility(0);
    }
    Object localObject = null;
    int i = paramArrayOfView.length;
    for (int j = 0; j < i; j++)
    {
      View localView1 = paramArrayOfView[j];
      if (localView1 != null)
      {
        localViewGroup.addView(localView1);
        localObject = localView1;
      }
    }
    if ((localObject != null) && ((localObject instanceof y)))
      ((y)localObject).a.findViewById(2131296642).setVisibility(8);
  }

  private View b(QuickPayTransaction paramQuickPayTransaction)
  {
    if ((!paramQuickPayTransaction.D()) || (paramQuickPayTransaction.H() < 0))
      return null;
    return a("Frequency", paramQuickPayTransaction.Y());
  }

  private View b(h paramh)
  {
    y localy = a("To", paramh.a());
    if (s.m(paramh.j().f()))
      localy.a(s.f(paramh.j().f()));
    if (s.m(paramh.b()))
      localy.a(paramh.b());
    return localy;
  }

  private View b(h paramh, boolean paramBoolean)
  {
    if (paramBoolean)
      return null;
    y localy = a("Transaction #", paramh.g());
    localy.setValueColor(getResources().getColor(2131099718));
    return localy;
  }

  private y b(QuickPayActivityItem paramQuickPayActivityItem)
  {
    return a("Due Date", s.j(paramQuickPayActivityItem.v().b()));
  }

  private View c(QuickPayTransaction paramQuickPayTransaction)
  {
    Date localDate = paramQuickPayTransaction.s();
    if (localDate != null)
    {
      Resources localResources = getResources();
      if (paramQuickPayTransaction.B())
        return a("Due date", s.a(localDate));
      if (paramQuickPayTransaction.V())
        return a(localResources.getString(2131165740), s.a(localDate));
      if (!paramQuickPayTransaction.D())
        return a(localResources.getString(2131165739), s.a(localDate));
      return a(localResources.getString(2131165741), s.a(localDate));
    }
    return a("Due date", s.a(paramQuickPayTransaction.E()));
  }

  private View c(h paramh)
  {
    y localy = a("From", paramh.a());
    if (s.o(paramh.b()))
      localy.a(paramh.b());
    return localy;
  }

  private View d(QuickPayTransaction paramQuickPayTransaction)
  {
    if (s.m(paramQuickPayTransaction.A()))
      return a("Invoice Number", paramQuickPayTransaction.A());
    return null;
  }

  private y d(h paramh)
  {
    String str = paramh.e();
    if (s.m(str));
    while (true)
    {
      return new y(getContext(), "Memo", str, null);
      str = "";
    }
  }

  private y e(h paramh)
  {
    if (paramh.i() == null)
      return a("Sent On", "");
    return a("Sent On", s.i(paramh.i()));
  }

  public final View getView()
  {
    return this.a;
  }
}

/* Location:           D:\code\Research\Android\apks\com.chase.sig.android-14\com.chase.sig.android-14_dex2jar.jar
 * Qualified Name:     com.chase.sig.android.view.aa
 * JD-Core Version:    0.6.2
 */
