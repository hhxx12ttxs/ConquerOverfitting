package com.google.myjson.internal.bind;

import com.google.myjson.reflect.TypeToken;
import com.google.myjson.stream.JsonReader;
import com.google.myjson.stream.JsonWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

class n
  implements TypeAdapter.Factory
{
  n()
    throws IOException
  {
  }

  public <T> TypeAdapter<T> create(MiniGson paramMiniGson, TypeToken<T> paramTypeToken)
  {
    if (paramTypeToken.getRawType() != Timestamp.class)
      return null;
    return new TypeAdapter()
    {
      public Timestamp read(JsonReader paramAnonymousJsonReader)
        throws IOException
      {
        Date localDate = (Date)this.a.read(paramAnonymousJsonReader);
        if (localDate != null)
          return new Timestamp(localDate.getTime());
        return null;
      }

      public void write(JsonWriter paramAnonymousJsonWriter, Timestamp paramAnonymousTimestamp)
        throws IOException
      {
        this.a.write(paramAnonymousJsonWriter, paramAnonymousTimestamp);
      }
    };
  }
}

/* Location:           D:\Jervis\Documents\Programming\Research\Android\apks\logos.quiz.companies.game-59\classes_dex2jar_simplified.jar
 * Qualified Name:     com.google.myjson.internal.bind.n
 * JD-Core Version:    0.6.2
 */
