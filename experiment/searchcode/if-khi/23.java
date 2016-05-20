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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jgentleframework.configure.enums.Scope;

/**
 * Specifies that a Jgentle dependency component should be outjected from the
 * annotated field or getter method of a bean instance.
 * 
 * @author LE QUOC CHUNG - mailto: <a
 *         href="mailto:skydunkpro@yahoo.com">skydunkpro@yahoo.com</a>
 * @date Oct 3, 2007
 * @see Inject
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Documented
public @interface Outject {
	/**
	 * The specified name or reference name of outjected instance. If no value
	 * name is explicitly specified, the container will use class type of
	 * outjected object to indicate outject object.
	 */
	String value() default "";

	/**
	 * Specifies that the oujected value must not be <code>null</code>.
	 * <p>
	 * The default is <b>true</b>;
	 */
	boolean required() default true;

	/**
	 * M?c ??nh khi <code>outject</code> m?t dependency vŕo context thě
	 * container s? t? ??ng kh?i t?o vŕ <code>outject</code> t?i th?i ?i?m kh?i
	 * t?o ??i t??ng bean, t??ng ?ng <code>invocation</code> ch? ??nh lŕ
	 * <b>false</b>. N?u thu?c tính <code>invocation</code> ???c ch? ??nh lŕ
	 * <b>true</b>, thě khi <code>outject</code> m?t dependency vŕo context,
	 * container s? l?a ch?n th?i ?i?m <b>invocation</b> c?a instance ?? outject
	 * dependency ch? ??nh.
	 * <p>
	 * Hay nói cách khác <code>dependency</code> s? ???c <code>outject</code>
	 * vŕo <code>context</code> t?i th?i ?i?m bean instance ???c
	 * <code>invoke</code>.
	 * <p>
	 * <b>L?u ý:</b> b?t kě method nŕo khi ???c invoke ??u <i>kích ho?t</i> ?i?u
	 * ki?n ho?t ??ng c?a <b>outjection</b> ngo?i tr? các method có prefix lŕ
	 * "<b>set</b>" ho?c "<b>get</b>". Hay nói cách khác t?t c? các
	 * <code>setter vŕ getter method</code> khi ???c invoke ??u không lŕm th?c
	 * thi ti?n trěnh <b>outjection</b>.
	 */
	boolean invocation() default true;

	/**
	 * Specifies the scope to outject to. If no scope is explicitly specified,
	 * the default scope depends upon the scope value of reference instance
	 * according to <code>'value'</code> attribute if it was existed. In case there is no
	 * existed reference instance, the default scope will be
	 * {@link Scope#SINGLETON}.
	 * 
	 * @see {@link Scope}
	 */
	Scope scope() default Scope.UNSPECIFIED;
}

