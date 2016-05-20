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
package org.jgentleframework.context.beans;

import org.jgentleframework.configure.enums.Scope;
import org.jgentleframework.context.injecting.Provider;
import org.jgentleframework.context.injecting.scope.ScopeImplementation;

/**
 * Interface to be implemented by object beans used within a JGentle container
 * which are themselves factories. If a bean implements this interface, it is
 * used as a factory for an object bean to expose, not directly as a bean
 * instance that will be exposed itself.
 * <p>
 * A bean that implements this interface cannot be used as a normal bean. A
 * FactoryBean is defined in a bean style, but the object exposed for bean
 * references (getObject() is always the object that it creates.
 * 
 * @author LE QUOC CHUNG - mailto: <a
 *         href="mailto:skydunkpro@yahoo.com">skydunkpro@yahoo.com</a>
 * @date Jun 2, 2008 3:07:34 PM
 */
public interface FactoryBean {
	/**
	 * Tr? v? m?t th?c th? <code>instance</code> ???c qu?n lý b?i
	 * <code>factory</code> hi?n hŕnh. <code>Instance bean</code> ???c qu?n
	 * lý có th? ???c ch? ??nh m?u Singleton design pattern (tr? v? 1 vŕ ch? m?t
	 * reference ??n m?t bean duy nh?t ch? ??nh). Trong tr??ng h?p nŕy giá tr?
	 * tr? v? c?a cŕi ??t c?a ph??ng th?c {@link #isSingleton()} nęn tr? v?
	 * <b>true</b>.
	 * <p>
	 * <i><b>L?u ý r?ng:</b></i>
	 * <p> - Theo m?c ??nh <code>object bean</code> tr? v? thông qua
	 * {@link #getBean()} không nh?n ???c s? h? tr? v? scope vd nh?
	 * {@link Scope} ho?c <code>custome scope</code> ({@link ScopeImplementation})
	 * t? phía container. Vi?c ch? ??nh scope tręn khai báo ??nh ngh?a c?u hěnh
	 * c?a hi?n th?c {@link FactoryBean} ch? có ý ngh?a tręn chính b?n thân
	 * {@link FactoryBean} ?ó, vŕ không có hi?u l?c tręn các object bean ???c
	 * tr? v? t? FactoryBean ch? ??nh.
	 * <p> - ?? các object bean tr? v? th?c s? có th? nh?n ???c s? h? tr? v?
	 * scope, c?ng nh? các <code>services</code> ???c cung c?p b?i
	 * <code>JGentle container</code> nh? IoC, AOP, ... FactoryBean có th? ch?
	 * ??nh implements {@link ProviderAware} interface ?? có th? tri?u g?i các
	 * ph??ng th?c getBean() t? {@link Provider} container.
	 * <p> - Trong tr??ng h?p, cŕi ??t c?a {@link FactoryBean} có ch? ??nh
	 * implements {@link ProviderAware} vŕ th?c hi?n vi?c kh?i t?o object bean
	 * thông qua ??i t??ng {@link Provider} container thě {@link #isSingleton()}
	 * nh?t thi?t ph?i tr? v? lŕ <b>false</b> dů trong b?t kě tr??ng h?p nŕo
	 * (k? c? khi <code>bean</code> ch? ??nh truy v?n ra t?
	 * <code>container</code> ???c c?u hěnh lŕ <code>Singleton scope</code>
	 * do các beans nŕy ?ă ???c container qu?n lý scope t? thŕnh ph?n lői). ?i?u
	 * nŕy giúp ??m b?o cho container không qu?n lý d? th?a các thông tin c?a
	 * các beans t?i <code>singleton cache</code>.
	 * <p> - Cŕi ??t c?a ph??ng th?c {@link #getBean()} không ???c phép tr? v?
	 * m?t giá tr? <b>null</b>, n?u vi ph?m ngo?i l?
	 * {@link FactoryBeanProcessException} s? ???c ném ra t?i th?i ?i?m
	 * run-time.
	 * 
	 * @return tr? v? m?t {@link Object} lŕ object bean hi?n th?c hoá c?a
	 *         {@link FactoryBean} hi?n hŕnh.
	 * @throws Exception
	 * @throws FactoryBeanProcessException
	 */
	Object getBean() throws Exception;

	/**
	 * Trong tr??ng h?p {@link FactoryBean} hi?n hŕnh luôn tr? v? 1 vŕ ch? 1
	 * reference duy nh?t ??n 1 object bean ch? ??nh <i>(<b>Singleton</b>
	 * design pattern)</i> ho?c ???c qu?n lý b?i h? th?ng
	 * <code>singleton cache</code> c?a rięng FactoryBean, vi?c ch? ??nh
	 * {@link #isSingleton()} tr? v? <b>true</b> có th? giúp container qu?n lý
	 * object bean tr? v? d?a tręn h? th?ng <code>singleton cache</code> c?a
	 * chính <code>container</code>.
	 * <p>
	 * <b><i>L?u ý r?ng:</i></b>
	 * <p> - Vi?c ch? ??nh {@link #isSingleton()} tr? v? <b>false</b> không có
	 * ngh?a r?ng b?t bu?c m?i khi tri?u g?i {@link #getBean()} luôn ph?i tr? v?
	 * m?t <code>object bean</code> m?i, ??c l?p.
	 * <p> - Ch? nęn tr? v? cŕi ??t {@link #isSingleton()} tr? v? <b>true</b>
	 * khi vŕ ch? khi {@link #getBean()} luôn tr? v? m?t <code>reference</code>
	 * duy nh?t.
	 * 
	 * @return tr? v? <b>true</b> n?u cŕi ??t c?a ph??ng th?c
	 *         {@link #getBean()} luôn tr? v? m?t vŕ ch? m?t
	 *         <code>reference</code> ??n 1 bean duy nh?t ch? ??nh.
	 */
	boolean isSingleton();
}

