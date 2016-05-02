package com.flixster.android.view.gtv;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.flixster.android.utils.DateTimeHelper;
import java.util.Date;
import net.flixster.android.FlixsterApplication;
import net.flixster.android.model.LockerRight;
import net.flixster.android.model.Movie;

public class MovieGalleryItem extends RelativeLayout
{
  private final Context context;
  private ImageView poster;
  private TextView title;
  private ImageView tomatometerIcon;
  private TextView tomatometerScore;

  public MovieGalleryItem(Context paramContext)
  {
    super(paramContext);
    this.context = paramContext;
    initialize();
  }

  public MovieGalleryItem(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    this.context = paramContext;
    initialize();
  }

  public MovieGalleryItem(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    this.context = paramContext;
    initialize();
  }

  private void initialize()
  {
    View.inflate(this.context, 2130903126, this);
    this.poster = ((ImageView)findViewById(2131165493));
    this.title = ((TextView)findViewById(2131165494));
    this.tomatometerIcon = ((ImageView)findViewById(2131165479));
    this.tomatometerScore = ((TextView)findViewById(2131165480));
  }

  private void loadCaption(Movie paramMovie)
  {
    this.tomatometerScore.setVisibility(0);
    this.tomatometerIcon.setVisibility(0);
    if (paramMovie.isUpcoming)
    {
      Date localDate = paramMovie.getTheaterReleaseDate();
      if (localDate != null)
      {
        int j = localDate.getYear();
        int k = FlixsterApplication.sToday.getYear();
        int m = 0;
        if (j > k)
          m = 1;
        TextView localTextView = this.tomatometerScore;
        if (m != 0);
        for (String str = DateTimeHelper.formatMonthDayYear(localDate); ; str = DateTimeHelper.formatMonthDay(localDate))
        {
          localTextView.setText(str);
          this.tomatometerIcon.setVisibility(8);
          return;
        }
      }
      this.tomatometerScore.setVisibility(8);
      this.tomatometerIcon.setVisibility(8);
      return;
    }
    if (paramMovie.getTomatometer() > -1)
    {
      this.tomatometerScore.setText(paramMovie.getTomatometer() + "%");
      ImageView localImageView = this.tomatometerIcon;
      if (paramMovie.isFresh());
      for (int i = 2130837725; ; i = 2130837740)
      {
        localImageView.setImageResource(i);
        return;
      }
    }
    this.tomatometerScore.setVisibility(8);
    this.tomatometerIcon.setVisibility(8);
  }

  protected void load(LockerRight paramLockerRight)
  {
    this.title.setText(paramLockerRight.getTitle());
    if (paramLockerRight.getDetailPoster() == null)
      this.poster.setImageResource(2130837839);
    while (true)
    {
      loadCaption(paramLockerRight.getAsset());
      return;
      Bitmap localBitmap = paramLockerRight.getDetailBitmap(this.poster);
      if (localBitmap != null)
        this.poster.setImageBitmap(localBitmap);
    }
  }

  protected void load(Movie paramMovie)
  {
    this.title.setText(paramMovie.getTitle());
    if (paramMovie.getDetailPoster() == null)
      this.poster.setImageResource(2130837839);
    while (true)
    {
      loadCaption(paramMovie);
      return;
      Bitmap localBitmap = paramMovie.getDetailBitmap(this.poster);
      if (localBitmap != null)
        this.poster.setImageBitmap(localBitmap);
    }
  }

  protected void reset()
  {
    this.poster.setImageResource(2130837844);
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\net.flixster.android-5000461\classes_dex2jar.jar
 * Qualified Name:     com.flixster.android.view.gtv.MovieGalleryItem
 * JD-Core Version:    0.6.2
 */
