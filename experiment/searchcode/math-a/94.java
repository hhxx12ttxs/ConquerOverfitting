/**
 * Copyright (C) 2008 Mathieu Carbou <mathieu.carbou@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mycila.testing.plugin.guice;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a method that provides Guice module instances.
 * If a module needs to be instanciated with a specific
 * logic, you can add methods that provide Modules.
 * Supported method return type is any type extending:
 * Module, Module[] and Iterable&lt;Module>. Example:
 * <pre>
 * Module[] buildModules() {
 *     return ...;
 * }
 *
 * Module buildModule() {
 *     return ...;
 * }
 *
 * List&lt;Module> moduleList() {
 *     return ...;
 * }
 * </pre>
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface ModuleProvider {
}
