contexts.put(parts[0].toLowerCase(), vec);
}
}

public double Predict(String hPrev, String hCurr, String hNext, String mPrev, String mCurr, String mNext, int offset)
{
double []headp = words.get(hPrev + &quot;_-1&quot;);
double []heado = words.get(hCurr + &quot;_0&quot;);

