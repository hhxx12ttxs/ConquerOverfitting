package com.gft.larozanam.client.componentes.util;

import java.math.BigDecimal;
import java.text.ParseException;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;

public abstract class NumberParserRenderer<E extends Number> extends AbstractRenderer<E> implements Parser<E> {
        
        public static final NumberParserRenderer<Long> getLongInstance(){
                return LONG_PARSER_RENDERER;
        }
        private static final NumberParserRenderer<Long> LONG_PARSER_RENDERER = new NumberParserRenderer<Long>() {               
                @Override
                public String render(Long object) {
                        if (object == null)
                                return null;
                        return String.valueOf(object);
                }
                
                @Override
                public Long parse(CharSequence text) throws ParseException {
                        String string = (String) text;
                        if (string == null || string.isEmpty())
                                return null;
                        return Long.parseLong(string);
                }
        }; 
        
        public static final NumberParserRenderer<BigDecimal> getDecimalInstance(){
                return DECIMAL_PARSER_RENDERER;
        }
        private static final NumberParserRenderer<BigDecimal> DECIMAL_PARSER_RENDERER = new NumberParserRenderer<BigDecimal>() {
                @Override
                public String render(BigDecimal object) {
                        if (object == null)
                                return null;
                        return String.valueOf(object).replace(".", ",");
                }
                
                @Override
                public BigDecimal parse(CharSequence text) throws ParseException {
                        String string = (String) text;
                        if (string == null || string.isEmpty())
                                return null;
                        return new BigDecimal(string.replace(",", "."));
                }
        };
        
        
        public static final NumberParserRenderer<Integer> getIntegerInstance(){
            return INTEGER_PARSER_RENDERER;
	    }
	    private static final NumberParserRenderer<Integer> INTEGER_PARSER_RENDERER = new NumberParserRenderer<Integer>() {               
	            @Override
	            public String render(Integer object) {
	                    if (object == null)
	                            return null;
	                    return String.valueOf(object);
	            }
	            
	            @Override
	            public Integer parse(CharSequence text) throws ParseException {
	                    String string = (String) text;
	                    if (string == null || string.isEmpty())
	                            return null;
	                    return Integer.parseInt(string);
	            }
	    }; 
        
}
