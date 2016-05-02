/*
 * Copyright (c) 2007, CustomWare Asia Pacific
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of "CustomWare Asia Pacific" nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.randombits.supplier.core.general;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.randombits.supplier.core.SupplierContext;
import org.randombits.supplier.core.SupplierException;
import org.randombits.supplier.core.annotate.AnnotatedSupplier;
import org.randombits.supplier.core.annotate.KeyContext;
import org.randombits.supplier.core.annotate.KeyParam;
import org.randombits.supplier.core.annotate.KeyValue;
import org.randombits.supplier.core.annotate.SupplierKey;
import org.randombits.supplier.core.annotate.SupplierPrefix;
import org.randombits.supplier.core.annotate.SupportedTypes;
import org.randombits.utils.lang.API;

/**
 * Supplies some extra ways of manipulating text (aka String) values.
 *
 * @author David Peterson
 */
@SupplierPrefix("text")
@SupportedTypes(String.class)
@API("1.0.0")
public class TextSupplier extends AnnotatedSupplier {

    @SupplierKey({"length", "size"})
    @API("1.0.0")
    public int getLength(@KeyValue String text) {
        return text.length();
    }

    @SupplierKey("xml escape")
    @API("1.0.0")
    public String xmlEscape(@KeyValue String text) {
        return StringEscapeUtils.escapeXml(text);
    }

    @SupplierKey("html escape")
    @API("1.0.0")
    public String htmlEscape(@KeyValue String text) {
        return StringEscapeUtils.escapeHtml(text);
    }

    @SupplierKey("url encode")
    @API("1.0.0")
    public String urlEncode(@KeyValue String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    @SupplierKey("split with {delimiter}")
    @API("1.0.0")
    public String[] splitWith(@KeyValue String text, @KeyParam("delimiter") String delimiter) {
        return text.split(delimiter);
    }

    @SupplierKey("is blank")
    @API("1.0.0")
    public boolean isBlank(@KeyValue String text) {
        return text.trim().length() == 0;
    }

    @SupplierKey("is empty")
    @API("1.0.0")
    public boolean isEmpty(@KeyValue String text) {
        return text.length() == 0;
    }

    @SupplierKey("ends with {ending}")
    @API("1.0.0")
    public boolean endsWith(@KeyValue String text, @KeyParam("ending") String ending) {
        return text.endsWith(ending);
    }

    @SupplierKey("starts with {beginning}")
    @API("1.0.0")
    public boolean startsWith(@KeyValue String text, @KeyParam("beginning") String beginning) {
        return text.startsWith(beginning);
    }

    @SupplierKey("equals {text}")
    @API("1.0.0")
    public boolean equals(@KeyValue String text, @KeyParam("text") String otherText) {
        return text.equals(otherText);
    }

    @SupplierKey("trim")
    @API("1.0.0")
    public String trim(@KeyValue String text) {
        return text.trim();
    }

    @SupplierKey({"upper case", "uppercase"})
    @API("1.0.0")
    public String toUpperCase(@KeyValue String text) {
        return text.toUpperCase();
    }

    @SupplierKey({"lower case", "lowercase"})
    @API("1.0.0")
    public String toLowerCase(@KeyValue String text) {
        return text.toLowerCase();
    }

    @SupplierKey("as date {pattern}")
    @API("1.0.0")
    public Date asDate(@KeyContext SupplierContext context, @KeyValue String text, @KeyParam("pattern") String pattern) throws SupplierException {
        try {
            return DateSupplier.parseDate(context, text, pattern);
        } catch (ParseException e) {
            return null;
        } catch (IllegalArgumentException e) {
            throw new SupplierException("Unsupported format: " + pattern);
        }
    }

    @SupplierKey({"as number", "as number {pattern}"})
    @API("1.0.0")
    public Number asNumber(@KeyContext SupplierContext context, @KeyValue String text, @KeyParam("pattern") String pattern) {
        try {
            return NumberSupplier.parseNumber(text, pattern);
        } catch (ParseException e) {
            return null;
        }
    }

    @SupplierKey("find {pattern}")
    @API("1.0.0")
    public Iterator<Match> find(@KeyValue String value, @KeyParam("pattern") String pattern) {
        Pattern p = Pattern.compile(pattern);
        return new MatchIterator(p.matcher(value));
    }

    @SupplierKey("match {pattern}")
    @API("1.0.0")
    public Match match(@KeyValue String value, @KeyParam("pattern") String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(value);
        return new Match(matcher);
    }

    @SupplierKey("last {count}")
    @API("1.0.0")
    public String getLast(@KeyValue String text, @KeyParam("count") String count) throws SupplierException {
        int countInt = parseInteger(count);
        text = stripRichTextRenderCha( stripHtmlTags( stripXhtmlTags( text ) ) );
        return (countInt < text.length()) ? text.substring(text.length() - countInt) : text;
    }

    @SupplierKey("before last {count}")
    @API("1.0.0")
    public String getBeforeLast(@KeyValue String text, @KeyParam("count") String count) throws SupplierException {
        int countInt = parseInteger(count);
        text = stripRichTextRenderCha( stripHtmlTags( stripXhtmlTags( text ) ) );
        return (countInt < text.length()) ? text.substring(0, text.length() - countInt) : "";
    }

    @SupplierKey("first {count}")
    @API("1.0.0")
    public String getFirst(@KeyValue String text, @KeyParam("count") String count) throws SupplierException {
        int countInt = parseInteger(count);
        text = stripRichTextRenderCha( stripHtmlTags( stripXhtmlTags( text ) ) );        
        return (countInt < text.length()) ? text.substring(0, countInt) : text;
    }

    @SupplierKey("after first {count}")
    @API("1.0.0")
    public String afterFirst(@KeyValue String text, @KeyParam("count") String count) throws SupplierException {
        int countInt = parseInteger(count);
        text = stripRichTextRenderCha( stripHtmlTags( stripXhtmlTags( text ) ) );
        return (countInt < text.length()) ? text.substring(countInt) : "";
    }

    private int parseInteger(String count) throws SupplierException {
        int countInt;
        try {
            countInt = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            throw new SupplierException("Invalid number: " + count);
        }
        return countInt;
    }
    
    private static final Pattern AC_TAG_PATTERN = Pattern.compile(
            "(<ac:.+?>|</ac:.+?>)", Pattern.DOTALL );
    
    private String stripXhtmlTags( String content ) {
    	return AC_TAG_PATTERN.matcher( content ).replaceAll( "" );   	
    }
    
    private String stripHtmlTags( String content ) {
    	if ( content != null ) 
    		return Jsoup.parse( content ).text() ;
    	
    	return null;
    }
    
    
    // TODO : to be checked 
    private String stripRichTextRenderCha( String content ) {
    	if ( content != null) 
    		return content.replaceAll("[&>_]", "");
    	
    	return null;
    }
}

