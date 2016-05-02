/**
 * Copyright (c) 2009 Washington University
 */
package org.nrg.xnat.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Kevin A. Archie <karchie@npg.wustl.edu>
 *
 */
public final class StringUtils {
    private StringUtils() {}	// prevent instantiation

    /**
     * Appends the components to the StringBuilder, separated by the separator.
     * @param sb
     * @param separator 
     * @param components
     * @return sb
     */
    public static StringBuilder join(final StringBuilder sb, final String separator, final String...components) {
	return join(sb, separator, Arrays.asList(components));
    }

    /**
     * Appends the components to the StringBuilder, separated by the separator.
     * @param sb
     * @param separator
     * @param components
     * @return sb
     */
    public static StringBuilder join(final StringBuilder sb, final String separator, final Collection<?> components) {
	final Iterator<?> i = components.iterator();
	if (i.hasNext()) {
	    sb.append(i.next());
	    while (i.hasNext()) {
		sb.append(separator);
		sb.append(i.next());
	    }
	}
	return sb;
    }

    /**
     * Combines the components into a single string, separated by the separator.
     * @param separator
     * @param components
     * @return String consisting of the components separated by the separator
     */
    public static String join(final String separator, final String...components) {
	return join(separator, Arrays.asList(components));
    }

    /**
     * Combines the components into a single string, separated by the separator.
     * @param separator
     * @param components
     * @return String consisting of the components separated by the separator
     */
    public static String join(final String separator, final Collection<?> components) {
	return join(new StringBuilder(), separator, components).toString();
    }

    /**
     * Appends the components in the range [beginIndex, endIndex) to the given StringBuilder,
     * separated by the given separator.
     * @param sb
     * @param separator
     * @param beginIndex index of first component to be appended
     * @param endIndex 1 + index of last component to be appended
     * @param components
     * @return sb
     */
    public static StringBuilder join(final StringBuilder sb, final String separator,
	    final int beginIndex, final int endIndex, final String...components) {
	if (endIndex <= beginIndex) {
	    throw new IndexOutOfBoundsException("endIndex must be larger than beginIndex");
	}
	sb.append(components[beginIndex]);
	for (int i = beginIndex + 1; i < endIndex; i++) {
	    sb.append(separator);
	    sb.append(components[i]);
	}
	return sb;
    }

    /***
     * Combines the components in the range [beginIndex, endIndex) into a single string,
     * separated by the given separator.
     * @param separator
     * @param beginIndex
     * @param endIndex
     * @param components
     * @return String consisting of the selected components, separated by the separator.
     */
    public static String join(final String separator, final int beginIndex, final int endIndex, final String...components) {
	return join(new StringBuilder(), separator, beginIndex, endIndex, components).toString();
    }

    /**
     * Appends the components in the range [beginIndex, endIndex) to the given StringBuilder,
     * separated by the given separator.
     * @param sb
     * @param separator
     * @param beginIndex index of first component to be appended
     * @param endIndex 1 + index of last component to be appended
     * @param components
     * @return sb
     */
    public static StringBuilder join(final StringBuilder sb, final String separator,
	    final int beginIndex, final int endIndex, final Collection<?> components) {
	if (endIndex <= beginIndex) {
	    throw new IndexOutOfBoundsException("endIndex must be larger than beginIndex");
	}
	final Iterator<?> ci = components.iterator();
	for (int i = 0; i < beginIndex; i++) {
	    if (!ci.hasNext()) {
		throw new IndexOutOfBoundsException("beginIndex larger than collection length");
	    }
	    ci.next();	// ignore this component
	}
	if (!ci.hasNext()) {
	    throw new IndexOutOfBoundsException("beginIndex larger than collection length");
	}
	sb.append(ci.next());
	for (int i = beginIndex + 1; i < endIndex; i++) {
	    if (!ci.hasNext()) {
		throw new IndexOutOfBoundsException("endIndex larger than collection length");
	    }
	    sb.append(separator);
	    sb.append(ci.next());
	}
	return sb;
    }

    /***
     * Combines the components in the range [beginIndex, endIndex) into a single string,
     * separated by the given separator.
     * @param separator
     * @param beginIndex
     * @param endIndex
     * @param components
     * @return String consisting of the selected components, separated by the separator.
     */
    public static String join(final String separator, final int beginIndex, final int endIndex,
	    final Collection<?> components) {
	return join(new StringBuilder(), separator, beginIndex, endIndex, components).toString();
    }
    

    /**
     * Strips all trailing instances of the given character from the given string.
     * @param s
     * @param c
     * @return new String that looks like s, minus all the trailing c's.
     */
    public static String stripTrailingChar(final String s, final char c) {
	for (int i = s.length() - 1; i >= 0; i--) {
	    if (c != s.charAt(i)) {
		return s.substring(0, i+1);
	    }
	}
	return "";
    }

    
    private static boolean isIn(final char c, final char[] cs) {
	for (int i = 0; i < cs.length; i++) {
	    if (c == cs[i]) return true;
	}
	return false;
    }
    
    
    /**
     * Strips all trailing instances of the given characters from the given string.
     * @param s
     * @param chars
     * @return new String that looks like s, minus all the trailing c's.
     */
    public static String stripTrailingChars(String s, final char...chars) {
	for (int i = s.length() - 1; i >= 0; i--) {
	    if (!isIn(s.charAt(i), chars)) {
		return s.substring(0, i+1);
	    }
	}
	return "";
    }
}

