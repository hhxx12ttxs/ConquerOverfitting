/*
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * 
 * Project: JGentleFramework
 */
package org.jgentleframework.configure.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that a dependency should be injected to the annotated field, setter
 * method of a bean instance or also be injected to the attribute element of
 * another {@link Annotation}.
 * 
 * @author LE QUOC CHUNG - mailto: <a
 *         href="mailto:skydunkpro@yahoo.com">skydunkpro@yahoo.com</a>
 * @date Oct 3, 2007
 * @see Outject
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
public @interface Inject {
	/**
	 * The specified name or reference name of injected instance. If no value
	 * name is explicitly specified, the container will use class type of
	 * annotated field or return type of annotated setter method to indicate
	 * injected object.
	 */
	String value() default "";

	/**
	 * If specifies <b>true</b>, the container will automatically replace the
	 * value of element with new specified injected instance disregard whether
	 * the current value of element is <code>null</code>. Otherwise, if
	 * specifies <b>false</b>, and current value of desired inject element is
	 * not <code>null</code>, the container will bypass current injecting
	 * execution.
	 * <p>
	 * Note that this attribute is not effected in case the {@link Inject}
	 * annotation is marked on setter method or the attribute element of another
	 * {@link Annotation}. In this case, its value is <b>true</b>.
	 * <p>
	 * The default value is <b>true</b>.
	 */
	boolean alwaysInject() default true;

	/**
	 * M?c ??nh khi <code>inject</code> m?t <code>dependency</code> vŕo m?t th?c
	 * th? ch? ??nh thě <code>container</code> s? t? ??ng kh?i t?o vŕ th?c thi
	 * inject t?i th?i ?i?m kh?i t?o ??i t??ng. N?u thu?c tính
	 * <code>invocation</code> ???c ch? ??nh lŕ <b>true</b>, thě khi
	 * <b>inject</b>, <code>container</code> s? l?a ch?n th?i ?i?m
	 * <code>invocation</code> c?a <code>instance</code> ?? th?c thi
	 * <code>injecting</code>.
	 * <p>
	 * Hay nói cách khác <code>dependency</code> s? ???c <code>inject</code> vŕo
	 * <code>instance</code> t?i th?i ?i?m m?t <code>method</code> nŕo ?ó c?a
	 * <code>instance</code> ???c <code>invoke</code>. Trong tr??ng h?p c?a
	 * annotation, ?ó chính lŕ th?i ?i?m l?y ra giá tr? thu?c tính t??ng ?ng c?a
	 * chính <code>annotation</code>.
	 * <p>
	 * <b>L?u ý:</b>
	 * <p>
	 * - N?u <code>{@code @Inject}</code> ???c ch? ??nh tręn tham s? c?a
	 * <code>constructor</code> thě thu?c tính <code>invocation</code> có hay
	 * không ???c ch? ??nh ??u không có hi?u l?c. T??ng ???ng
	 * <code>invocation</code> b?ng <b>false</b>.
	 */
	boolean invocation() default false;

	/**
	 * Specifies that the injected value must not be <code>null</code>.
	 * <p>
	 * The default is <b>false</b>;
	 */
	boolean required() default false;
}

