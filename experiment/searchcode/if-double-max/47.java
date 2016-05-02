<<<<<<< HEAD
/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.ui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Mathieu Bastian
 */
public class JRangeSliderPanel extends javax.swing.JPanel {

    public static final String LOWER_BOUND = "lowerbound";
    public static final String UPPER_BOUND = "upperbound";
    private static final int SLIDER_MAXIMUM = 1000;
    private String lowerBound = "N/A";
    private String upperBound = "N/A";
    private Range range;

    /** Creates new form JRangeSliderPanel */
    public JRangeSliderPanel() {
        initComponents();
        ((JRangeSlider) rangeSlider).setUpperValue(1000);
        rangeSlider.setOpaque(false);
        lowerBoundTextField.setOpaque(false);
        upperBoundTextField.setOpaque(false);

        lowerBoundTextField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                lowerBoundTextField.setEnabled(true);
                lowerBoundTextField.selectAll();
            }
        });
        lowerBoundTextField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!lowerBoundTextField.getText().equals(lowerBound)) {
                    lowerBound = lowerBoundTextField.getText();
                    if (range != null) {
                        range.setLowerBound(lowerBound);
                        firePropertyChange(LOWER_BOUND, null, lowerBound);
                    }
                } else {
                    lowerBound = lowerBoundTextField.getText();
                }
                refreshBoundTexts();
                lowerBoundTextField.setEnabled(false);
            }
        });
        lowerBoundTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                lowerBoundTextField.setEnabled(false);
            }
        });
        upperBoundTextField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                upperBoundTextField.setEnabled(true);
                upperBoundTextField.selectAll();
            }
        });
        upperBoundTextField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!upperBoundTextField.getText().equals(upperBound)) {
                    upperBound = upperBoundTextField.getText();
                    if (range != null) {
                        range.setUpperBound(upperBound);
                        firePropertyChange(UPPER_BOUND, null, upperBound);
                    }
                } else {
                    upperBound = upperBoundTextField.getText();
                }
                refreshBoundTexts();
                upperBoundTextField.setEnabled(false);
            }
        });
        upperBoundTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                upperBoundTextField.setEnabled(false);
            }
        });

        rangeSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JRangeSlider source = (JRangeSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    if (range != null) {
                        range.refreshBounds();
                        refreshBoundTexts();
                    }
                }
            }
        });
    }

    private void refreshBoundTexts() {
        if (range != null) {
            lowerBound = range.lowerBound.toString();
            upperBound = range.upperBound.toString();
            lowerBoundTextField.setText(lowerBound);
            upperBoundTextField.setText(upperBound);
        }
    }

    public JRangeSlider getSlider() {
        return (JRangeSlider) rangeSlider;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        if (!range.min.equals(range.max)) {
            this.range = range;
            rangeSlider.setEnabled(true);
            range.refreshSlider();
            refreshBoundTexts();
        } else {
            lowerBound = range.lowerBound.toString();
            upperBound = range.upperBound.toString();
            lowerBoundTextField.setText(lowerBound);
            upperBoundTextField.setText(upperBound);
            rangeSlider.setEnabled(false);
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        rangeSlider = new JRangeSlider();
        lowerBoundTextField = new javax.swing.JTextField();
        upperBoundTextField = new javax.swing.JTextField();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        rangeSlider.setMaximum(1000);
        rangeSlider.setValue(0);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(rangeSlider, gridBagConstraints);

        lowerBoundTextField.setText(org.openide.util.NbBundle.getMessage(JRangeSliderPanel.class, "JRangeSliderPanel.lowerBoundTextField.text")); // NOI18N
        lowerBoundTextField.setBorder(null);
        lowerBoundTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(lowerBoundTextField, gridBagConstraints);

        upperBoundTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        upperBoundTextField.setText(org.openide.util.NbBundle.getMessage(JRangeSliderPanel.class, "JRangeSliderPanel.upperBoundTextField.text")); // NOI18N
        upperBoundTextField.setBorder(null);
        upperBoundTextField.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(upperBoundTextField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField lowerBoundTextField;
    private javax.swing.JSlider rangeSlider;
    private javax.swing.JTextField upperBoundTextField;
    // End of variables declaration//GEN-END:variables

    public static class Range {

        private JRangeSliderPanel slider;
        private Object min;
        private Object max;
        private Object lowerBound;
        private Object upperBound;
        private int sliderLowValue = -1;
        private int sliderUpValue = -1;

        public Range(JRangeSliderPanel slider, Object min, Object max) {
            this.slider = slider;
            this.min = min;
            this.max = max;
            this.lowerBound = min;
            this.upperBound = max;
        }

        public Range(JRangeSliderPanel slider, Object min, Object max, Object lowerBound, Object upperBound) {
            this.slider = slider;
            this.min = min;
            this.max = max;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public Object getLowerBound() {
            return lowerBound;
        }

        public Object getUpperBound() {
            return upperBound;
        }

        private void setLowerBound(String bound) {
            if (min instanceof Float) {
                try {
                    Float l = Float.parseFloat(bound);
                    if (l < (Float) min) {
                        lowerBound = min;
                    } else if (l > (Float) upperBound) {
                        lowerBound = upperBound;
                    } else {
                        lowerBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Double) {
                try {
                    Double l = Double.parseDouble(bound);
                    if (l < (Double) min) {
                        lowerBound = min;
                    } else if (l > (Double) upperBound) {
                        lowerBound = upperBound;
                    } else {
                        lowerBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Integer) {
                try {
                    Integer l = Integer.parseInt(bound);
                    if (l < (Integer) min) {
                        lowerBound = min;
                    } else if (l > (Integer) upperBound) {
                        lowerBound = upperBound;
                    } else {
                        lowerBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Long) {
                try {
                    Long l = Long.parseLong(bound);
                    if (l < (Long) min) {
                        lowerBound = min;
                    } else if (l > (Long) upperBound) {
                        lowerBound = upperBound;
                    } else {
                        lowerBound = l;
                    }
                } catch (Exception e) {
                }
            }
            refreshSlider();
        }

        private void setUpperBound(String bound) {
            if (min instanceof Float) {
                try {
                    Float l = Float.parseFloat(bound);
                    if (l > (Float) max) {
                        upperBound = max;
                    } else if (l < (Float) lowerBound) {
                        upperBound = lowerBound;
                    } else {
                        upperBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Double) {
                try {
                    Double l = Double.parseDouble(bound);
                    if (l > (Double) max) {
                        upperBound = max;
                    } else if (l < (Double) lowerBound) {
                        upperBound = lowerBound;
                    } else {
                        upperBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Integer) {
                try {
                    Integer l = Integer.parseInt(bound);
                    if (l > (Integer) max) {
                        upperBound = max;
                    } else if (l < (Integer) lowerBound) {
                        upperBound = lowerBound;
                    } else {
                        upperBound = l;
                    }
                } catch (Exception e) {
                }
            } else if (min instanceof Long) {
                try {
                    Long l = Long.parseLong(bound);
                    if (l > (Long) max) {
                        upperBound = max;
                    } else if (l < (Long) lowerBound) {
                        upperBound = lowerBound;
                    } else {
                        upperBound = l;
                    }
                } catch (Exception e) {
                }
            }
            refreshSlider();
        }

        private void refreshSlider() {
            double normalizedLow = 0.;
            double normalizedUp = 1.;
            if (min instanceof Float) {
                normalizedLow = ((Float) lowerBound - (Float) min) / ((Float) max - (Float) min);
                normalizedUp = ((Float) upperBound - (Float) min) / ((Float) max - (Float) min);
            } else if (min instanceof Double) {
                normalizedLow = ((Double) lowerBound - (Double) min) / ((Double) max - (Double) min);
                normalizedUp = ((Double) upperBound - (Double) min) / ((Double) max - (Double) min);
            } else if (min instanceof Integer) {
                normalizedLow = ((Integer) lowerBound - (Integer) min) / (double) ((Integer) max - (Integer) min);
                normalizedUp = ((Integer) upperBound - (Integer) min) / (double) ((Integer) max - (Integer) min);
            } else if (min instanceof Long) {
                normalizedLow = ((Long) lowerBound - (Long) min) / (double) ((Long) max - (Long) min);
                normalizedUp = ((Long) upperBound - (Long) min) / (double) ((Long) max - (Long) min);
            }
            sliderLowValue = (int) (normalizedLow * SLIDER_MAXIMUM);
            sliderUpValue = (int) (normalizedUp * SLIDER_MAXIMUM);
            slider.getSlider().setValues(sliderLowValue, sliderUpValue);
//            slider.getSlider().setUpperValue(sliderUpValue);
//            slider.getSlider().setValue(sliderLowValue);
        }

        private void refreshBounds() {
            boolean lowerChanged = slider.getSlider().getValue() != sliderLowValue;
            boolean upperChanged = slider.getSlider().getUpperValue() != sliderUpValue;
            sliderLowValue = slider.getSlider().getValue();
            sliderUpValue = slider.getSlider().getUpperValue();

            double normalizedLow = slider.getSlider().getValue() / (double) SLIDER_MAXIMUM;
            double normalizedUp = slider.getSlider().getUpperValue() / (double) SLIDER_MAXIMUM;
            if (min instanceof Float) {
                lowerBound = lowerChanged ? new Float((normalizedLow * ((Float) max - (Float) min)) + (Float) min) : lowerBound;
                upperBound = upperChanged ? new Float((normalizedUp * ((Float) max - (Float) min)) + (Float) min) : upperBound;
            } else if (min instanceof Double) {
                lowerBound = lowerChanged ? new Double((normalizedLow * ((Double) max - (Double) min)) + (Double) min) : lowerBound;
                upperBound = upperChanged ? new Double((normalizedUp * ((Double) max - (Double) min)) + (Double) min) : upperBound;
            } else if (min instanceof Integer) {
                lowerBound = lowerChanged ? new Integer((int) ((normalizedLow * ((Integer) max - (Integer) min)) + (Integer) min)) : lowerBound;
                upperBound = upperChanged ? new Integer((int) ((normalizedUp * ((Integer) max - (Integer) min)) + (Integer) min)) : upperBound;
            } else if (min instanceof Long) {
                lowerBound = lowerChanged ? new Long((long) ((normalizedLow * ((Long) max - (Long) min)) + (Long) min)) : lowerBound;
                upperBound = upperChanged ? new Long((long) ((normalizedUp * ((Long) max - (Long) min)) + (Long) min)) : upperBound;
            }

            if (lowerChanged) {
                slider.firePropertyChange(LOWER_BOUND, null, lowerBound);
            }
            if (upperChanged) {
                slider.firePropertyChange(UPPER_BOUND, null, upperBound);
            }
        }
    }
}
=======

// $Id$

package net.sf.persist.tests.framework;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

/**
 * Set of helpers to build and manipulate beans at runtime.
 */
public class DynamicBean {
	
	public static Class createBeanClass(BeanMap beanMap, boolean noTable) {

		ClassPool pool = ClassPool.getDefault();
		CtClass cc = pool.makeClass("net.sf.persist.tests.generated." 
				+ beanMap.getClassName() + (noTable ? "NoTable" : ""));

		if (noTable) {	
			ClassFile cf = cc.getClassFile();
			ConstPool cp = cf.getConstPool();
			AnnotationsAttribute attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
			Annotation a;
			try {
				a = new Annotation(cp, pool.get("net.sf.persist.annotations.NoTable"));
			} catch (NotFoundException e) {
				throw new RuntimeException(e);
			}
			attr.setAnnotation(a);
			cf.addAttribute(attr);
			cf.setVersionToJava5();
		}
		
		try {

			for (FieldMap fieldMap : beanMap.getFields()) {

				String fieldName = fieldMap.getFieldName();
				Class fieldType = fieldMap.getTypes().get(0);

				String fieldNameU = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
				String fieldTypeName = fieldType.getCanonicalName();

				String getterCode = "public " + fieldTypeName + " get" + fieldNameU + "() { return " + fieldName + "; }";
				String setterCode = "public void set" + fieldNameU + "(" + fieldTypeName + " " + fieldName + ") { this." + fieldName + "=" + fieldName + "; }";

				CtField cf = new CtField(pool.get(fieldTypeName), fieldName, cc);
				cc.addField(cf);

				CtMethod cm = CtNewMethod.make(getterCode, cc);
				cc.addMethod(cm);

				cm = CtNewMethod.make(setterCode, cc);
				cc.addMethod(cm);
			}

			String toStringCode = "public String toString() { return net.sf.persist.tests.framework.DynamicBean.toString(this); }";
			CtMethod cm = CtNewMethod.make(toStringCode, cc);
			cc.addMethod(cm);

			String equalsCode = "public boolean equals(Object obj) { return net.sf.persist.tests.framework.DynamicBean.compareBeans(this,obj); }";
			cm = CtNewMethod.make(equalsCode, cc);
			cc.addMethod(cm);

			return cc.toClass();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object createInstance(Class cls, BeanMap beanMap, boolean useNulls) {
		Object obj = null;
		try {
			obj = cls.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (Field field : cls.getDeclaredFields()) {
			FieldMap fieldMap = beanMap.getField(field.getName());
			setRandomValue(obj, field, fieldMap, useNulls);
		}
		return obj;
	}

	private static void setRandomValue(Object obj, Field field, FieldMap fieldMap, boolean useNull) {

		Class fieldType = field.getType();
		int size = fieldMap.getSize();
		double min = fieldMap.getMin();
		double max = fieldMap.getMax();

		Object value = null;

		if (fieldType == Boolean.class)
			value = useNull ? null : new Boolean(randomBoolean());
		else if (fieldType == boolean.class)
			value = useNull ? false : randomBoolean();
		else if (fieldType == Byte.class)
			value = useNull ? null : new Byte(randomByte((byte)min, (byte)max));
		else if (fieldType == byte.class)
			value = useNull ? (byte) 0 : randomByte((byte)min, (byte)max);
		else if (fieldType == Byte[].class)
			value = useNull ? null : randomByteObjArray(size);
		else if (fieldType == byte[].class)
			value = useNull ? null : randomByteArray(size);
		else if (fieldType == Short.class)
			value = useNull ? null : new Short(randomShort((short)min, (short)max));
		else if (fieldType == short.class)
			value = useNull ? (short) 0 : randomShort((short)min, (short)max);
		else if (fieldType == Integer.class)
			value = useNull ? null : new Integer(randomInt((int)min, (int)max));
		else if (fieldType == int.class)
			value = useNull ? (int) 0 : randomInt((int)min, (int)max);
		else if (fieldType == Long.class)
			value = useNull ? null : new Long(randomLong((long)min, (long)max));
		else if (fieldType == long.class)
			value = useNull ? (long) 0 : randomLong((long)min, (long)max);
		else if (fieldType == Float.class)
			value = useNull ? null : new Float(randomFloat((float)min, (float)max));
		else if (fieldType == float.class)
			value = useNull ? (float) 0 : randomFloat((float)min, (float)max);
		else if (fieldType == Double.class)
			value = useNull ? null : new Double(randomDouble(min, max));
		else if (fieldType == double.class)
			value = useNull ? (double) 0 : randomDouble(min, max);
		else if (fieldType == Character.class)
			value = useNull ? null : new Character(randomChar());
		else if (fieldType == char.class)
			value = useNull ? ' ' : randomChar();
		else if (fieldType == Character[].class)
			value = useNull ? null : randomCharObjArray(size);
		else if (fieldType == char[].class)
			value = useNull ? null : randomCharArray(size);
		else if (fieldType == String.class)
			value = useNull ? null : randomString(size);
		else if (fieldType == BigDecimal.class)
			value = useNull ? null : new BigDecimal(randomLong((long)min, (long)max));
		else if (fieldType == java.io.Reader.class)
			value = useNull ? null : new StringReader(randomString(size));
		else if (fieldType == java.io.InputStream.class)
			value = useNull ? null : new ByteArrayInputStream(randomByteArray(size));
		else if (fieldType == java.util.Date.class)
			value = useNull ? null : new java.util.Date(randomTimestamp());
		else if (fieldType == java.sql.Date.class)
			value = useNull ? null : new java.sql.Date(randomTimestamp());
		else if (fieldType == java.sql.Time.class)
			value = useNull ? null : new java.sql.Time(randomTimestamp());
		else if (fieldType == java.sql.Timestamp.class)
			value = useNull ? null : new java.sql.Timestamp(randomTimestamp());
		else if (fieldType == java.sql.Blob.class)
			value = useNull ? null : new BytesBlob(randomByteArray(size));
		else if (fieldType == java.sql.Clob.class)
			value = useNull ? null : new StringClob(randomString(size));
		else {
			if (useNull)
				value = null;
			else {
				Map m = new HashMap();
				m.put(randomString(3), randomString(32));
				m.put(randomString(3), randomString(32));
				m.put(randomString(3), randomString(32));
				value = m;
			}
		}

		try {
			String fieldName = field.getName();
			Field f = obj.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			f.set(obj, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static Object getFieldValue(Object obj, String fieldName) {
		try {
			Field f = obj.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String toString(Object obj) {
		if (obj == null)
			return "null";
		StringBuffer sb = new StringBuffer();
		sb.append("{ ");
		for (Field field : obj.getClass().getDeclaredFields()) {
			String fieldName = field.getName();

			Object value = getFieldValue(obj, fieldName);
			String s = value == null ? "null" : value.toString();
			if (s.length() > 32)
				s = s.substring(0, 32) + "...";

			sb.append(fieldName + "=" + s + ", ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		sb.append(" }");
		return sb.toString();
	}
	
	/**
	 * Returns true if the field is a primitive number type (byte, short, int, etc.) and its value is zero,
	 * or if the field is an object and its value is null
	 */
	public static boolean isNull(Class cls, Object obj) {
		
		if (obj==null) return true;
		
		if (cls==boolean.class || cls==Boolean.class)
			return ((Boolean)obj).booleanValue()==false;
		else if (cls==byte.class || cls==Byte.class || cls==short.class || cls==Short.class 
				|| cls==int.class || cls==Integer.class || cls==long.class || cls==Long.class 
				|| cls==float.class || cls==Float.class  || cls==double.class || cls==Double.class 
				|| cls==BigDecimal.class) {
			
			// first cast to Number
			Number n = (Number) obj;
			return n.longValue()==0;
		}
		else 
			return false;
	}

	public static boolean compareBeans(Object o1, Object o2) {

		if (o1 == null && o2 == null)
			return true;
		if (o1 == o2)
			return true;
		if (o1 == null && o2 != null)
			return false;
		if (o1 != null && o2 == null)
			return false;
		if (o1.getClass() != o2.getClass())
			return false;

		try {

			for (Field field : o1.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				Object v1 = field.get(o1);
				Object v2 = field.get(o2);
				if (!compareValues(v1, v2))
					return false;
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return true;
	}
	
	
	public static boolean compareBeansFromDifferentClasses(Object o1, Object o2) {

		if (o1 == null && o2 == null)
			return true;
		if (o1 == o2)
			return true;
		if (o1 == null && o2 != null)
			return false;
		if (o1 != null && o2 == null)
			return false;

		try {

			for (Field f1 : o1.getClass().getDeclaredFields()) {
				f1.setAccessible(true);
				Object v1 = f1.get(o1);
				
				Field f2;
				try {
					f2 = o2.getClass().getDeclaredField(f1.getName());
				} catch (NoSuchFieldException e) {
					return false;
				}
				f2.setAccessible(true);
				Object v2 = f2.get(o2);
				
				if (!compareValues(v1, v2))
					return false;
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return true;
	}

	
	

	/**
	 * Compare values trying to convert types if they are found to be compatible
	 */
	public static boolean compareValues(Object v1, Object v2) {

		if (v1 == null && v2 == null)
			return true;
		if (v1 == v2)
			return true;
		if (v1 == null && v2 != null)
			return false;
		if (v1 != null && v2 == null)
			return false;

		// try to convert v2 into v1 type
		v2 = convertToType(v1.getClass(), v2);

		if (v1.getClass() != v2.getClass())
			return false;

		Class type = v1.getClass();

		try {

			if (type == Boolean.class || type == boolean.class) {
				if (!((Boolean) v1).equals((Boolean) v2))
					return false;
			} else if (type == Byte.class || type == byte.class) {
				if (!((Byte) v1).equals((Byte) v2))
					return false;
			} else if (type == Byte[].class) {
				if (!Arrays.equals((Byte[]) v1, (Byte[]) v2))
					return false;
			} else if (type == byte[].class) {
				if (!Arrays.equals((byte[]) v1, (byte[]) v2))
					return false;
			} else if (type == Short.class || type == short.class) {
				if (!((Short) v1).equals((Short) v2))
					return false;
			} else if (type == Integer.class || type == int.class) {
				if (!((Integer) v1).equals((Integer) v2))
					return false;
			} else if (type == Long.class || type == long.class) {
				if (!((Long) v1).equals((Long) v2))
					return false;
			} else if (type == Float.class || type == float.class) {
				Float v1f = (Float) v1;
				Float v2f = (Float) v2;
				if (Float.floatToIntBits(v1f) != Float.floatToIntBits(v2f))
					return false;
			} else if (type == Double.class || type == double.class) {
				Double v1d = (Double) v1;
				Double v2d = (Double) v2;
				if (Double.doubleToLongBits(v1d) != Double.doubleToLongBits(v2d))
					return false;
			} else if (type == Character.class || type == char.class) {
				if (!((Character) v1).equals((Character) v2))
					return false;
			} else if (type == Character[].class) {
				if (!Arrays.equals((Character[]) v1, (Character[]) v2))
					return false;
			} else if (type == char[].class) {
				if (!Arrays.equals((char[]) v1, (char[]) v2))
					return false;
			} else if (type == String.class) {
				if (!((String) v1).equals((String) v2))
					return false;
			} else if (type == BigDecimal.class) {
				if (!((BigDecimal) v1).equals((BigDecimal) v2))
					return false;
			} else if (type == Reader.class) {
				Reader r1 = (Reader) v1;
				Reader r2 = (Reader) v2;
				if (!compareReaders(r1,r2))
					return false;
			} else if (v1  == InputStream.class) {
				InputStream i1 = (InputStream) v1;
				InputStream i2 = (InputStream) v2;
				if (!compareInputStreams(i1,i2))
					return false;
			} else if (v1 instanceof Clob) {
				Clob c1 = (Clob) v1;
				Clob c2 = (Clob) v2;
				if (!compareReaders(c1.getCharacterStream(), c2.getCharacterStream()))
					return false;
			} else if (v1 instanceof Blob) {
				Blob b1 = (Blob) v1;
				Blob b2 = (Blob) v2;
				if (!compareInputStreams(b1.getBinaryStream(), b2.getBinaryStream()))
					return false;
			} else if (type == java.util.Date.class) {
				java.util.Date d1 = (java.util.Date) v1;
				java.util.Date d2 = (java.util.Date) v2;
				if (!d1.toString().substring(0,19).equals(d2.toString().substring(0,19)))
					return false;
			} else if (type == java.sql.Date.class) {
				java.sql.Date d1 = (java.sql.Date) v1;
				java.sql.Date d2 = (java.sql.Date) v2;
				if (!d1.toString().equals(d2.toString()))
					return false;
			} else if (type == java.sql.Time.class) {
				java.sql.Time d1 = (java.sql.Time) v1;
				java.sql.Time d2 = (java.sql.Time) v2;
				if (!d1.toString().equals(d2.toString()))
					return false;
			} else if (type == java.sql.Timestamp.class) {
				java.sql.Timestamp d1 = (java.sql.Timestamp) v1;
				java.sql.Timestamp d2 = (java.sql.Timestamp) v2;
				// quick fix for smalldatetimes is to compare up to 15, instead of 19
				if (!d1.toString().substring(0,15).equals(d2.toString().substring(0,15)))
					return false;
			} else if (!v1.equals(v2))
				return false;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return true;
	}
	
	/**
	 * Try to convert the provided object to the provided class. 
	 * The following groups of types allow for conversion among their types:
	 * { String, char, Character, char[], Character[], Reader }
	 * { byte[], Byte[], InputStream }
	 * { boolean, Boolean }
	 * { byte, Byte, short, Short, int, Integer, long, Long, float, Float, double, Double, BigDecimal }
	 * { java.sql.Timestamp, java.sql.Date, java.sql.Time, java.util.Date }
	 */
	public static Object convertToType(Class cls, Object value) {
		
		if (cls == value.getClass()) return value;
		
		// if cls implements Clob or Blob, upcast
		for (Class iface : cls.getInterfaces()) {
			if (iface==Clob.class) {
				cls = Clob.class;
				break;
			}
			else if (iface==Blob.class) {
				cls = Blob.class;
				break;
			}
		}
		
		Class clsValue = value.getClass();

		if (cls==String.class || cls==char.class || cls==Character.class 
				|| cls==char[].class || cls==Character[].class || cls==Reader.class || cls==Clob.class) {
			
			// first convert it to string
			
			String s = null;
			
			if (clsValue==String.class) {
				s = (String) value;
			}
			else if (clsValue==char.class || clsValue==Character.class) {
				s = "" + value;
			}
			else if (clsValue==char[].class) {
				s = String.copyValueOf((char[])value);
			}
			else if (clsValue==Character[].class) { 
				Character[] a = (Character[])value; 
				char[] ac = new char[a.length];
				for (int i=0; i<a.length; i++) ac[i] = a[i];
				s = String.copyValueOf(ac);
			}
			else if (value instanceof Reader) {
				Reader r2 = (Reader)value;
				s = readReader(r2);
			}
			else if (value instanceof Clob) {
				Clob c2 = (Clob)value;
				try {
					s = readReader(c2.getCharacterStream());
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
			else {
				return value;
			}
			
			// now convert it to the target type
			
			if (cls==String.class) {
				return s;
			}
			else if (cls==char.class) {
				if (s.length()==1) return s.charAt(0);
				else return value;
			}
			else if (cls==Character.class) {
				if (s.length()==1) return new Character(s.charAt(0));
				else return value;
			}
			else if (cls==char[].class) {
				return s.toCharArray();
			}
			else if (cls==Character[].class) {
				Character[] ret = new Character[s.length()];
				for (int i=0; i<s.length(); i++) ret[i] = new Character(s.charAt(i));
				return ret;
			}
			else if (cls==Reader.class) {
				return new StringReader(s);
			}
			else if (cls==Clob.class) {
				return new StringClob(s);
			}
		}
		
		else if (cls==byte[].class || cls==Byte[].class || cls==InputStream.class || cls==Blob.class) {
			
			// first convert to byte[]
			
			byte[] a = null;
			if (clsValue==byte[].class) {
				a = (byte[]) value;
			}
			else if (clsValue==Byte[].class) {
				Byte[] ba = (Byte[]) value;
				a = new byte[ba.length];
				for (int i=0; i<ba.length; i++) a[i]=ba[i];
			}
			else if (value instanceof InputStream) {
				InputStream is = (InputStream) value;
				a = readInputStream(is);
			}
			else if (value instanceof Blob) {
				Blob b = (Blob) value;
				try {
					a = readInputStream(b.getBinaryStream());
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
			else return value;
			
			// now convert to target class
			
			if (cls==byte[].class) {
				return a;
			}
			else if (cls==Byte[].class) {
				Byte[] ba = new Byte[a.length];
				for (int i=0; i<a.length; i++) ba[i]=a[i];
				return ba;
			}
			else if (cls==InputStream.class) {
				return new ByteArrayInputStream(a);
			}
			else if (cls==Blob.class) {
				return new BytesBlob(a);
			}
		}
		
		else if (clsValue==java.util.Date.class) {
			java.util.Date d = (java.util.Date)value;
			if (cls==java.sql.Date.class) {
				return new java.sql.Date(d.getTime());
			}
			if (cls==java.sql.Time.class) {
				return new java.sql.Time(d.getTime());
			}
			if (cls==java.sql.Timestamp.class) {
				return new java.sql.Timestamp(d.getTime());
			}
			else return value;
		}
		
		else if (cls==java.util.Date.class) {
			if (clsValue==java.sql.Date.class) {
				return new java.util.Date(((java.sql.Date)value).getTime());
			}
			if (clsValue==java.sql.Time.class) {
				return new java.util.Date(((java.util.Date)value).getTime());
			}
			if (clsValue==java.sql.Timestamp.class) {
				return new java.util.Date(((java.util.Date)value).getTime());
			}
			else return value;
		}
		
		else if (clsValue==boolean.class || clsValue==Boolean.class) {
			Boolean b = (Boolean) value;
			if (cls==boolean.class) return b.booleanValue();
			else if (cls==Boolean.class) return b;
			else return value;
		}
		
		else if (clsValue==byte.class || clsValue==Byte.class || clsValue==short.class || clsValue==Short.class 
				|| clsValue==int.class || clsValue==Integer.class || clsValue==long.class || clsValue==Long.class 
				|| clsValue==float.class || clsValue==Float.class  || clsValue==double.class || clsValue==Double.class 
				|| clsValue==BigDecimal.class) {
			
			// first cast to Number
			Number n = (Number) value;
			
			if (cls==byte.class) return n.byteValue();
			else if (cls==Byte.class) return new Byte(n.byteValue());
			else if (cls==short.class) return n.shortValue();
			else if (cls==Short.class) return new Short(n.shortValue());
			else if (cls==int.class) return n.intValue();
			else if (cls==Integer.class) return new Integer(n.intValue());
			else if (cls==long.class) return n.longValue();
			else if (cls==Long.class) return new Long(n.longValue());
			else if (cls==float.class) return n.floatValue();
			else if (cls==Float.class) return new Float(n.floatValue());
			else if (cls==double.class) return n.doubleValue();
			else if (cls==Double.class) return new Double(n.doubleValue());
			else if (cls==BigDecimal.class) return BigDecimal.valueOf(n.doubleValue());
			else return value;
		}
		
		return value;
	}
	
	public static byte[] readInputStream(InputStream is) {
		// assumes no more than 64KB of data
		try {
			byte[] buf = new byte[65535];
			int n = is.read(buf);
			if (n<0) n=0;
			byte[] ret = new byte[n];
			for (int i=0; i<n; i++) ret[i] = buf[i];
			return ret;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String readReader(Reader r) {
		char[] buf = new char[65535];
		try {
			int n = r.read(buf);
			return new String(buf, 0, n);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean compareReaders(Reader r1, Reader r2) {
		char[] buf1 = new char[65535];
		char[] buf2 = new char[65535];
		try {
			int n1 = r1.read(buf1);
			int n2 = r2.read(buf2);
			return (n1 == n2 && Arrays.equals(buf1, buf2));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static boolean compareInputStreams(InputStream i1, InputStream i2) {
		byte[] buf1 = new byte[65535];
		byte[] buf2 = new byte[65535];
		try {
			int n1 = i1.read(buf1);
			int n2 = i2.read(buf2);
			return (n1 == n2 && Arrays.equals(buf1, buf2));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// ---------- create random values ----------

	public static boolean randomBoolean() {
		return Math.random() > 0.5;
	}

	public static byte randomByte(byte min, byte max) {
		if (min==-1 && max==-1) {
			min = Byte.MIN_VALUE;
			max = Byte.MAX_VALUE;
		}
		return (byte) (min+Math.random()*(max-min));
	}

	public static byte[] randomByteArray(int size) {
		byte[] a = new byte[size];
		for (int i = 0; i < size; i++)
			a[i] = randomByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
		return a;
	}

	public static Byte[] randomByteObjArray(int size) {
		Byte[] a = new Byte[size];
		for (int i = 0; i < size; i++)
			a[i] = new Byte(randomByte(Byte.MIN_VALUE, Byte.MAX_VALUE));
		return a;
	}

	public static short randomShort(short min, short max) {
		if (min==-1 && max==-1) {
			min = Short.MIN_VALUE;
			max = Short.MAX_VALUE;
		}
		return (short) (min+Math.random()*(max-min));
	}

	public static int randomInt(int min, int max) {
		if (min==-1 && max==-1) {
			min = Integer.MIN_VALUE;
			max = Integer.MAX_VALUE;
		}
		return (int) (min+Math.random()*(max-min));
	}

	public static long randomLong(long min, long max) {
		if (min==-1 && max==-1) {
			min = Long.MIN_VALUE;
			max = Long.MAX_VALUE;
		}
		return (long) (min+Math.random()*(max-min));
	}

	public static float randomFloat(float min, float max) {
		if (min==-1 && max==-1) {
			min = Float.MIN_VALUE;
			max = Float.MAX_VALUE;
		}
		//double f = (int) (Math.random() * 10);
		double f = 0;
		return (float) ( ((long)(min+Math.random()*(max-min))) + f/10 );
	}

	public static double randomDouble(double min, double max) {
		if (min==-1 && max==-1) {
			min = Double.MIN_VALUE;
			max = Double.MAX_VALUE;
		}
		double f = (int) (Math.random() * 10);
		return ((long)(min+Math.random()*(max-min))) + f/10;
	}

	public static char randomChar() {
		byte[] b = new byte[] { (byte) (97 + (byte) (Math.random() * 26)) };
		return new String(b).charAt(0);
	}

	public static char[] randomCharArray(int size) {
		return randomString(size).toCharArray();
	}

	public static Character[] randomCharObjArray(int size) {
		Character[] a = new Character[size];
		int i = 0;
		for (char c : randomString(size).toCharArray()) {
			a[i] = new Character(c);
			i++;
		}
		return a;
	}

	public static String randomString(int size) {
		byte b[] = new byte[size];
		for (int i = 0; i < size; i++) {
			b[i] = (byte) (97 + (byte) (Math.random() * 26));
		}
		return new String(b);
	}
	
	public static long randomTimestamp() {
		return (long) (Math.random() * System.currentTimeMillis());
	}

}
>>>>>>> 76aa07461566a5976980e6696204781271955163

