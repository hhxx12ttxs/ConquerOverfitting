/*
 * Copyright (c) 2002-2012 Alibaba Group Holding Limited.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.citrus.util;

import static com.alibaba.citrus.util.CollectionUtil.*;
import static com.alibaba.citrus.util.ObjectUtil.*;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.alibaba.citrus.util.i18n.LocaleUtil;

/**
 * ?????????????
 * <p>
 * ????Java??????????<code>System.getProperty</code>????????????
 * <code>null</code>? ???????????<code>System.err</code>??
 * </p>
 *
 * @author Michael Zhou
 */
public class SystemUtil {
    private static final JvmSpecInfo     JVM_SPEC_INFO     = new JvmSpecInfo();
    private static final JvmInfo         JVM_INFO          = new JvmInfo();
    private static final JavaSpecInfo    JAVA_SPEC_INFO    = new JavaSpecInfo();
    private static final JavaInfo        JAVA_INFO         = new JavaInfo();
    private static final OsInfo          OS_INFO           = new OsInfo();
    private static final UserInfo        USER_INFO         = new UserInfo();
    private static final HostInfo        HOST_INFO         = new HostInfo();
    private static final JavaRuntimeInfo JAVA_RUNTIME_INFO = new JavaRuntimeInfo();

    /**
     * ??Java Virtual Machine Specification????
     *
     * @return <code>JvmSpecInfo</code>??
     */
    public static final JvmSpecInfo getJvmSpecInfo() {
        return JVM_SPEC_INFO;
    }

    /**
     * ??Java Virtual Machine Implementation????
     *
     * @return <code>JvmInfo</code>??
     */
    public static final JvmInfo getJvmInfo() {
        return JVM_INFO;
    }

    /**
     * ??Java Specification????
     *
     * @return <code>JavaSpecInfo</code>??
     */
    public static final JavaSpecInfo getJavaSpecInfo() {
        return JAVA_SPEC_INFO;
    }

    /**
     * ??Java Implementation????
     *
     * @return <code>JavaInfo</code>??
     */
    public static final JavaInfo getJavaInfo() {
        return JAVA_INFO;
    }

    /**
     * ???????JRE????
     *
     * @return <code>JreInfo</code>??
     */
    public static final JavaRuntimeInfo getJavaRuntimeInfo() {
        return JAVA_RUNTIME_INFO;
    }

    /**
     * ??OS????
     *
     * @return <code>OsInfo</code>??
     */
    public static final OsInfo getOsInfo() {
        return OS_INFO;
    }

    /**
     * ??User????
     *
     * @return <code>UserInfo</code>??
     */
    public static final UserInfo getUserInfo() {
        return USER_INFO;
    }

    /**
     * ??Host????
     *
     * @return <code>HostInfo</code>??
     */
    public static final HostInfo getHostInfo() {
        return HOST_INFO;
    }

    /** ??Java Virutal Machine Specification???? */
    public static final class JvmSpecInfo {
        private final String JAVA_VM_SPECIFICATION_NAME    = getSystemProperty("java.vm.specification.name", false);
        private final String JAVA_VM_SPECIFICATION_VERSION = getSystemProperty("java.vm.specification.version", false);
        private final String JAVA_VM_SPECIFICATION_VENDOR  = getSystemProperty("java.vm.specification.vendor", false);

        /** ??????????? */
        private JvmSpecInfo() {
        }

        /**
         * ????JVM spec.???????????<code>java.vm.specification.name</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"Java Virtual Machine Specification"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getName() {
            return JAVA_VM_SPECIFICATION_NAME;
        }

        /**
         * ????JVM spec.???????????<code>java.vm.specification.version</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"1.0"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getVersion() {
            return JAVA_VM_SPECIFICATION_VERSION;
        }

        /**
         * ????JVM spec.???????????<code>java.vm.specification.vendor</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"Sun Microsystems Inc."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getVendor() {
            return JAVA_VM_SPECIFICATION_VENDOR;
        }

        /**
         * ?Java Virutal Machine Specification??????????
         *
         * @return JVM spec.??????
         */
        @Override
        public final String toString() {
            StringBuilder buffer = new StringBuilder();

            append(buffer, "JavaVM Spec. Name:    ", getName());
            append(buffer, "JavaVM Spec. Version: ", getVersion());
            append(buffer, "JavaVM Spec. Vendor:  ", getVendor());

            return buffer.toString();
        }
    }

    /** ??Java Virtual Machine Implementation???? */
    public static final class JvmInfo {
        private final String JAVA_VM_NAME    = getSystemProperty("java.vm.name", false);
        private final String JAVA_VM_VERSION = getSystemProperty("java.vm.version", false);
        private final String JAVA_VM_VENDOR  = getSystemProperty("java.vm.vendor", false);
        private final String JAVA_VM_INFO    = getSystemProperty("java.vm.info", false);

        /** ??????????? */
        private JvmInfo() {
        }

        /**
         * ????JVM impl.???????????<code>java.vm.name</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"Java HotSpot(TM) Client VM"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getName() {
            return JAVA_VM_NAME;
        }

        /**
         * ????JVM impl.???????????<code>java.vm.version</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"1.4.2-b28"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getVersion() {
            return JAVA_VM_VERSION;
        }

        /**
         * ????JVM impl.???????????<code>java.vm.vendor</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"Sun Microsystems Inc."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getVendor() {
            return JAVA_VM_VENDOR;
        }

        /**
         * ????JVM impl.???????????<code>java.vm.info</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"mixed mode"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getInfo() {
            return JAVA_VM_INFO;
        }

        /**
         * ?Java Virutal Machine Implementation??????????
         *
         * @return JVM impl.??????
         */
        @Override
        public final String toString() {
            StringBuilder buffer = new StringBuilder();

            append(buffer, "JavaVM Name:    ", getName());
            append(buffer, "JavaVM Version: ", getVersion());
            append(buffer, "JavaVM Vendor:  ", getVendor());
            append(buffer, "JavaVM Info:    ", getInfo());

            return buffer.toString();
        }
    }

    /** ??Java Specification???? */
    public static final class JavaSpecInfo {
        private final String JAVA_SPECIFICATION_NAME    = getSystemProperty("java.specification.name", false);
        private final String JAVA_SPECIFICATION_VERSION = getSystemProperty("java.specification.version", false);
        private final String JAVA_SPECIFICATION_VENDOR  = getSystemProperty("java.specification.vendor", false);

        /** ??????????? */
        private JavaSpecInfo() {
        }

        /**
         * ????Java Spec.???????????<code>java.specification.name</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"Java Platform API Specification"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getName() {
            return JAVA_SPECIFICATION_NAME;
        }

        /**
         * ????Java Spec.???????????<code>java.specification.version</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"1.4"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.3
         */
        public final String getVersion() {
            return JAVA_SPECIFICATION_VERSION;
        }

        /**
         * ????Java Spec.???????????<code>java.specification.vendor</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"Sun Microsystems Inc."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getVendor() {
            return JAVA_SPECIFICATION_VENDOR;
        }

        /**
         * ?Java Specification??????????
         *
         * @return JVM spec.??????
         */
        @Override
        public final String toString() {
            StringBuilder buffer = new StringBuilder();

            append(buffer, "Java Spec. Name:    ", getName());
            append(buffer, "Java Spec. Version: ", getVersion());
            append(buffer, "Java Spec. Vendor:  ", getVendor());

            return buffer.toString();
        }
    }

    /** ??Java Implementation???? */
    public static final class JavaInfo {
        private final String  JAVA_VERSION       = getSystemProperty("java.version", false);
        private final float   JAVA_VERSION_FLOAT = getJavaVersionAsFloat();
        private final int     JAVA_VERSION_INT   = getJavaVersionAsInt();
        private final String  JAVA_VENDOR        = getSystemProperty("java.vendor", false);
        private final String  JAVA_VENDOR_URL    = getSystemProperty("java.vendor.url", false);
        private final boolean IS_JAVA_1_1        = getJavaVersionMatches("1.1");
        private final boolean IS_JAVA_1_2        = getJavaVersionMatches("1.2");
        private final boolean IS_JAVA_1_3        = getJavaVersionMatches("1.3");
        private final boolean IS_JAVA_1_4        = getJavaVersionMatches("1.4");
        private final boolean IS_JAVA_1_5        = getJavaVersionMatches("1.5");

        /** ??????????? */
        private JavaInfo() {
        }

        /**
         * ????Java impl.???????????<code>java.version</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"1.4.2"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getVersion() {
            return JAVA_VERSION;
        }

        /**
         * ????Java impl.???????????<code>java.version</code>??
         * <p>
         * ???
         * <ul>
         * <li>JDK 1.2?<code>1.2f</code>?</li>
         * <li>JDK 1.3.1?<code>1.31f</code></li>
         * </ul>
         * </p>
         *
         * @return ?????????????Java??????????????<code>0</code>?
         * @since Java 1.1
         */
        public final float getVersionFloat() {
            return JAVA_VERSION_FLOAT;
        }

        /**
         * ????Java impl.???????????<code>java.version</code>??
         * <p>
         * ???
         * <ul>
         * <li>JDK 1.2?<code>120</code>?</li>
         * <li>JDK 1.3.1?<code>131</code></li>
         * </ul>
         * </p>
         *
         * @return ?????????????Java??????????????<code>0</code>?
         * @since Java 1.1
         */
        public final int getVersionInt() {
            return JAVA_VERSION_INT;
        }

        /**
         * ????Java impl.????<code>float</code>??
         *
         * @return Java???<code>float</code>??<code>0</code>
         */
        private final float getJavaVersionAsFloat() {
            if (JAVA_VERSION == null) {
                return 0f;
            }

            String str = JAVA_VERSION.substring(0, 3);

            if (JAVA_VERSION.length() >= 5) {
                str = str + JAVA_VERSION.substring(4, 5);
            }

            return Float.parseFloat(str);
        }

        /**
         * ????Java impl.????<code>int</code>??
         *
         * @return Java???<code>int</code>??<code>0</code>
         */
        private final int getJavaVersionAsInt() {
            if (JAVA_VERSION == null) {
                return 0;
            }

            String str = JAVA_VERSION.substring(0, 1);

            str = str + JAVA_VERSION.substring(2, 3);

            if (JAVA_VERSION.length() >= 5) {
                str = str + JAVA_VERSION.substring(4, 5);
            } else {
                str = str + "0";
            }

            return Integer.parseInt(str);
        }

        /**
         * ????Java impl.???????????<code>java.vendor</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"Sun Microsystems Inc."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getVendor() {
            return JAVA_VENDOR;
        }

        /**
         * ????Java impl.??????URL????????<code>java.vendor.url</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"http://java.sun.com/"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getVendorURL() {
            return JAVA_VENDOR_URL;
        }

        /**
         * ????Java????
         * <p>
         * ??????????<code>java.version</code>???Java???????????
         * <code>false</code>
         * </p>
         *
         * @return ????Java???1.1????<code>true</code>
         */
        public final boolean isJava11() {
            return IS_JAVA_1_1;
        }

        /**
         * ????Java????
         * <p>
         * ??????????<code>java.version</code>???Java???????????
         * <code>false</code>
         * </p>
         *
         * @return ????Java???1.2????<code>true</code>
         */
        public final boolean isJava12() {
            return IS_JAVA_1_2;
        }

        /**
         * ????Java????
         * <p>
         * ??????????<code>java.version</code>???Java???????????
         * <code>false</code>
         * </p>
         *
         * @return ????Java???1.3????<code>true</code>
         */
        public final boolean isJava13() {
            return IS_JAVA_1_3;
        }

        /**
         * ????Java????
         * <p>
         * ??????????<code>java.version</code>???Java???????????
         * <code>false</code>
         * </p>
         *
         * @return ????Java???1.4????<code>true</code>
         */
        public final boolean isJava14() {
            return IS_JAVA_1_4;
        }

        /**
         * ????Java????
         * <p>
         * ??????????<code>java.version</code>???Java???????????
         * <code>false</code>
         * </p>
         *
         * @return ????Java???1.5????<code>true</code>
         */
        public final boolean isJava15() {
            return IS_JAVA_1_5;
        }

        /**
         * ????Java????
         *
         * @param versionPrefix Java????
         * @return ??????????<code>true</code>
         */
        private final boolean getJavaVersionMatches(String versionPrefix) {
            if (JAVA_VERSION == null) {
                return false;
            }

            return JAVA_VERSION.startsWith(versionPrefix);
        }

        /**
         * ????Java????????????????
         * <p>
         * ???
         * </p>
         * <ul>
         * <li>??JDK 1.2?<code>isJavaVersionAtLeast(1.2f)</code></li>
         * <li>??JDK 1.2.1?<code>isJavaVersionAtLeast(1.31f)</code></li>
         * </ul>
         *
         * @param requiredVersion ?????
         * @return ????Java????????????????<code>true</code>
         */
        public final boolean isJavaVersionAtLeast(float requiredVersion) {
            return getVersionFloat() >= requiredVersion;
        }

        /**
         * ????Java????????????????
         * <p>
         * ???
         * </p>
         * <ul>
         * <li>??JDK 1.2?<code>isJavaVersionAtLeast(120)</code></li>
         * <li>??JDK 1.2.1?<code>isJavaVersionAtLeast(131)</code></li>
         * </ul>
         *
         * @param requiredVersion ?????
         * @return ????Java????????????????<code>true</code>
         */
        public final boolean isJavaVersionAtLeast(int requiredVersion) {
            return getVersionInt() >= requiredVersion;
        }

        /**
         * ?Java Implementation??????????
         *
         * @return JVM impl.??????
         */
        @Override
        public final String toString() {
            StringBuilder buffer = new StringBuilder();

            append(buffer, "Java Version:    ", getVersion());
            append(buffer, "Java Vendor:     ", getVendor());
            append(buffer, "Java Vendor URL: ", getVendorURL());

            return buffer.toString();
        }
    }

    /** ???????JRE???? */
    public static final class JavaRuntimeInfo {
        private final String JAVA_RUNTIME_NAME    = getSystemProperty("java.runtime.name", false);
        private final String JAVA_RUNTIME_VERSION = getSystemProperty("java.runtime.version", false);
        private final String JAVA_HOME            = getSystemProperty("java.home", false);
        private final String JAVA_EXT_DIRS        = getSystemProperty("java.ext.dirs", false);
        private final String JAVA_ENDORSED_DIRS   = getSystemProperty("java.endorsed.dirs", false);
        private final String JAVA_CLASS_PATH      = getSystemProperty("java.class.path", false);
        private final String JAVA_CLASS_VERSION   = getSystemProperty("java.class.version", false);
        private final String JAVA_LIBRARY_PATH    = getSystemProperty("java.library.path", false);

        /** ??????????? */
        private JavaRuntimeInfo() {
        }

        /**
         * ????JRE???????????<code>java.runtime.name</code>??
         * <p>
         * ??Sun JDK 1.4.2?
         * <code>"Java(TM) 2 Runtime Environment, Standard Edition"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.3
         */
        public final String getName() {
            return JAVA_RUNTIME_NAME;
        }

        /**
         * ????JRE???????????<code>java.runtime.version</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"1.4.2-b28"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.3
         */
        public final String getVersion() {
            return JAVA_RUNTIME_VERSION;
        }

        /**
         * ????JRE?????????????<code>java.home</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"/opt/jdk1.4.2/jre"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getHomeDir() {
            return JAVA_HOME;
        }

        /**
         * ????JRE???????????????<code>java.ext.dirs</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"/opt/jdk1.4.2/jre/lib/ext:..."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.3
         */
        public final String getExtDirs() {
            return JAVA_EXT_DIRS;
        }

        /**
         * ????JRE?endorsed????????????<code>java.endorsed.dirs</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"/opt/jdk1.4.2/jre/lib/endorsed:..."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.4
         */
        public final String getEndorsedDirs() {
            return JAVA_ENDORSED_DIRS;
        }

        /**
         * ????JRE???classpath????????<code>java.class.path</code>??
         * <p>
         * ???<code>"/home/admin/myclasses:/home/admin/..."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getClassPath() {
            return JAVA_CLASS_PATH;
        }

        /**
         * ????JRE???classpath????????<code>java.class.path</code>??
         * <p>
         * ???<code>"/home/admin/myclasses:/home/admin/..."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String[] getClassPathArray() {
            return StringUtil.split(getClassPath(), getOsInfo().getPathSeparator());
        }

        /**
         * ????JRE?class???????????????<code>java.class.version</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"48.0"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getClassVersion() {
            return JAVA_CLASS_VERSION;
        }

        /**
         * ????JRE?library????????????<code>java.library.path</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"/opt/jdk1.4.2/bin:..."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getLibraryPath() {
            return JAVA_LIBRARY_PATH;
        }

        /**
         * ????JRE?library????????????<code>java.library.path</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"/opt/jdk1.4.2/bin:..."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String[] getLibraryPathArray() {
            return StringUtil.split(getLibraryPath(), getOsInfo().getPathSeparator());
        }

        /**
         * ????JRE?URL??packages??????????<code>java.library.path</code>??
         * <p>
         * ??Sun JDK 1.4.2?<code>"sun.net.www.protocol|..."</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getProtocolPackages() {
            return getSystemProperty("java.protocol.handler.pkgs", true);
        }

        /**
         * ??????JRE?????????
         *
         * @return JRE????????
         */
        @Override
        public final String toString() {
            StringBuilder buffer = new StringBuilder();

            append(buffer, "Java Runtime Name:      ", getName());
            append(buffer, "Java Runtime Version:   ", getVersion());
            append(buffer, "Java Home Dir:          ", getHomeDir());
            append(buffer, "Java Extension Dirs:    ", getExtDirs());
            append(buffer, "Java Endorsed Dirs:     ", getEndorsedDirs());
            append(buffer, "Java Class Path:        ", getClassPath());
            append(buffer, "Java Class Version:     ", getClassVersion());
            append(buffer, "Java Library Path:      ", getLibraryPath());
            append(buffer, "Java Protocol Packages: ", getProtocolPackages());

            return buffer.toString();
        }
    }

    /** ????OS???? */
    public static final class OsInfo {
        private final String  OS_VERSION         = getSystemProperty("os.version", false);
        private final String  OS_ARCH            = getSystemProperty("os.arch", false);
        private final String  OS_NAME            = getSystemProperty("os.name", false);
        private final boolean IS_OS_AIX          = getOSMatches("AIX");
        private final boolean IS_OS_HP_UX        = getOSMatches("HP-UX");
        private final boolean IS_OS_IRIX         = getOSMatches("Irix");
        private final boolean IS_OS_LINUX        = getOSMatches("Linux") || getOSMatches("LINUX");
        private final boolean IS_OS_MAC          = getOSMatches("Mac");
        private final boolean IS_OS_MAC_OSX      = getOSMatches("Mac OS X");
        private final boolean IS_OS_OS2          = getOSMatches("OS/2");
        private final boolean IS_OS_SOLARIS      = getOSMatches("Solaris");
        private final boolean IS_OS_SUN_OS       = getOSMatches("SunOS");
        private final boolean IS_OS_WINDOWS      = getOSMatches("Windows");
        private final boolean IS_OS_WINDOWS_2000 = getOSMatches("Windows", "5.0");
        private final boolean IS_OS_WINDOWS_95   = getOSMatches("Windows 9", "4.0");
        private final boolean IS_OS_WINDOWS_98   = getOSMatches("Windows 9", "4.1");
        private final boolean IS_OS_WINDOWS_ME   = getOSMatches("Windows", "4.9");
        private final boolean IS_OS_WINDOWS_NT   = getOSMatches("Windows NT");
        private final boolean IS_OS_WINDOWS_XP   = getOSMatches("Windows", "5.1");

        // ????file.encoding???????????????????????LocaleUtil????????
        private final String FILE_ENCODING  = LocaleUtil.getSystem().getCharset().name();
        private final String FILE_SEPARATOR = getSystemProperty("file.separator", false);
        private final String LINE_SEPARATOR = getSystemProperty("line.separator", false);
        private final String PATH_SEPARATOR = getSystemProperty("path.separator", false);

        /** ??????????? */
        private OsInfo() {
        }

        /**
         * ????OS???????????<code>os.arch</code>??
         * <p>
         * ???<code>"x86"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getArch() {
            return OS_ARCH;
        }

        /**
         * ????OS???????????<code>os.name</code>??
         * <p>
         * ???<code>"Windows XP"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getName() {
            return OS_NAME;
        }

        /**
         * ????OS???????????<code>os.version</code>??
         * <p>
         * ???<code>"5.1"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getVersion() {
            return OS_VERSION;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???AIX????<code>true</code>
         */
        public final boolean isAix() {
            return IS_OS_AIX;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???HP-UX????<code>true</code>
         */
        public final boolean isHpUx() {
            return IS_OS_HP_UX;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???IRIX????<code>true</code>
         */
        public final boolean isIrix() {
            return IS_OS_IRIX;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Linux????<code>true</code>
         */
        public final boolean isLinux() {
            return IS_OS_LINUX;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Mac????<code>true</code>
         */
        public final boolean isMac() {
            return IS_OS_MAC;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???MacOS X????<code>true</code>
         */
        public final boolean isMacOsX() {
            return IS_OS_MAC_OSX;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???OS2????<code>true</code>
         */
        public final boolean isOs2() {
            return IS_OS_OS2;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Solaris????<code>true</code>
         */
        public final boolean isSolaris() {
            return IS_OS_SOLARIS;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Sun OS????<code>true</code>
         */
        public final boolean isSunOS() {
            return IS_OS_SUN_OS;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Windows????<code>true</code>
         */
        public final boolean isWindows() {
            return IS_OS_WINDOWS;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Windows 2000????<code>true</code>
         */
        public final boolean isWindows2000() {
            return IS_OS_WINDOWS_2000;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Windows 95????<code>true</code>
         */
        public final boolean isWindows95() {
            return IS_OS_WINDOWS_95;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Windows 98????<code>true</code>
         */
        public final boolean isWindows98() {
            return IS_OS_WINDOWS_98;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Windows ME????<code>true</code>
         */
        public final boolean isWindowsME() {
            return IS_OS_WINDOWS_ME;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Windows NT????<code>true</code>
         */
        public final boolean isWindowsNT() {
            return IS_OS_WINDOWS_NT;
        }

        /**
         * ????OS????
         * <p>
         * ??????????<code>os.name</code>???Java???????????<code>false</code>
         * </p>
         *
         * @return ????OS???Windows XP????<code>true</code>
         */
        public final boolean isWindowsXP() {
            return IS_OS_WINDOWS_XP;
        }

        /**
         * ??OS???
         *
         * @param osNamePrefix OS????
         * @return ????????<code>true</code>
         */
        private final boolean getOSMatches(String osNamePrefix) {
            if (OS_NAME == null) {
                return false;
            }

            return OS_NAME.startsWith(osNamePrefix);
        }

        /**
         * ??OS???
         *
         * @param osNamePrefix    OS????
         * @param osVersionPrefix OS????
         * @return ????????<code>true</code>
         */
        private final boolean getOSMatches(String osNamePrefix, String osVersionPrefix) {
            if (OS_NAME == null || OS_VERSION == null) {
                return false;
            }

            return OS_NAME.startsWith(osNamePrefix) && OS_VERSION.startsWith(osVersionPrefix);
        }

        /**
         * ??OS????????????????<code>file.encoding</code>??
         * <p>
         * ?????????????JVM????/???????? ???<code>GBK</code>?
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getFileEncoding() {
            return FILE_ENCODING;
        }

        /**
         * ??OS?????????????????<code>file.separator</code>??
         * <p>
         * ???Unix?<code>"/"</code>?Windows?<code>"\\"</code>?
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getFileSeparator() {
            return FILE_SEPARATOR;
        }

        /**
         * ??OS????????????????<code>line.separator</code>??
         * <p>
         * ???Unix?<code>"\n"</code>?Windows?<code>"\r\n"</code>?
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getLineSeparator() {
            return LINE_SEPARATOR;
        }

        /**
         * ??OS????????????????<code>path.separator</code>??
         * <p>
         * ???Unix?<code>":"</code>?Windows?<code>";"</code>?
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getPathSeparator() {
            return PATH_SEPARATOR;
        }

        /**
         * ?OS??????????
         *
         * @return OS??????
         */
        @Override
        public final String toString() {
            StringBuilder buffer = new StringBuilder();

            append(buffer, "OS Arch:        ", getArch());
            append(buffer, "OS Name:        ", getName());
            append(buffer, "OS Version:     ", getVersion());
            append(buffer, "File Encoding:  ", getFileEncoding());
            append(buffer, "File Separator: ", getFileSeparator());
            append(buffer, "Line Separator: ", getLineSeparator());
            append(buffer, "Path Separator: ", getPathSeparator());

            return buffer.toString();
        }
    }

    /** ?????????? */
    public static final class UserInfo {
        private final String USER_NAME      = getSystemProperty("user.name", false);
        private final String USER_HOME      = getSystemProperty("user.home", false);
        private final String USER_DIR       = getSystemProperty("user.dir", false);
        private final String USER_LANGUAGE  = getSystemProperty("user.language", false);
        private final String USER_COUNTRY   = getSystemProperty("user.country", false) == null ? getSystemProperty(
                "user.region", false) : getSystemProperty("user.country", false);
        private final String JAVA_IO_TMPDIR = getSystemProperty("java.io.tmpdir", false);

        /** ??????????? */
        private UserInfo() {
        }

        /**
         * ???????????????????<code>user.name</code>??
         * <p>
         * ???<code>"admin"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getName() {
            return USER_NAME;
        }

        /**
         * ?????????home??????????<code>user.home</code>??
         * <p>
         * ???<code>"/home/admin"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getHomeDir() {
            return USER_HOME;
        }

        /**
         * ??????????????<code>user.dir</code>??
         * <p>
         * ???<code>"/home/admin/working"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.1
         */
        public final String getCurrentDir() {
            return USER_DIR;
        }

        /**
         * ??????????????<code>java.io.tmpdir</code>??
         * <p>
         * ???<code>"/tmp"</code>
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getTempDir() {
            return JAVA_IO_TMPDIR;
        }

        /**
         * ?????????????????????<code>user.language</code>??
         * <p>
         * ???<code>"zh"</code>?<code>"en"</code>?
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getLanguage() {
            return USER_LANGUAGE;
        }

        /**
         * ????????????????????????JDK1.4 <code>user.country</code>?JDK1.2
         * <code>user.region</code>??
         * <p>
         * ???<code>"CN"</code>?<code>"US"</code>?
         * </p>
         *
         * @return ?????????????Java??????????????<code>null</code>?
         * @since Java 1.2
         */
        public final String getCountry() {
            return USER_COUNTRY;
        }

        /**
         * ???????????????
         *
         * @return ??????????
         */
        @Override
        public final String toString() {
            StringBuilder buffer = new StringBuilder();

            append(buffer, "User Name:        ", getName());
            append(buffer, "User Home Dir:    ", getHomeDir());
            append(buffer, "User Current Dir: ", getCurrentDir());
            append(buffer, "User Temp Dir:    ", getTempDir());
            append(buffer, "User Language:    ", getLanguage());
            append(buffer, "User Country:     ", getCountry());

            return buffer.toString();
        }
    }

    /** ?????????? */
    public static final class HostInfo {
        private final String HOST_NAME;
        private final String HOST_ADDRESS;

        /** ??????????? */
        private HostInfo() {
            String hostName;
            String hostAddress;

            try {
                InetAddress localhost = InetAddress.getLocalHost();

                hostName = localhost.getHostName();
                hostAddress = localhost.getHostAddress();
            } catch (UnknownHostException e) {
                hostName = "localhost";
                hostAddress = "127.0.0.1";
            }

            HOST_NAME = hostName;
            HOST_ADDRESS = hostAddress;
        }

        /**
         * ??????????
         * <p>
         * ???<code>"webserver1"</code>
         * </p>
         *
         * @return ???
         */
        public final String getName() {
            return HOST_NAME;
        }

        /**
         * ??????????
         * <p>
         * ???<code>"192.168.0.1"</code>
         * </p>
         *
         * @return ????
         */
        public final String getAddress() {
            return HOST_ADDRESS;
        }

        /**
         * ???????????????
         *
         * @return ??????????
         */
        @Override
        public final String toString() {
            StringBuilder buffer = new StringBuilder();

            append(buffer, "Host Name:    ", getName());
            append(buffer, "Host Address: ", getAddress());

            return buffer.toString();
        }
    }

    /** ??????????<code>PrintWriter</code>?? */
    public static final void dumpSystemInfo() {
        dumpSystemInfo(new PrintWriter(System.out));
    }

    /**
     * ??????????<code>PrintWriter</code>??
     *
     * @param out <code>PrintWriter</code>???
     */
    public static final void dumpSystemInfo(PrintWriter out) {
        out.println("--------------");
        out.println(getJvmSpecInfo());
        out.println("--------------");
        out.println(getJvmInfo());
        out.println("--------------");
        out.println(getJavaSpecInfo());
        out.println("--------------");
        out.println(getJavaInfo());
        out.println("--------------");
        out.println(getJavaRuntimeInfo());
        out.println("--------------");
        out.println(getOsInfo());
        out.println("--------------");
        out.println(getUserInfo());
        out.println("--------------");
        out.println(getHostInfo());
        out.println("--------------");
        out.flush();
    }

    /**
     * ???????????Java???????????????<code>System.err</code>??????
     * <code>null</code>?
     *
     * @param name  ???
     * @param quiet ?????????????<code>System.err</code>?
     * @return ????<code>null</code>
     */
    private static String getSystemProperty(String name, boolean quiet) {
        try {
            return System.getProperty(name);
        } catch (SecurityException e) {
            if (!quiet) {
                System.err.println("Caught a SecurityException reading the system property '" + name
                                   + "'; the SystemUtil property value will default to null.");
            }

            return null;
        }
    }

    /**
     * ???<code>StringBuilder</code>?
     *
     * @param buffer  <code>StringBuilder</code>??
     * @param caption ??
     * @param value   ?
     */
    private static void append(StringBuilder buffer, String caption, String value) {
        buffer.append(caption).append(defaultIfNull(StringEscapeUtil.escapeJava(value), "[n/a]")).append("\n");
    }

    public static void main(String[] args) {
        dumpSystemInfo();

        Set<?> keys = System.getProperties().keySet();

        @SuppressWarnings("unchecked")
        List<String> list = createArrayList((Set<String>) keys);

        Collections.sort(list);

        for (String key : list) {
            String value = System.getProperty(key);

            System.out.println(key + " = " + defaultIfNull(StringEscapeUtil.escapeJava(value), "[n/a]"));
        }
    }
}

