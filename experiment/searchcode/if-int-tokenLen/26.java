package com.ybi.whoot.utils;

import java.util.HashMap;
import java.util.Random;

import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

public class Utils
{
	private final static HashMap<String, String> hmLeet = new HashMap<String, String>()
	{
		private static final long serialVersionUID = -4774084030754406983L;

		{
			put("e", "3");
			put("l", "1");
			put("s", "$");
			put("t", "7");
			put("z", "2");
			put("c", "<");
			put("a", "4");
			put("i", "|");
			put("o", "0");
			put("b", "8");
		}
	};

	public static CharSequence texteAndValue(CharSequence text, String libelle)
	{
		String textCut;
		if (((text.toString())).indexOf("~") > 0)
		{
			textCut = ((text.toString())).substring(0, ((text.toString())).indexOf("~") - 1);
		} else
		{
			textCut = text.toString();
		}
		String out = textCut + " ~ $$(" + libelle + ")$$";
		CharSequence alpha = Utils.setSpanBetweenTokens(out, "$$", new ForegroundColorSpan(0xFF666666));
		return alpha;
	}

	public static CharSequence texteAndColor(CharSequence text, int color)
	{
		String textCut;
		if (((text.toString())).indexOf("~") > 0)
		{
			textCut = ((text.toString())).substring(0, ((text.toString())).indexOf("~") - 1);
		} else
		{
			textCut = text.toString();
		}
		String out = textCut + " ~ $$()$$";
		CharSequence alpha = Utils.setSpanBetweenTokens(out, "$$", new ForegroundColorSpan(color));
		return alpha;
	}

	/**
	 * Given either a Spannable String or a regular String and a token, apply
	 * the given CharacterStyle to the span between the tokens, and also remove
	 * tokens.
	 * <p>
	 * For example, {@code setSpanBetweenTokens("Hello ##world##!", "##",
	 * new ForegroundColorSpan(0xFFFF0000));} will return a CharSequence
	 * {@code "Hello world!"} with {@code world} in red.
	 * 
	 * @param text
	 *            The text, with the tokens, to adjust.
	 * @param token
	 *            The token string; there should be at least two instances of
	 *            token in text.
	 * @param cs
	 *            The style to apply to the CharSequence. WARNING: You cannot
	 *            send the same two instances of this parameter, otherwise the
	 *            second call will remove the original span.
	 * @return A Spannable CharSequence with the new style applied.
	 * 
	 * @see http
	 *      ://developer.android.com/reference/android/text/style/CharacterStyle
	 *      .html
	 */
	public static CharSequence setSpanBetweenTokens(CharSequence text, String token, CharacterStyle... cs)
	{
		// Start and end refer to the points where the span will apply
		int tokenLen = token.length();
		int start = text.toString().indexOf(token) + tokenLen;
		int end = text.toString().indexOf(token, start);

		if (start > -1 && end > -1)
		{
			// Copy the spannable string to a mutable spannable string
			SpannableStringBuilder ssb = new SpannableStringBuilder(text);
			for (CharacterStyle c : cs)
				ssb.setSpan(c, start, end, 0);

			// Delete the tokens before and after the span
			ssb.delete(end, end + tokenLen);
			ssb.delete(start - tokenLen, start);

			text = ssb;
		}

		return text;
	}

	public static String getChaotique(String s)
	{
		Random generator = new Random();

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length(); i++)
		{
			if (generator.nextInt(2) == 0)
				sb.append((Character.toString(s.toLowerCase().charAt(i))));
			else
				sb.append((Character.toString(s.toUpperCase().charAt(i))));
		}
		return sb.toString();
	}

	public static String getMinuscle(String s)
	{
		return s.toLowerCase();
	}

	public static String getMajuscule(String s)
	{
		return s.toUpperCase();
	}

	public static String getHaxor(String s)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length(); i++)
		{
			sb.append(hmLeet.get(Character.toString(s.toLowerCase().charAt(i))) != null ? hmLeet.get(Character.toString(s.toLowerCase().charAt(i)))
					: Character.toString(s.toLowerCase().charAt(i)));
		}
		return sb.toString();

	}

}

