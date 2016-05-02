// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.nnee.p_c;

import java.text.*;
import java.util.*;

public class cls_d extends DateFormat
{

    public cls_d()
    {
    }

    public static DateFormat a()
    {
        if(a == null)
            a = new cls_d();
        return a;
    }

    public StringBuffer format(Date date, StringBuffer stringbuffer, FieldPosition fieldposition)
    {
        String s = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")).format(date);
        return stringbuffer.append(s.substring(0, -2 + s.length())).append(':').append(s.substring(-2 + s.length()));
    }

    public Date parse(String s, ParsePosition parseposition)
    {
    	int j6;
        int k6;
        int l6;
        int i7;
        int j7;
        int k7;
        int l7;
        int i8;
        
    	int l3;
        int i6;
        
        int i4;
        int j4;
        int k4;
        int l4;
        int i5;
        int j5;
        int k5;
        int l5;
        
    	int j8;
        int k8;
        int i9;
        int j9;
        int k9;
        
        Object obj;
        char c1;
        byte byte0;
        int l8;
        int l9;
        int i10;
        int j10;
        char c2;
        byte byte1;
        int k10;
        int l10;
        int i11;
        int j11;
        char c3;
        byte byte2;
        int k11;
        int l11;
        int i12;
        int j12;
        int k12;
        int l12;
        int i13;
        int j13;
        int k13;
        int l13;
        int i14;
        
        Calendar calendar;
        int j;
        int k;
        int l;
        int i1;
        int j1;
        int k1;
        int l1;
        int i2;
        int j2;
        int k2;
        int l2;
        int i3;
        int j3;
        int k3;
        int i = parseposition.getIndex();
        calendar = Calendar.getInstance();
        calendar.clear();
        j = s.length() - i;
        if(j != 4 && j != 7 && j != 10 && j != 17 && j != 20 && j < 22)
            throw new IllegalArgumentException("");
        k = 1;
        l = 4;
        i1 = 0;
        j1 = 0;
        k1 = 0;
        l1 = 0;
        i2 = 0;
        j2 = 0;
        k2 = 0;
        l2 = 1;
        i3 = 1;
        j3 = 1900;
        k3 = 0;
while(true)
{
        if(i1 >= j) {
        	l3 = i1;
            i4 = k1;
            j4 = i2;
            k4 = k2;
            l4 = j2;
            i5 = l1;
            j5 = i3;
            k5 = l2;
            l5 = j3;
            
            
            parseposition.setIndex(l3);
            StringBuilder stringbuilder = (new StringBuilder()).append("GMT+").append(i5).append(":");
            
            if(i4 < 10)
                obj = (new StringBuilder()).append("0").append(i4).toString();
            else
                obj = Integer.valueOf(i4);
            calendar.setTimeZone(TimeZone.getTimeZone(stringbuilder.append(obj).toString()));
            calendar.set(1, l5);
            calendar.set(2, j5 + -1);
            calendar.set(5, k5);
            calendar.set(11, k4);
            calendar.set(12, l4);
            calendar.set(13, j4);
            return calendar.getTime();
        } else 
        {
        	l3 = i1 + 1;
            char c = s.charAt(i1);
            if(!Character.isDigit(c))
                throw new IllegalArgumentException(Integer.toString(l3 + -1));
            i6 = k3 * 10 + (c + -48);
            if(l3 != l) 
            {
            	k3 = i6;
                i1 = l3;
                continue;
            } else {
            	switch(j1)
            	{
            	
            	case 0:
            		k6 = i3;
                    j6 = i6;
                    l6 = l2;
                    i7 = k2;
                    j7 = j2;
                    k7 = i2;
                    l7 = l1;
                    i8 = k1;
                    
                     
            		break;
            	case 1:
            		
            		 l6 = l2;
            	        j6 = j3;
            	        i7 = k2;
            	        k6 = i6;
            	        j7 = j2;
            	        k7 = i2;
            	        l7 = l1;
            	        i8 = k1;
            		break;
            	case 2:
            		i7 = k2;
                    j6 = j3;
                    j7 = j2;
                    k6 = i3;
                    l6 = i6;
                    k7 = i2;
                    l7 = l1;
                    i8 = k1;
            		break;
            	case 3:
            		j7 = j2;
                    j6 = j3;
                    k7 = i2;
                    k6 = i3;
                    l6 = l2;
                    l7 = l1;
                    i8 = k1;
                    i7 = i6;
            		break;
            	case 4:
            		k7 = i2;
                    j6 = j3;
                    k6 = i3;
                    l7 = l1;
                    l6 = l2;
                    i8 = k1;
                    i7 = k2;
                    j7 = i6;
            		break;
            	case 5:
            		l7 = l1;
                    j6 = j3;
                    k6 = i3;
                    i8 = k1;
                    l6 = l2;
                    i7 = k2;
                    j7 = j2;
                    k7 = i6;
            		break;
            	case 6:
            		j6 = j3;
                    k6 = i3;
                    l6 = l2;
                    i7 = k2;
                    j7 = j2;
                    k7 = i2;
                    l7 = l1;
                    i8 = k1;
            		break;
            	case 7:
            		i14 = k * i6;
                    j6 = j3;
                    k6 = i3;
                    l6 = l2;
                    i7 = k2;
                    j7 = j2;
                    k7 = i2;
                    l7 = i14;
                    i8 = k1;
            		break;
            	case 8:
            		
            		j6 = j3;
                    k6 = i3;
                    l6 = l2;
                    i7 = k2;
                    j7 = j2;
                    k7 = i2;
                    l7 = l1;
                    i8 = i6;
            		break;
            	default:
            		j6 = j3;
                    k6 = i3;
                    l6 = l2;
                    i7 = k2;
                    j7 = j2;
                    k7 = i2;
                    l7 = l1;
                    i8 = k1;
                    
                    
            		break;
            	}
            	
            	if(l3 != j) {
            		switch(j1)
            		{
            			
            			case 0:
            			case 1:
            				j13 = l3 + 1;
            		        if(s.charAt(l3) != '-')
            		        {
            		            j9 = k;
            		            l13 = l;
            		            k9 = j1;
            		            i9 = j13;
            		            k8 = l13;
            		        } else
            		        {
            		            k13 = j13 + 2;
            		            j9 = k;
            		            k9 = j1;
            		            i9 = j13;
            		            k8 = k13;
            		        }
            				break;
            			case 2:
            				k12 = l3 + 1;
            		        if(s.charAt(l3) != 'T')
            		        {
            		            j9 = k;
            		            i13 = l;
            		            k9 = j1;
            		            i9 = k12;
            		            k8 = i13;
            		        } else
            		        {
            		            l12 = k12 + 2;
            		            j9 = k;
            		            k9 = j1;
            		            i9 = k12;
            		            k8 = l12;
            		        }
            				break;
            			case 3:
            			case 7:
            				l11 = l3 + 1;
            		        if(s.charAt(l3) != ':')
            		        {
            		            j9 = k;
            		            j12 = l;
            		            k9 = j1;
            		            i9 = l11;
            		            k8 = j12;
            		        } else
            		        {
            		            i12 = l11 + 2;
            		            j9 = k;
            		            k9 = j1;
            		            i9 = l11;
            		            k8 = i12;
            		        }
            				break;
            			case 4:
            				j8 = l3 + 1;
            		        c3 = s.charAt(l3);
            		        if(c3 != ':') {
            		        	if(c3 != '+' && c3 != '-') {
            		        		if(c3 != 'Z') {
            		        			 k8 = l;
            		        		        k9 = j1;
            		        		        i9 = j8;
            		        		        j9 = k;
            		        		} else {
            		        			k8 = l;
            		        	        k9 = j1;
            		        	        i9 = j8;
            		        	        j9 = k;
            		        		}
            		        	}else {
            		        		if(c3 == '-')
            		                    byte2 = -1;
            		                else
            		                    byte2 = 1;
            		                k11 = j1 + 2;
            		                k8 = j8 + 2;
            		                i9 = j8;
            		                j9 = byte2;
            		                k9 = k11;
            		        	}
            		        } else {
            		        	k8 = j8 + 2;
            		            k9 = j1;
            		            i9 = j8;
            		            j9 = k;
            		        }
            				break;
            			case 5:
            				j10 = l3 + 1;
            		        c2 = s.charAt(l3);
            		        if(c2 != '.') {
            		        	if(c2 != '+' && c2 != '-')
            		                {
            		        		if(c2 != 'Z') {
            		        			j9 = k;
            		        	        j11 = l;
            		        	        k9 = j1;
            		        	        i9 = j10;
            		        	        k8 = j11;
            		        		} else {
            		        			j9 = k;
            		        	        i11 = l;
            		        	        k9 = j1;
            		        	        i9 = j10;
            		        	        k8 = i11;
            		        		}
            		                }
            		            if(c2 == '-')
            		                byte1 = -1;
            		            else
            		                byte1 = 1;
            		            k10 = j1 + 1;
            		            l10 = j10 + 2;
            		            i9 = j10;
            		            k8 = l10;
            		            j9 = byte1;
            		            k9 = k10;
            		        }else {
            		        	l = j10;
            		            do
            		                l++;
            		            while(Character.isDigit(s.charAt(l)));
            		    
            		            j9 = k;
            		            j11 = l;
            		            k9 = j1;
            		            i9 = j10;
            		            k8 = j11;
            		        }
            				break;
            			case 6:
            				j8 = l3 + 1;
            		        c1 = s.charAt(l3);
            		        if(c1 != '+' && c1 != '-') {
            		        	if(c1 != 'Z') {
            		        		k8 = l;
            		                k9 = j1;
            		                i9 = j8;
            		                j9 = k;
            		        	} else {
            		        		k8 = l;
            		                k9 = j1;
            		                i9 = j8;
            		                j9 = k;
            		        	}
            		        }
            		        else {
            		        	if(c1 == '-')
            		            byte0 = -1;
            		        else
            		            byte0 = 1;
            		        k8 = j8 + 2;
            		        l8 = j1;
            		        i9 = j8;
            		        j9 = byte0;
            		        k9 = l8;}
            				break;
            			default:
            				k8 = l;
            		        j9 = k;
            		        k9 = j1;
            		        i9 = l3;
            				break;
            		}
            		
            		l9 = k9 + 1;
                    k = j9;
                    k1 = i8;
                    l1 = l7;
                    i2 = k7;
                    j2 = j7;
                    k2 = i7;
                    l2 = l6;
                    i3 = k6;
                    j3 = j6;
                    k3 = 0;
                    i10 = i9;
                    j1 = l9;
                    l = k8;
                    i1 = i10;

continue;
                  
            	} else {
                	i4 = i8;
                    i5 = l7;
                    j4 = k7;
                    l4 = j7;
                    k4 = i7;
                    k5 = l6;
                    j5 = k6;
                    l5 = j6;
                    
                    parseposition.setIndex(l3);
                    StringBuilder stringbuilder = (new StringBuilder()).append("GMT+").append(i5).append(":");
                    
                    if(i4 < 10)
                        obj = (new StringBuilder()).append("0").append(i4).toString();
                    else
                        obj = Integer.valueOf(i4);
                    calendar.setTimeZone(TimeZone.getTimeZone(stringbuilder.append(obj).toString()));
                    calendar.set(1, l5);
                    calendar.set(2, j5 + -1);
                    calendar.set(5, k5);
                    calendar.set(11, k4);
                    calendar.set(12, l4);
                    calendar.set(13, j4);
                    return calendar.getTime();
                }
            }
        }
}
 
    }

    private static cls_d a = null;
    private static final long serialVersionUID = 1L;

}

