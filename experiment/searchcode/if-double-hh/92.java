/*
 *   Copyright (C) Christian Schulte, 2005-206
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions
 *   are met:
 *
 *     o Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     o Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *   INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *   AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 *   THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *   INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *   NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *   THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   $JOMC$
 *
 */
package org.jomc.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DateFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.URLResourceLoader;
import org.jomc.model.Argument;
import org.jomc.model.ArgumentType;
import org.jomc.model.Dependency;
import org.jomc.model.Implementation;
import org.jomc.model.InheritanceModel;
import org.jomc.model.Message;
import org.jomc.model.ModelObject;
import org.jomc.model.Modules;
import org.jomc.model.Multiplicity;
import org.jomc.model.Properties;
import org.jomc.model.Property;
import org.jomc.model.Specification;
import org.jomc.model.SpecificationReference;
import org.jomc.model.Specifications;
import org.jomc.model.Text;
import org.jomc.model.Texts;
import org.jomc.model.modlet.ModelHelper;
import org.jomc.modlet.Model;

/**
 * Base tool class.
 *
 * @author <a href="mailto:schulte2005@users.sourceforge.net">Christian Schulte</a>
 * @version $JOMC$
 */
public class JomcTool
{

    /** Listener interface. */
    public abstract static class Listener
    {

        /** Creates a new {@code Listener} instance. */
        public Listener()
        {
            super();
        }

        /**
         * Gets called on logging.
         *
         * @param level The level of the event.
         * @param message The message of the event or {@code null}.
         * @param throwable The throwable of the event or {@code null}.
         *
         * @throws NullPointerException if {@code level} is {@code null}.
         */
        public void onLog( final Level level, final String message, final Throwable throwable )
        {
            if ( level == null )
            {
                throw new NullPointerException( "level" );
            }
        }

    }

    /** Empty byte array. */
    private static final byte[] NO_BYTES =
    {
    };

    /** The prefix of the template location. */
    private static final String TEMPLATE_PREFIX =
        JomcTool.class.getPackage().getName().replace( '.', '/' ) + "/templates/";

    /**
     * Constant for the name of the template profile property specifying a parent template profile name.
     * @since 1.3
     */
    private static final String PARENT_TEMPLATE_PROFILE_PROPERTY_NAME = "parent-template-profile";

    /**
     * Constant for the name of the template profile property specifying the template encoding.
     * @since 1.3
     */
    private static final String TEMPLATE_ENCODING_PROFILE_PROPERTY_NAME = "template-encoding";

    /**
     * The default encoding to use for reading templates.
     * @since 1.3
     */
    private String defaultTemplateEncoding;

    /** The default template profile. */
    private String defaultTemplateProfile;

    /**
     * The log level events are logged at by default.
     * @see #getDefaultLogLevel()
     */
    private static final Level DEFAULT_LOG_LEVEL = Level.WARNING;

    /** The default log level. */
    private static volatile Level defaultLogLevel;

    /** The model of the instance. */
    private Model model;

    /** The {@code VelocityEngine} of the instance. */
    private VelocityEngine velocityEngine;

    /**
     * Flag indicating the default {@code VelocityEngine}.
     * @since 1.2.4
     */
    private boolean defaultVelocityEngine;

    /**
     * The location to search for templates in addition to searching the class path.
     * @since 1.2
     */
    private URL templateLocation;

    /** The encoding to use for reading files. */
    private String inputEncoding;

    /** The encoding to use for writing files. */
    private String outputEncoding;

    /**
     * The template parameters.
     * @since 1.2
     */
    private Map<String, Object> templateParameters;

    /** The template profile of the instance. */
    private String templateProfile;

    /** The indentation string of the instance. */
    private String indentation;

    /** The line separator of the instance. */
    private String lineSeparator;

    /** The listeners of the instance. */
    private List<Listener> listeners;

    /** The log level of the instance. */
    private Level logLevel;

    /**
     * The locale of the instance.
     * @since 1.2
     */
    private Locale locale;

    /** Cached indentation strings. */
    private volatile Reference<Map<String, String>> indentationCache;

    /**
     * Cached templates.
     * @since 1.3
     */
    private volatile Reference<Map<String, TemplateData>> templateCache;

    /**
     * Cached template profile context properties.
     * @since 1.3
     */
    private volatile Reference<Map<String, java.util.Properties>> templateProfileContextPropertiesCache;

    /**
     * Cached template profile properties.
     * @since 1.3
     */
    private volatile Reference<Map<String, java.util.Properties>> templateProfilePropertiesCache;

    /** Cached Java keywords. */
    private volatile Reference<Set<String>> javaKeywordsCache;

    /** Creates a new {@code JomcTool} instance. */
    public JomcTool()
    {
        super();
    }

    /**
     * Creates a new {@code JomcTool} instance taking a {@code JomcTool} instance to initialize the new instance with.
     *
     * @param tool The instance to initialize the new instance with.
     *
     * @throws NullPointerException if {@code tool} is {@code null}.
     * @throws IOException if copying {@code tool} fails.
     */
    public JomcTool( final JomcTool tool ) throws IOException
    {
        this();

        if ( tool == null )
        {
            throw new NullPointerException( "tool" );
        }

        this.indentation = tool.indentation;
        this.inputEncoding = tool.inputEncoding;
        this.lineSeparator = tool.lineSeparator;
        this.listeners = tool.listeners != null ? new CopyOnWriteArrayList<Listener>( tool.listeners ) : null;
        this.logLevel = tool.logLevel;
        this.model = tool.model != null ? tool.model.clone() : null;
        this.outputEncoding = tool.outputEncoding;
        this.defaultTemplateEncoding = tool.defaultTemplateEncoding;
        this.defaultTemplateProfile = tool.defaultTemplateProfile;
        this.templateProfile = tool.templateProfile;
        this.velocityEngine = tool.velocityEngine;
        this.defaultVelocityEngine = tool.defaultVelocityEngine;
        this.locale = tool.locale;
        this.templateParameters =
            tool.templateParameters != null
            ? Collections.synchronizedMap( new HashMap<String, Object>( tool.templateParameters ) )
            : null;

        this.templateLocation =
            tool.templateLocation != null ? new URL( tool.templateLocation.toExternalForm() ) : null;

    }

    /**
     * Gets the list of registered listeners.
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make
     * to the returned list will be present inside the object. This is why there is no {@code set} method for the
     * listeners property.</p>
     *
     * @return The list of registered listeners.
     *
     * @see #log(java.util.logging.Level, java.lang.String, java.lang.Throwable)
     */
    public List<Listener> getListeners()
    {
        if ( this.listeners == null )
        {
            this.listeners = new CopyOnWriteArrayList<Listener>();
        }

        return this.listeners;
    }

    /**
     * Gets the default log level events are logged at.
     * <p>The default log level is controlled by system property {@code org.jomc.tools.JomcTool.defaultLogLevel} holding
     * the log level to log events at by default. If that property is not set, the {@code WARNING} default is
     * returned.</p>
     *
     * @return The log level events are logged at by default.
     *
     * @see #getLogLevel()
     * @see Level#parse(java.lang.String)
     */
    public static Level getDefaultLogLevel()
    {
        if ( defaultLogLevel == null )
        {
            defaultLogLevel = Level.parse( System.getProperty( "org.jomc.tools.JomcTool.defaultLogLevel",
                                                               DEFAULT_LOG_LEVEL.getName() ) );

        }

        return defaultLogLevel;
    }

    /**
     * Sets the default log level events are logged at.
     *
     * @param value The new default level events are logged at or {@code null}.
     *
     * @see #getDefaultLogLevel()
     */
    public static void setDefaultLogLevel( final Level value )
    {
        defaultLogLevel = value;
    }

    /**
     * Gets the log level of the instance.
     *
     * @return The log level of the instance.
     *
     * @see #getDefaultLogLevel()
     * @see #setLogLevel(java.util.logging.Level)
     * @see #isLoggable(java.util.logging.Level)
     */
    public final Level getLogLevel()
    {
        if ( this.logLevel == null )
        {
            this.logLevel = getDefaultLogLevel();

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultLogLevelInfo", this.logLevel.getLocalizedName() ), null );
            }
        }

        return this.logLevel;
    }

    /**
     * Sets the log level of the instance.
     *
     * @param value The new log level of the instance or {@code null}.
     *
     * @see #getLogLevel()
     * @see #isLoggable(java.util.logging.Level)
     */
    public final void setLogLevel( final Level value )
    {
        this.logLevel = value;
    }

    /**
     * Checks if a message at a given level is provided to the listeners of the instance.
     *
     * @param level The level to test.
     *
     * @return {@code true}, if messages at {@code level} are provided to the listeners of the instance;
     * {@code false}, if messages at {@code level} are not provided to the listeners of the instance.
     *
     * @throws NullPointerException if {@code level} is {@code null}.
     *
     * @see #getLogLevel()
     * @see #setLogLevel(java.util.logging.Level)
     * @see #log(java.util.logging.Level, java.lang.String, java.lang.Throwable)
     */
    public boolean isLoggable( final Level level )
    {
        if ( level == null )
        {
            throw new NullPointerException( "level" );
        }

        return level.intValue() >= this.getLogLevel().intValue();
    }

    /**
     * Gets the Java package name of a specification.
     *
     * @param specification The specification to get the Java package name of.
     *
     * @return The Java package name of {@code specification} or {@code null}.
     *
     * @throws NullPointerException if {@code specification} is {@code null}.
     */
    public String getJavaPackageName( final Specification specification )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }

        return specification.getClazz() != null ? this.getJavaPackageName( specification.getClazz() ) : null;
    }

    /**
     * Gets the Java type name of a specification.
     *
     * @param specification The specification to get the Java type name of.
     * @param qualified {@code true}, to return the fully qualified type name (with package name prepended);
     * {@code false}, to return the short type name (without package name prepended).
     *
     * @return The Java type name of {@code specification} or {@code null}.
     *
     * @throws NullPointerException if {@code specification} is {@code null}.
     *
     * @see #getJavaPackageName(org.jomc.model.Specification)
     */
    public String getJavaTypeName( final Specification specification, final boolean qualified )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }

        if ( specification.getClazz() != null )
        {
            final StringBuilder typeName = new StringBuilder( specification.getClazz().length() );
            final String javaPackageName = this.getJavaPackageName( specification );

            if ( qualified && javaPackageName.length() > 0 )
            {
                typeName.append( javaPackageName ).append( '.' );
            }

            typeName.append( javaPackageName.length() > 0
                             ? specification.getClazz().substring( javaPackageName.length() + 1 )
                             : specification.getClazz() );

            return typeName.toString();
        }

        return null;
    }

    /**
     * Gets the Java class path location of a specification.
     *
     * @param specification The specification to return the Java class path location of.
     *
     * @return The Java class path location of {@code specification} or {@code null}.
     *
     * @throws NullPointerException if {@code specification} is {@code null}.
     *
     * @see #getJavaTypeName(org.jomc.model.Specification, boolean)
     * @see #getJavaClasspathLocation(java.lang.String, boolean)
     */
    public String getJavaClasspathLocation( final Specification specification )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }

        return specification.getClazz() != null
               ? ( this.getJavaClasspathLocation( this.getJavaTypeName( specification, true ), false ) )
               : null;

    }

    /**
     * Gets the Java package name of a specification reference.
     *
     * @param reference The specification reference to get the Java package name of.
     *
     * @return The Java package name of {@code reference} or {@code null}.
     *
     * @throws NullPointerException if {@code reference} is {@code null}.
     *
     * @see #getJavaPackageName(org.jomc.model.Specification)
     */
    public String getJavaPackageName( final SpecificationReference reference )
    {
        if ( reference == null )
        {
            throw new NullPointerException( "reference" );
        }

        Specification s = null;
        String javaPackageName = null;

        if ( this.getModules() != null
             && ( s = this.getModules().getSpecification( reference.getIdentifier() ) ) != null )
        {
            javaPackageName = s.getClazz() != null ? this.getJavaPackageName( s ) : null;
        }
        else if ( this.isLoggable( Level.WARNING ) )
        {
            this.log( Level.WARNING, getMessage( "specificationNotFound", reference.getIdentifier() ), null );
        }

        return javaPackageName;
    }

    /**
     * Gets the name of a Java type of a given specification reference.
     *
     * @param reference The specification reference to get a Java type name of.
     * @param qualified {@code true}, to return the fully qualified type name (with package name prepended);
     * {@code false}, to return the short type name (without package name prepended).
     *
     * @return The Java type name of {@code reference} or {@code null}.
     *
     * @throws NullPointerException if {@code reference} is {@code null}.
     *
     * @see #getJavaTypeName(org.jomc.model.Specification, boolean)
     */
    public String getJavaTypeName( final SpecificationReference reference, final boolean qualified )
    {
        if ( reference == null )
        {
            throw new NullPointerException( "reference" );
        }

        Specification s = null;
        String javaTypeName = null;

        if ( this.getModules() != null
             && ( s = this.getModules().getSpecification( reference.getIdentifier() ) ) != null )
        {
            javaTypeName = s.getClazz() != null ? this.getJavaTypeName( s, qualified ) : null;
        }
        else if ( this.isLoggable( Level.WARNING ) )
        {
            this.log( Level.WARNING, getMessage( "specificationNotFound", reference.getIdentifier() ), null );
        }

        return javaTypeName;
    }

    /**
     * Gets the Java package name of an implementation.
     *
     * @param implementation The implementation to get the Java package name of.
     *
     * @return The Java package name of {@code implementation} or {@code null}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     */
    public String getJavaPackageName( final Implementation implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        return implementation.getClazz() != null ? this.getJavaPackageName( implementation.getClazz() ) : null;
    }

    /**
     * Gets the Java type name of an implementation.
     *
     * @param implementation The implementation to get the Java type name of.
     * @param qualified {@code true}, to return the fully qualified type name (with package name prepended);
     * {@code false}, to return the short type name (without package name prepended).
     *
     * @return The Java type name of {@code implementation} or {@code null}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     *
     * @see #getJavaPackageName(org.jomc.model.Implementation)
     */
    public String getJavaTypeName( final Implementation implementation, final boolean qualified )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        if ( implementation.getClazz() != null )
        {
            final StringBuilder typeName = new StringBuilder( implementation.getClazz().length() );
            final String javaPackageName = this.getJavaPackageName( implementation );

            if ( qualified && javaPackageName.length() > 0 )
            {
                typeName.append( javaPackageName ).append( '.' );
            }

            typeName.append( javaPackageName.length() > 0
                             ? implementation.getClazz().substring( javaPackageName.length() + 1 )
                             : implementation.getClazz() );

            return typeName.toString();
        }

        return null;
    }

    /**
     * Gets the Java class path location of an implementation.
     *
     * @param implementation The implementation to return the Java class path location of.
     *
     * @return The Java class path location of {@code implementation} or {@code null}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     *
     * @see #getJavaClasspathLocation(java.lang.String, boolean)
     */
    public String getJavaClasspathLocation( final Implementation implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        return implementation.getClazz() != null
               ? ( this.getJavaClasspathLocation( this.getJavaTypeName( implementation, true ), false ) )
               : null;

    }

    /**
     * Gets a list of names of all Java types an implementation implements.
     *
     * @param implementation The implementation to get names of all implemented Java types of.
     * @param qualified {@code true}, to return the fully qualified type names (with package name prepended);
     * {@code false}, to return the short type names (without package name prepended).
     *
     * @return An unmodifiable list of names of all Java types implemented by {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     *
     * @since 1.2
     *
     * @see #getJavaTypeName(org.jomc.model.Specification, boolean)
     */
    public List<String> getImplementedJavaTypeNames( final Implementation implementation, final boolean qualified )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        List<String> col = null;

        if ( this.getModules() != null )
        {
            final Specifications specs = this.getModules().getSpecifications( implementation.getIdentifier() );
            col = new ArrayList<String>( specs == null ? 0 : specs.getSpecification().size() );

            if ( specs != null )
            {
                for ( int i = 0, s0 = specs.getSpecification().size(); i < s0; i++ )
                {
                    final Specification s = specs.getSpecification().get( i );

                    if ( s.getClazz() != null )
                    {
                        final String typeName = this.getJavaTypeName( s, qualified );

                        if ( !col.contains( typeName ) )
                        {
                            col.add( typeName );
                        }
                    }
                }
            }
        }
        else if ( this.isLoggable( Level.WARNING ) )
        {
            this.log( Level.WARNING, getMessage( "modulesNotFound", this.getModel().getIdentifier() ), null );
        }

        return Collections.unmodifiableList( col != null ? col : Collections.<String>emptyList() );
    }

    /**
     * Gets the Java type name of an argument.
     *
     * @param argument The argument to get the Java type name of.
     *
     * @return The Java type name of {@code argument}.
     *
     * @throws NullPointerException if {@code argument} is {@code null}.
     */
    public String getJavaTypeName( final Argument argument )
    {
        if ( argument == null )
        {
            throw new NullPointerException( "argument" );
        }

        String javaTypeName = "java.lang.String";

        if ( argument.getType() == ArgumentType.DATE || argument.getType() == ArgumentType.TIME )
        {
            javaTypeName = "java.util.Date";
        }
        else if ( argument.getType() == ArgumentType.NUMBER )
        {
            javaTypeName = "java.lang.Number";
        }

        return javaTypeName;
    }

    /**
     * Gets a Java method parameter name of an argument.
     *
     * @param argument The argument to get the Java method parameter name of.
     *
     * @return The Java method parameter name of {@code argument}.
     *
     * @throws NullPointerException if {@code argument} is {@code null}.
     *
     * @see #getJavaMethodParameterName(java.lang.String)
     *
     * @since 1.2
     */
    public String getJavaMethodParameterName( final Argument argument )
    {
        if ( argument == null )
        {
            throw new NullPointerException( "argument" );
        }

        return this.getJavaMethodParameterName( argument.getName() );
    }

    /**
     * Gets the Java type name of a property.
     *
     * @param property The property to get the Java type name of.
     * @param boxify {@code true}, to return the name of the Java wrapper class when the type is a Java primitive type;
     * {@code false}, to return the exact binary name (unboxed name) of the Java type.
     *
     * @return The Java type name of {@code property}.
     *
     * @throws NullPointerException if {@code property} is {@code null}.
     */
    public String getJavaTypeName( final Property property, final boolean boxify )
    {
        if ( property == null )
        {
            throw new NullPointerException( "property" );
        }

        if ( property.getType() != null )
        {
            final String typeName = property.getType();

            if ( boxify )
            {
                if ( Boolean.TYPE.getName().equals( typeName ) )
                {
                    return Boolean.class.getName();
                }
                if ( Byte.TYPE.getName().equals( typeName ) )
                {
                    return Byte.class.getName();
                }
                if ( Character.TYPE.getName().equals( typeName ) )
                {
                    return Character.class.getName();
                }
                if ( Double.TYPE.getName().equals( typeName ) )
                {
                    return Double.class.getName();
                }
                if ( Float.TYPE.getName().equals( typeName ) )
                {
                    return Float.class.getName();
                }
                if ( Integer.TYPE.getName().equals( typeName ) )
                {
                    return Integer.class.getName();
                }
                if ( Long.TYPE.getName().equals( typeName ) )
                {
                    return Long.class.getName();
                }
                if ( Short.TYPE.getName().equals( typeName ) )
                {
                    return Short.class.getName();
                }
            }

            return typeName;
        }

        return property.getAny() != null ? Object.class.getName() : String.class.getName();
    }

    /**
     * Gets a flag indicating the type of a given property is a Java primitive.
     *
     * @param property The property to query.
     *
     * @return {@code true}, if the Java type of {@code property} is primitive; {@code false}, if not.
     *
     * @throws NullPointerException if {@code property} is {@code null}.
     *
     * @see #getJavaTypeName(org.jomc.model.Property, boolean)
     */
    public boolean isJavaPrimitiveType( final Property property )
    {
        if ( property == null )
        {
            throw new NullPointerException( "property" );
        }

        return !this.getJavaTypeName( property, false ).equals( this.getJavaTypeName( property, true ) );
    }

    /**
     * Gets the name of a Java getter method of a given property.
     *
     * @param property The property to get a Java getter method name of.
     *
     * @return The Java getter method name of {@code property}.
     *
     * @throws NullPointerException if {@code property} is {@code null}.
     *
     * @see #getJavaIdentifier(java.lang.String, boolean)
     */
    public String getJavaGetterMethodName( final Property property )
    {
        if ( property == null )
        {
            throw new NullPointerException( "property" );
        }

        String prefix = "get";

        final String javaTypeName = this.getJavaTypeName( property, true );
        if ( Boolean.class.getName().equals( javaTypeName ) )
        {
            prefix = "is";
        }

        return prefix + this.getJavaIdentifier( property.getName(), true );
    }

    /**
     * Gets the name of a Java setter method of a given property.
     *
     * @param property The property to get a Java setter method name of.
     *
     * @return The Java setter method name of {@code property}.
     *
     * @throws NullPointerException if {@code property} is {@code null}.
     *
     * @see #getJavaIdentifier(java.lang.String, boolean)
     *
     * @since 1.2
     */
    public String getJavaSetterMethodName( final Property property )
    {
        if ( property == null )
        {
            throw new NullPointerException( "property" );
        }

        return "set" + this.getJavaIdentifier( property.getName(), true );
    }

    /**
     * Gets a Java method parameter name of a property.
     *
     * @param property The property to get the Java method parameter name of.
     *
     * @return The Java method parameter name of {@code property}.
     *
     * @throws NullPointerException if {@code property} is {@code null}.
     *
     * @see #getJavaMethodParameterName(java.lang.String)
     *
     * @since 1.2
     */
    public String getJavaMethodParameterName( final Property property )
    {
        if ( property == null )
        {
            throw new NullPointerException( "property" );
        }

        return this.getJavaMethodParameterName( property.getName() );
    }

    /**
     * Gets a Java field name of a property.
     *
     * @param property The property to get the Java field name of.
     *
     * @return The Java field name of {@code property}.
     *
     * @throws NullPointerException if {@code property} is {@code null}.
     *
     * @see #getJavaFieldName(java.lang.String)
     *
     * @since 1.3
     */
    public String getJavaFieldName( final Property property )
    {
        if ( property == null )
        {
            throw new NullPointerException( "property" );
        }

        return this.getJavaFieldName( property.getName() );
    }

    /**
     * Gets the name of a Java type of a given dependency.
     *
     * @param dependency The dependency to get a dependency Java type name of.
     *
     * @return The Java type name of {@code dependency} or {@code null}.
     *
     * @throws NullPointerException if {@code dependency} is {@code null}.
     *
     * @see #getJavaTypeName(org.jomc.model.Specification, boolean)
     */
    public String getJavaTypeName( final Dependency dependency )
    {
        if ( dependency == null )
        {
            throw new NullPointerException( "dependency" );
        }

        Specification s = null;
        String javaTypeName = null;

        if ( this.getModules() != null
             && ( s = this.getModules().getSpecification( dependency.getIdentifier() ) ) != null )
        {
            if ( s.getClazz() != null )
            {
                final StringBuilder typeName = new StringBuilder( s.getClazz().length() );
                typeName.append( this.getJavaTypeName( s, true ) );

                if ( s.getMultiplicity() == Multiplicity.MANY && dependency.getImplementationName() == null )
                {
                    typeName.append( "[]" );
                }

                javaTypeName = typeName.toString();
            }
        }
        else if ( this.isLoggable( Level.WARNING ) )
        {
            this.log( Level.WARNING, getMessage( "specificationNotFound", dependency.getIdentifier() ), null );
        }

        return javaTypeName;
    }

    /**
     * Gets the name of a Java getter method of a given dependency.
     *
     * @param dependency The dependency to get a Java getter method name of.
     *
     * @return The Java getter method name of {@code dependency}.
     *
     * @throws NullPointerException if {@code dependency} is {@code null}.
     *
     * @see #getJavaIdentifier(java.lang.String, boolean)
     */
    public String getJavaGetterMethodName( final Dependency dependency )
    {
        if ( dependency == null )
        {
            throw new NullPointerException( "dependency" );
        }

        return "get" + this.getJavaIdentifier( dependency.getName(), true );
    }

    /**
     * Gets the name of a Java setter method of a given dependency.
     *
     * @param dependency The dependency to get a Java setter method name of.
     *
     * @return The Java setter method name of {@code dependency}.
     *
     * @throws NullPointerException if {@code dependency} is {@code null}.
     *
     * @see #getJavaIdentifier(java.lang.String, boolean)
     *
     * @since 1.2
     */
    public String getJavaSetterMethodName( final Dependency dependency )
    {
        if ( dependency == null )
        {
            throw new NullPointerException( "dependency" );
        }

        return "set" + this.getJavaIdentifier( dependency.getName(), true );
    }

    /**
     * Gets a Java method parameter name of a dependency.
     *
     * @param dependency The dependency to get the Java method parameter name of.
     *
     * @return The Java method parameter name of {@code dependency}.
     *
     * @throws NullPointerException if {@code dependency} is {@code null}.
     *
     * @see #getJavaMethodParameterName(java.lang.String)
     *
     * @since 1.2
     */
    public String getJavaMethodParameterName( final Dependency dependency )
    {
        if ( dependency == null )
        {
            throw new NullPointerException( "dependency" );
        }

        return this.getJavaMethodParameterName( dependency.getName() );
    }

    /**
     * Gets a Java field name of a dependency.
     *
     * @param dependency The dependency to get the Java field name of.
     *
     * @return The Java field name of {@code dependency}.
     *
     * @throws NullPointerException if {@code dependency} is {@code null}.
     *
     * @see #getJavaFieldName(java.lang.String)
     *
     * @since 1.3
     */
    public String getJavaFieldName( final Dependency dependency )
    {
        if ( dependency == null )
        {
            throw new NullPointerException( "dependency" );
        }

        return this.getJavaFieldName( dependency.getName() );
    }

    /**
     * Gets the name of a Java getter method of a given message.
     *
     * @param message The message to get a Java getter method name of.
     *
     * @return The Java getter method name of {@code message}.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
     *
     * @see #getJavaIdentifier(java.lang.String, boolean)
     */
    public String getJavaGetterMethodName( final Message message )
    {
        if ( message == null )
        {
            throw new NullPointerException( "message" );
        }

        return "get" + this.getJavaIdentifier( message.getName(), true );
    }

    /**
     * Gets the name of a Java setter method of a given message.
     *
     * @param message The message to get a Java setter method name of.
     *
     * @return The Java setter method name of {@code message}.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
     *
     * @see #getJavaIdentifier(java.lang.String, boolean)
     *
     * @since 1.2
     */
    public String getJavaSetterMethodName( final Message message )
    {
        if ( message == null )
        {
            throw new NullPointerException( "message" );
        }

        return "set" + this.getJavaIdentifier( message.getName(), true );
    }

    /**
     * Gets a Java method parameter name of a message.
     *
     * @param message The message to get the Java method parameter name of.
     *
     * @return The Java method parameter name of {@code message}.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
     *
     * @see #getJavaMethodParameterName(java.lang.String)
     *
     * @since 1.2
     */
    public String getJavaMethodParameterName( final Message message )
    {
        if ( message == null )
        {
            throw new NullPointerException( "message" );
        }

        return this.getJavaMethodParameterName( message.getName() );
    }

    /**
     * Gets a Java field name of a message.
     *
     * @param message The message to get the Java field name of.
     *
     * @return The Java field name of {@code message}.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
     *
     * @see #getJavaFieldName(java.lang.String)
     *
     * @since 1.3
     */
    public String getJavaFieldName( final Message message )
    {
        if ( message == null )
        {
            throw new NullPointerException( "message" );
        }

        return this.getJavaFieldName( message.getName() );
    }

    /**
     * Gets the Java modifier name of a dependency of a given implementation.
     *
     * @param implementation The implementation declaring the dependency to get a Java modifier name of.
     * @param dependency The dependency to get a Java modifier name of.
     *
     * @return The Java modifier name of {@code dependency} of {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} or {@code dependency} is {@code null}.
     */
    public String getJavaModifierName( final Implementation implementation, final Dependency dependency )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( dependency == null )
        {
            throw new NullPointerException( "dependency" );
        }

        return "private";
    }

    /**
     * Gets the Java modifier name of a message of a given implementation.
     *
     * @param implementation The implementation declaring the message to get a Java modifier name of.
     * @param message The message to get a Java modifier name of.
     *
     * @return The Java modifier name of {@code message} of {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} or {@code message} is {@code null}.
     */
    public String getJavaModifierName( final Implementation implementation, final Message message )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( message == null )
        {
            throw new NullPointerException( "message" );
        }

        return "private";
    }

    /**
     * Gets the Java modifier name of a property of a given implementation.
     *
     * @param implementation The implementation declaring the property to get a Java modifier name of.
     * @param property The property to get a Java modifier name of.
     *
     * @return The Java modifier name of {@code property} of {@code implementation}.
     *
     * @throws NullPointerException if {@code implementation} or {@code property} is {@code null}.
     */
    public String getJavaModifierName( final Implementation implementation, final Property property )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }
        if ( property == null )
        {
            throw new NullPointerException( "property" );
        }

        String javaModifierName = "private";

        if ( this.getModules() != null )
        {
            final Properties specified = this.getModules().getSpecifiedProperties( implementation.getIdentifier() );

            if ( specified != null && specified.getProperty( property.getName() ) != null )
            {
                javaModifierName = "public";
            }
        }
        else if ( this.isLoggable( Level.WARNING ) )
        {
            this.log( Level.WARNING, getMessage( "modulesNotFound", this.getModel().getIdentifier() ), null );
        }

        return javaModifierName;
    }

    /**
     * Formats a text to a Javadoc comment.
     *
     * @param text The text to format to a Javadoc comment.
     * @param indentationLevel The indentation level of the comment.
     * @param linePrefix The text to prepend lines with.
     *
     * @return {@code text} formatted to a Javadoc comment.
     *
     * @throws NullPointerException if {@code text} or {@code linePrefix} is {@code null}.
     * @throws IllegalArgumentException if {@code indentationLevel} is negative.
     */
    public String getJavadocComment( final Text text, final int indentationLevel, final String linePrefix )
    {
        if ( text == null )
        {
            throw new NullPointerException( "text" );
        }
        if ( linePrefix == null )
        {
            throw new NullPointerException( "linePrefix" );
        }
        if ( indentationLevel < 0 )
        {
            throw new IllegalArgumentException( Integer.toString( indentationLevel ) );
        }

        BufferedReader reader = null;
        boolean suppressExceptionOnClose = true;

        try
        {
            String javadoc = "";

            if ( text.getValue() != null )
            {
                final String indent = this.getIndentation( indentationLevel );
                reader = new BufferedReader( new StringReader( text.getValue() ) );
                final StringBuilder builder = new StringBuilder( text.getValue().length() );

                String line;
                while ( ( line = reader.readLine() ) != null )
                {
                    builder.append( this.getLineSeparator() ).append( indent ).append( linePrefix ).
                        append( line.replaceAll( "\\/\\*\\*", "/*" ).replaceAll( "\\*/", "/" ) );

                }

                if ( builder.length() > 0 )
                {
                    javadoc =
                        builder.substring( this.getLineSeparator().length() + indent.length() + linePrefix.length() );

                    if ( !new MimeType( text.getType() ).match( "text/html" ) )
                    {
                        javadoc = StringEscapeUtils.escapeHtml( javadoc );
                    }
                }
            }

            suppressExceptionOnClose = false;
            return javadoc;
        }
        catch ( final MimeTypeParseException e )
        {
            throw new AssertionError( e );
        }
        catch ( final IOException e )
        {
            throw new AssertionError( e );
        }
        finally
        {
            try
            {
                if ( reader != null )
                {
                    reader.close();
                }
            }
            catch ( final IOException e )
            {
                if ( suppressExceptionOnClose )
                {
                    this.log( Level.SEVERE, getMessage( e ), e );
                }
                else
                {
                    throw new AssertionError( e );
                }
            }
        }
    }

    /**
     * Formats a text from a list of texts to a Javadoc comment.
     *
     * @param texts The list of texts to format to a Javadoc comment.
     * @param indentationLevel The indentation level of the comment.
     * @param linePrefix The text to prepend lines with.
     *
     * @return The text corresponding to the locale of the instance from the list of texts formatted to a Javadoc
     * comment.
     *
     * @throws NullPointerException if {@code texts} or {@code linePrefix} is {@code null}.
     * @throws IllegalArgumentException if {@code indentationLevel} is negative.
     *
     * @see #getLocale()
     *
     * @since 1.2
     */
    public String getJavadocComment( final Texts texts, final int indentationLevel, final String linePrefix )
    {
        if ( texts == null )
        {
            throw new NullPointerException( "texts" );
        }
        if ( linePrefix == null )
        {
            throw new NullPointerException( "linePrefix" );
        }
        if ( indentationLevel < 0 )
        {
            throw new IllegalArgumentException( Integer.toString( indentationLevel ) );
        }

        return this.getJavadocComment( texts.getText( this.getLocale().getLanguage() ), indentationLevel, linePrefix );
    }

    /**
     * Formats a string to a Java string with unicode escapes.
     *
     * @param str The string to format to a Java string or {@code null}.
     *
     * @return {@code str} formatted to a Java string or {@code null}.
     *
     * @see StringEscapeUtils#escapeJava(java.lang.String)
     */
    public String getJavaString( final String str )
    {
        return StringEscapeUtils.escapeJava( str );
    }

    /**
     * Formats a string to a Java class path location.
     *
     * @param str The string to format or {@code null}.
     * @param absolute {@code true} to return an absolute class path location; {@code false} to return a relative
     * class path location.
     *
     * @return {@code str} formatted to a Java class path location.
     *
     * @since 1.3
     */
    public String getJavaClasspathLocation( final String str, final boolean absolute )
    {
        String classpathLocation = null;

        if ( str != null )
        {
            classpathLocation = str.replace( '.', '/' );

            if ( absolute )
            {
                classpathLocation = "/" + classpathLocation;
            }
        }

        return classpathLocation;
    }

    /**
     * Formats a string to a Java identifier.
     *
     * @param str The string to format or {@code null}.
     * @param capitalize {@code true}, to return an identifier with the first character upper cased; {@code false}, to
     * return an identifier with the first character lower cased.
     *
     * @return {@code str} formatted to a Java identifier or {@code null}.
     *
     * @since 1.2
     */
    public String getJavaIdentifier( final String str, final boolean capitalize )
    {
        String identifier = null;

        if ( str != null )
        {
            final int len = str.length();
            final StringBuilder builder = new StringBuilder( len );
            boolean uc = capitalize;

            for ( int i = 0; i < len; i++ )
            {
                final char c = str.charAt( i );
                final String charString = Character.toString( c );

                if ( builder.length() > 0 )
                {
                    if ( Character.isJavaIdentifierPart( c ) )
                    {
                        builder.append( uc ? charString.toUpperCase( this.getLocale() ) : charString );
                        uc = false;
                    }
                    else
                    {
                        uc = true;
                    }
                }
                else
                {
                    if ( Character.isJavaIdentifierStart( c ) )
                    {
                        builder.append( uc ? charString.toUpperCase( this.getLocale() )
                                        : charString.toLowerCase( this.getLocale() ) );

                        uc = false;
                    }
                    else
                    {
                        uc = capitalize;
                    }
                }
            }

            identifier = builder.toString();

            if ( identifier.length() <= 0 && this.isLoggable( Level.WARNING ) )
            {
                this.log( Level.WARNING, getMessage( "invalidJavaIdentifier", str ), null );
            }
        }

        return identifier;
    }

    /**
     * Formats a string to a Java method parameter name.
     *
     * @param str The string to format or {@code null}.
     *
     * @return {@code str} formatted to a Java method parameter name or {@code null}.
     *
     * @since 1.3
     */
    public String getJavaMethodParameterName( final String str )
    {
        String methodParameterName = null;

        if ( str != null )
        {
            final int len = str.length();
            final StringBuilder builder = new StringBuilder( len );
            boolean uc = false;

            for ( int i = 0; i < len; i++ )
            {
                final char c = str.charAt( i );
                final String charString = Character.toString( c );

                if ( builder.length() > 0 )
                {
                    if ( Character.isJavaIdentifierPart( c ) )
                    {
                        builder.append( uc ? charString.toUpperCase( this.getLocale() ) : charString );
                        uc = false;
                    }
                    else
                    {
                        uc = true;
                    }
                }
                else if ( Character.isJavaIdentifierStart( c ) )
                {
                    builder.append( charString.toLowerCase( this.getLocale() ) );
                }
            }

            methodParameterName = builder.toString();

            if ( methodParameterName.length() <= 0 && this.isLoggable( Level.WARNING ) )
            {
                this.log( Level.WARNING, getMessage( "invalidJavaMethodParameterName", str ), null );
            }

            if ( this.getJavaKeywords().contains( methodParameterName ) )
            {
                methodParameterName = "_" + methodParameterName;
            }
        }

        return methodParameterName;
    }

    /**
     * Formats a string to a Java field name.
     *
     * @param str The string to format or {@code null}.
     *
     * @return {@code str} formatted to a Java field name or {@code null}.
     *
     * @since 1.3
     */
    public String getJavaFieldName( final String str )
    {
        String fieldName = null;

        if ( str != null )
        {
            final int len = str.length();
            final StringBuilder builder = new StringBuilder( len );
            boolean uc = false;

            for ( int i = 0; i < len; i++ )
            {
                final char c = str.charAt( i );
                final String charString = Character.toString( c );

                if ( builder.length() > 0 )
                {
                    if ( Character.isJavaIdentifierPart( c ) )
                    {
                        builder.append( uc ? charString.toUpperCase( this.getLocale() ) : charString );
                        uc = false;
                    }
                    else
                    {
                        uc = true;
                    }
                }
                else if ( Character.isJavaIdentifierStart( c ) )
                {
                    builder.append( charString.toLowerCase( this.getLocale() ) );
                }
            }

            fieldName = builder.toString();

            if ( fieldName.length() <= 0 && this.isLoggable( Level.WARNING ) )
            {
                this.log( Level.WARNING, getMessage( "invalidJavaFieldName", str ), null );
            }

            if ( this.getJavaKeywords().contains( fieldName ) )
            {
                fieldName = "_" + fieldName;
            }
        }

        return fieldName;
    }

    /**
     * Formats a string to a Java constant name.
     *
     * @param str The string to format or {@code null}.
     *
     * @return {@code str} formatted to a Java constant name or {@code null}.
     *
     * @since 1.3
     */
    public String getJavaConstantName( final String str )
    {
        String name = null;

        if ( str != null )
        {
            final int len = str.length();
            final StringBuilder builder = new StringBuilder( len );
            boolean separator = false;

            for ( int i = 0; i < len; i++ )
            {
                final char c = str.charAt( i );

                if ( builder.length() > 0 ? Character.isJavaIdentifierPart( c ) : Character.isJavaIdentifierStart( c ) )
                {
                    if ( builder.length() > 0 )
                    {
                        if ( !separator )
                        {
                            final char previous = builder.charAt( builder.length() - 1 );
                            separator = Character.isLowerCase( previous ) && Character.isUpperCase( c );
                        }

                        if ( separator )
                        {
                            builder.append( '_' );
                        }
                    }

                    builder.append( c );
                    separator = false;
                }
                else
                {
                    separator = true;
                }
            }

            name = builder.toString().toUpperCase( this.getLocale() );

            if ( name.length() <= 0 && this.isLoggable( Level.WARNING ) )
            {
                this.log( Level.WARNING, getMessage( "invalidJavaConstantName", str ), null );
            }
        }

        return name;
    }

    /**
     * Gets a flag indicating the class of a given specification is located in the Java default package.
     *
     * @param specification The specification to query.
     *
     * @return {@code true}, if the class of {@code specification} is located in the Java default package;
     * {@code false}, else.
     *
     * @throws NullPointerException if {@code specification} is {@code null}.
     */
    public boolean isJavaDefaultPackage( final Specification specification )
    {
        if ( specification == null )
        {
            throw new NullPointerException( "specification" );
        }

        return specification.getClazz() != null && this.getJavaPackageName( specification ).length() == 0;
    }

    /**
     * Gets a flag indicating the class of a given implementation is located in the Java default package.
     *
     * @param implementation The implementation to query.
     *
     * @return {@code true}, if the class of {@code implementation} is located in the Java default package;
     * {@code false}, else.
     *
     * @throws NullPointerException if {@code implementation} is {@code null}.
     */
    public boolean isJavaDefaultPackage( final Implementation implementation )
    {
        if ( implementation == null )
        {
            throw new NullPointerException( "implementation" );
        }

        return implementation.getClazz() != null && this.getJavaPackageName( implementation ).length() == 0;
    }

    /**
     * Formats a string to a HTML string with HTML entities.
     *
     * @param str The string to format to a HTML string with HTML entities or {@code null}.
     *
     * @return {@code str} formatted to a HTML string with HTML entities or {@code null}.
     *
     * @see StringEscapeUtils#escapeHtml(java.lang.String)
     *
     * @since 1.2
     */
    public String getHtmlString( final String str )
    {
        return StringEscapeUtils.escapeHtml( str );
    }

    /**
     * Formats a string to a XML string with XML entities.
     *
     * @param str The string to format to a XML string with XML entities or {@code null}.
     *
     * @return {@code str} formatted to a XML string with XML entities or {@code null}.
     *
     * @see StringEscapeUtils#escapeXml(java.lang.String)
     *
     * @since 1.2
     */
    public String getXmlString( final String str )
    {
        return StringEscapeUtils.escapeXml( str );
    }

    /**
     * Formats a string to a JavaScript string applying JavaScript string rules.
     *
     * @param str The string to format to a JavaScript string by applying JavaScript string rules or {@code null}.
     *
     * @return {@code str} formatted to a JavaScript string with JavaScript string rules applied or {@code null}.
     *
     * @see StringEscapeUtils#escapeJavaScript(java.lang.String)
     *
     * @since 1.2
     */
    public String getJavaScriptString( final String str )
    {
        return StringEscapeUtils.escapeJavaScript( str );
    }

    /**
     * Formats a string to a SQL string.
     *
     * @param str The string to format to a SQL string or {@code null}.
     *
     * @return {@code str} formatted to a SQL string or {@code null}.
     *
     * @see StringEscapeUtils#escapeSql(java.lang.String)
     *
     * @since 1.2
     */
    public String getSqlString( final String str )
    {
        return StringEscapeUtils.escapeSql( str );
    }

    /**
     * Formats a string to a CSV string.
     *
     * @param str The string to format to a CSV string or {@code null}.
     *
     * @return {@code str} formatted to a CSV string or {@code null}.
     *
     * @see StringEscapeUtils#escapeCsv(java.lang.String)
     *
     * @since 1.2
     */
    public String getCsvString( final String str )
    {
        return StringEscapeUtils.escapeCsv( str );
    }

    /**
     * Formats a {@code Boolean} to a string.
     *
     * @param b The {@code Boolean} to format to a string or {@code null}.
     *
     * @return {@code b} formatted to a string.
     *
     * @see #getLocale()
     *
     * @since 1.2
     */
    public String getBooleanString( final Boolean b )
    {
        final MessageFormat messageFormat = new MessageFormat( ResourceBundle.getBundle(
            JomcTool.class.getName().replace( '.', '/' ), this.getLocale() ).
            getString( b ? "booleanStringTrue" : "booleanStringFalse" ), this.getLocale() );

        return messageFormat.format( null );
    }

    /**
     * Gets the display language of a given language code.
     *
     * @param language The language code to get the display language of.
     *
     * @return The display language of {@code language}.
     *
     * @throws NullPointerException if {@code language} is {@code null}.
     */
    public String getDisplayLanguage( final String language )
    {
        if ( language == null )
        {
            throw new NullPointerException( "language" );
        }

        final Locale l = new Locale( language );
        return l.getDisplayLanguage( l );
    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The date of {@code calendar} formatted using a short format style pattern.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see DateFormat#SHORT
     */
    public String getShortDate( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return DateFormat.getDateInstance( DateFormat.SHORT, this.getLocale() ).format( calendar.getTime() );
    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The date of {@code calendar} formatted using a medium format style pattern.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see DateFormat#MEDIUM
     *
     * @since 1.2
     */
    public String getMediumDate( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return DateFormat.getDateInstance( DateFormat.MEDIUM, this.getLocale() ).format( calendar.getTime() );
    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The date of {@code calendar} formatted using a long format style pattern.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see DateFormat#LONG
     */
    public String getLongDate( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return DateFormat.getDateInstance( DateFormat.LONG, this.getLocale() ).format( calendar.getTime() );
    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The date of {@code calendar} formatted using an ISO-8601 format style.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see SimpleDateFormat yyyy-DDD
     *
     * @since 1.2
     */
    public String getIsoDate( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return new SimpleDateFormat( "yyyy-DDD", this.getLocale() ).format( calendar.getTime() );
    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The time of {@code calendar} formatted using a short format style pattern.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see DateFormat#SHORT
     */
    public String getShortTime( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return DateFormat.getTimeInstance( DateFormat.SHORT, this.getLocale() ).format( calendar.getTime() );
    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The time of {@code calendar} formatted using a medium format style pattern.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see DateFormat#MEDIUM
     *
     * @since 1.2
     */
    public String getMediumTime( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return DateFormat.getTimeInstance( DateFormat.MEDIUM, this.getLocale() ).format( calendar.getTime() );
    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The time of {@code calendar} formatted using a long format style pattern.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see DateFormat#LONG
     */
    public String getLongTime( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return DateFormat.getTimeInstance( DateFormat.LONG, this.getLocale() ).format( calendar.getTime() );
    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The time of {@code calendar} formatted using an ISO-8601 format style.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see SimpleDateFormat HH:mm
     *
     * @since 1.2
     */
    public String getIsoTime( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return new SimpleDateFormat( "HH:mm", this.getLocale() ).format( calendar.getTime() );
    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The date and time of {@code calendar} formatted using a short format style pattern.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see DateFormat#SHORT
     */
    public String getShortDateTime( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, this.getLocale() ).
            format( calendar.getTime() );

    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The date and time of {@code calendar} formatted using a medium format style pattern.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see DateFormat#MEDIUM
     *
     * @since 1.2
     */
    public String getMediumDateTime( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM, this.getLocale() ).
            format( calendar.getTime() );

    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The date and time of {@code calendar} formatted using a long format style pattern.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see DateFormat#LONG
     */
    public String getLongDateTime( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        return DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG, this.getLocale() ).
            format( calendar.getTime() );

    }

    /**
     * Formats a calendar instance to a string.
     *
     * @param calendar The calendar to format to a string.
     *
     * @return The date and time of {@code calendar} formatted using a ISO-8601 format style.
     *
     * @throws NullPointerException if {@code calendar} is {@code null}.
     *
     * @see SimpleDateFormat yyyy-MM-dd'T'HH:mm:ssZ
     *
     * @since 1.2
     */
    public String getIsoDateTime( final Calendar calendar )
    {
        if ( calendar == null )
        {
            throw new NullPointerException( "calendar" );
        }

        // JDK: As of JDK 7, "yyyy-MM-dd'T'HH:mm:ssXXX".
        return new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ", this.getLocale() ).format( calendar.getTime() );
    }

    /**
     * Gets a string describing the range of years for given calendars.
     *
     * @param start The start of the range.
     * @param end The end of the range.
     *
     * @return Formatted range of the years of {@code start} and {@code end} (e.g. {@code "start - end"}).
     *
     * @throws NullPointerException if {@code start} or {@code end} is {@code null}.
     */
    public String getYears( final Calendar start, final Calendar end )
    {
        if ( start == null )
        {
            throw new NullPointerException( "start" );
        }
        if ( end == null )
        {
            throw new NullPointerException( "end" );
        }

        final Format yearFormat = new SimpleDateFormat( "yyyy", this.getLocale() );
        final int s = start.get( Calendar.YEAR );
        final int e = end.get( Calendar.YEAR );
        final StringBuilder years = new StringBuilder();

        if ( s != e )
        {
            if ( s < e )
            {
                years.append( yearFormat.format( start.getTime() ) ).append( " - " ).
                    append( yearFormat.format( end.getTime() ) );

            }
            else
            {
                years.append( yearFormat.format( end.getTime() ) ).append( " - " ).
                    append( yearFormat.format( start.getTime() ) );

            }
        }
        else
        {
            years.append( yearFormat.format( start.getTime() ) );
        }

        return years.toString();
    }

    /**
     * Gets the model of the instance.
     *
     * @return The model of the instance.
     *
     * @see #getModules()
     * @see #setModel(org.jomc.modlet.Model)
     */
    public final Model getModel()
    {
        if ( this.model == null )
        {
            this.model = new Model();
            this.model.setIdentifier( ModelObject.MODEL_PUBLIC_ID );
        }

        return this.model;
    }

    /**
     * Sets the model of the instance.
     *
     * @param value The new model of the instance or {@code null}.
     *
     * @see #getModel()
     */
    public final void setModel( final Model value )
    {
        this.model = value;
    }

    /**
     * Gets the modules of the model of the instance.
     *
     * @return The modules of the model of the instance or {@code null}, if no modules are found.
     *
     * @see #getModel()
     * @see #setModel(org.jomc.modlet.Model)
     */
    public final Modules getModules()
    {
        return ModelHelper.getModules( this.getModel() );
    }

    /**
     * Gets the {@code VelocityEngine} of the instance.
     *
     * @return The {@code VelocityEngine} of the instance.
     *
     * @throws IOException if initializing a new velocity engine fails.
     *
     * @see #setVelocityEngine(org.apache.velocity.app.VelocityEngine)
     */
    public final VelocityEngine getVelocityEngine() throws IOException
    {
        if ( this.velocityEngine == null )
        {
            /** {@code LogChute} logging to the listeners of the tool. */
            class JomcLogChute implements LogChute
            {

                JomcLogChute()
                {
                    super();
                }

                public void init( final RuntimeServices runtimeServices ) throws Exception
                {
                }

                public void log( final int level, final String message )
                {
                    this.log( level, message, null );
                }

                public void log( final int level, final String message, final Throwable throwable )
                {
                    JomcTool.this.log( Level.FINEST, message, throwable );
                }

                public boolean isLevelEnabled( final int level )
                {
                    return isLoggable( Level.FINEST );
                }

            }

            final VelocityEngine engine = new VelocityEngine();
            engine.setProperty( RuntimeConstants.RUNTIME_REFERENCES_STRICT, Boolean.TRUE.toString() );
            engine.setProperty( RuntimeConstants.VM_ARGUMENTS_STRICT, Boolean.TRUE.toString() );
            engine.setProperty( RuntimeConstants.STRICT_MATH, Boolean.TRUE.toString() );
            engine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new JomcLogChute() );

            engine.setProperty( RuntimeConstants.RESOURCE_LOADER, "class" );
            engine.setProperty( "class.resource.loader.class", ClasspathResourceLoader.class.getName() );
            engine.setProperty( "class.resource.loader.cache", Boolean.TRUE.toString() );

            if ( this.getTemplateLocation() != null )
            {
                engine.setProperty( RuntimeConstants.RESOURCE_LOADER, "class,url" );
                engine.setProperty( "url.resource.loader.class", URLResourceLoader.class.getName() );
                engine.setProperty( "url.resource.loader.cache", Boolean.TRUE.toString() );
                engine.setProperty( "url.resource.loader.root", this.getTemplateLocation().toExternalForm() );
                engine.setProperty( "url.resource.loader.timeout", Integer.toString( 60000 ) );
            }

            this.velocityEngine = engine;
            this.defaultVelocityEngine = true;
        }

        return this.velocityEngine;
    }

    /**
     * Sets the {@code VelocityEngine} of the instance.
     *
     * @param value The new {@code VelocityEngine} of the instance or {@code null}.
     *
     * @see #getVelocityEngine()
     */
    public final void setVelocityEngine( final VelocityEngine value )
    {
        this.velocityEngine = value;
        this.defaultVelocityEngine = false;
    }

    /**
     * Gets a new velocity context used for merging templates.
     *
     * @return A new velocity context used for merging templates.
     *
     * @throws IOException if creating a new context instance fails.
     *
     * @see #getTemplateParameters()
     */
    public VelocityContext getVelocityContext() throws IOException
    {
        final Calendar now = Calendar.getInstance();
        final VelocityContext ctx =
            new VelocityContext( new HashMap<String, Object>( this.getTemplateParameters() ) );

        this.mergeTemplateProfileContextProperties( this.getTemplateProfile(), this.getLocale().getLanguage(), ctx );
        this.mergeTemplateProfileContextProperties( this.getTemplateProfile(), null, ctx );

        final Model clonedModel = this.getModel().clone();
        final Modules clonedModules = ModelHelper.getModules( clonedModel );
        assert clonedModules != null : "Unexpected missing modules for model '" + clonedModel.getIdentifier() + "'.";

        ctx.put( "model", clonedModel );
        ctx.put( "modules", clonedModules );
        ctx.put( "imodel", new InheritanceModel( clonedModules ) );
        ctx.put( "tool", this );
        ctx.put( "toolName", this.getClass().getName() );
        ctx.put( "toolVersion", getMessage( "projectVersion" ) );
        ctx.put( "toolUrl", getMessage( "projectUrl" ) );
        ctx.put( "calendar", now.getTime() );

        // JDK: As of JDK 7, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX".
        ctx.put( "now",
                 new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ", this.getLocale() ).format( now.getTime() ) );

        ctx.put( "year", new SimpleDateFormat( "yyyy", this.getLocale() ).format( now.getTime() ) );
        ctx.put( "month", new SimpleDateFormat( "MM", this.getLocale() ).format( now.getTime() ) );
        ctx.put( "day", new SimpleDateFormat( "dd", this.getLocale() ).format( now.getTime() ) );
        ctx.put( "hour", new SimpleDateFormat( "HH", this.getLocale() ).format( now.getTime() ) );
        ctx.put( "minute", new SimpleDateFormat( "mm", this.getLocale() ).format( now.getTime() ) );
        ctx.put( "second", new SimpleDateFormat( "ss", this.getLocale() ).format( now.getTime() ) );
        ctx.put( "timezone", new SimpleDateFormat( "Z", this.getLocale() ).format( now.getTime() ) );
        ctx.put( "shortDate", this.getShortDate( now ) );
        ctx.put( "mediumDate", this.getMediumDate( now ) );
        ctx.put( "longDate", this.getLongDate( now ) );
        ctx.put( "isoDate", this.getIsoDate( now ) );
        ctx.put( "shortTime", this.getShortTime( now ) );
        ctx.put( "mediumTime", this.getMediumTime( now ) );
        ctx.put( "longTime", this.getLongTime( now ) );
        ctx.put( "isoTime", this.getIsoTime( now ) );
        ctx.put( "shortDateTime", this.getShortDateTime( now ) );
        ctx.put( "mediumDateTime", this.getMediumDateTime( now ) );
        ctx.put( "longDateTime", this.getLongDateTime( now ) );
        ctx.put( "isoDateTime", this.getIsoDateTime( now ) );

        return ctx;
    }

    /**
     * Gets the template parameters of the instance.
     * <p>This accessor method returns a reference to the live map, not a snapshot. Therefore any modification you make
     * to the returned map will be present inside the object. This is why there is no {@code set} method for the
     * template parameters property.</p>
     *
     * @return The template parameters of the instance.
     *
     * @see #getVelocityContext()
     *
     * @since 1.2
     */
    public final Map<String, Object> getTemplateParameters()
    {
        if ( this.templateParameters == null )
        {
            this.templateParameters = Collections.synchronizedMap( new HashMap<String, Object>() );
        }

        return this.templateParameters;
    }

    /**
     * Gets the location to search for templates in addition to searching the class path.
     *
     * @return The location to search for templates in addition to searching the class path or {@code null}.
     *
     * @see #setTemplateLocation(java.net.URL)
     *
     * @since 1.2
     */
    public final URL getTemplateLocation()
    {
        return this.templateLocation;
    }

    /**
     * Sets the location to search for templates in addition to searching the class path.
     *
     * @param value The new location to search for templates in addition to searching the class path or {@code null}.
     *
     * @see #getTemplateLocation()
     *
     * @since 1.2
     */
    public final void setTemplateLocation( final URL value )
    {
        this.templateLocation = value;
        this.templateProfileContextPropertiesCache = null;
        this.templateProfilePropertiesCache = null;

        if ( this.defaultVelocityEngine )
        {
            this.setVelocityEngine( null );
        }
    }

    /**
     * Gets the default encoding used for reading templates.
     *
     * @return The default encoding used for reading templates.
     *
     * @see #setDefaultTemplateEncoding(java.lang.String)
     *
     * @since 1.3
     */
    public final String getDefaultTemplateEncoding()
    {
        if ( this.defaultTemplateEncoding == null )
        {
            this.defaultTemplateEncoding = getMessage( "buildSourceEncoding" );

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultTemplateEncoding", this.defaultTemplateEncoding ), null );
            }
        }

        return this.defaultTemplateEncoding;
    }

    /**
     * Sets the default encoding to use for reading templates.
     *
     * @param value The new default encoding to use for reading templates or {@code null}.
     *
     * @see #getDefaultTemplateEncoding()
     *
     * @since 1.3
     */
    public final void setDefaultTemplateEncoding( final String value )
    {
        this.defaultTemplateEncoding = value;
        this.templateCache = null;
    }

    /**
     * Gets the template encoding of a given template profile.
     *
     * @param tp The template profile to get the template encoding of.
     *
     * @return The template encoding of the template profile identified by {@code tp} or the default template encoding
     * if no such encoding is defined.
     *
     * @throws NullPointerException if {@code tp} is {@code null}.
     *
     * @see #getDefaultTemplateEncoding()
     *
     * @since 1.3
     */
    public final String getTemplateEncoding( final String tp )
    {
        if ( tp == null )
        {
            throw new NullPointerException( "tp" );
        }

        String te = null;

        try
        {
            te = this.getTemplateProfileProperties( tp ).getProperty( TEMPLATE_ENCODING_PROFILE_PROPERTY_NAME );
        }
        catch ( final IOException e )
        {
            if ( this.isLoggable( Level.SEVERE ) )
            {
                this.log( Level.SEVERE, getMessage( e ), e );
            }
        }

        return te != null ? te : this.getDefaultTemplateEncoding();
    }

    /**
     * Gets the encoding to use for reading files.
     *
     * @return The encoding to use for reading files.
     *
     * @see #setInputEncoding(java.lang.String)
     */
    public final String getInputEncoding()
    {
        if ( this.inputEncoding == null )
        {
            this.inputEncoding = new InputStreamReader( new ByteArrayInputStream( NO_BYTES ) ).getEncoding();

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultInputEncoding", this.inputEncoding ), null );
            }
        }

        return this.inputEncoding;
    }

    /**
     * Sets the encoding to use for reading files.
     *
     * @param value The new encoding to use for reading files or {@code null}.
     *
     * @see #getInputEncoding()
     */
    public final void setInputEncoding( final String value )
    {
        this.inputEncoding = value;
    }

    /**
     * Gets the encoding to use for writing files.
     *
     * @return The encoding to use for writing files.
     *
     * @see #setOutputEncoding(java.lang.String)
     */
    public final String getOutputEncoding()
    {
        if ( this.outputEncoding == null )
        {
            this.outputEncoding = new OutputStreamWriter( new ByteArrayOutputStream() ).getEncoding();

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultOutputEncoding", this.outputEncoding ), null );
            }
        }

        return this.outputEncoding;
    }

    /**
     * Sets the encoding to use for writing files.
     *
     * @param value The encoding to use for writing files or {@code null}.
     *
     * @see #getOutputEncoding()
     */
    public final void setOutputEncoding( final String value )
    {
        this.outputEncoding = value;
    }

    /**
     * Gets the default template profile.
     * <p>The default template profile is the implicit parent profile of any template profile not specifying a parent
     * template profile.</p>
     *
     * @return The default template profile.
     *
     *
     * @see #setDefaultTemplateProfile(java.lang.String)
     */
    public final String getDefaultTemplateProfile()
    {
        if ( this.defaultTemplateProfile == null )
        {
            this.defaultTemplateProfile = "jomc-java";
        }

        return this.defaultTemplateProfile;
    }

    /**
     * Sets the default template profile.
     *
     * @param value The new default template profile or {@code null}.
     *
     * @see #getDefaultTemplateProfile()
     */
    public final void setDefaultTemplateProfile( final String value )
    {
        this.defaultTemplateProfile = value;
    }

    /**
     * Gets the template profile of the instance.
     *
     * @return The template profile of the instance.
     *
     * @see #getDefaultTemplateProfile()
     * @see #setTemplateProfile(java.lang.String)
     */
    public final String getTemplateProfile()
    {
        if ( this.templateProfile == null )
        {
            this.templateProfile = this.getDefaultTemplateProfile();

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultTemplateProfile", this.templateProfile ), null );
            }
        }

        return this.templateProfile;
    }

    /**
     * Sets the template profile of the instance.
     *
     * @param value The new template profile of the instance or {@code null}.
     *
     * @see #getTemplateProfile()
     */
    public final void setTemplateProfile( final String value )
    {
        this.templateProfile = value;
    }

    /**
     * Gets the parent template profile of a given template profile.
     *
     * @param tp The template profile to get the parent template profile of.
     *
     * @return The parent template profile of the template profile identified by {@code tp}; the default template
     * profile, if no such parent template profile is defined; {@code null}, if {@code tp} denotes the default template
     * profile.
     *
     * @throws NullPointerException if {@code tp} is {@code null}.
     *
     * @see #getDefaultTemplateProfile()
     *
     * @since 1.3
     */
    public final String getParentTemplateProfile( final String tp )
    {
        if ( tp == null )
        {
            throw new NullPointerException( "tp" );
        }

        String parentTemplateProfile = null;

        try
        {
            parentTemplateProfile =
                this.getTemplateProfileProperties( tp ).getProperty( PARENT_TEMPLATE_PROFILE_PROPERTY_NAME );

        }
        catch ( final IOException e )
        {
            if ( this.isLoggable( Level.SEVERE ) )
            {
                this.log( Level.SEVERE, getMessage( e ), e );
            }
        }

        return parentTemplateProfile != null ? parentTemplateProfile
               : tp.equals( this.getDefaultTemplateProfile() ) ? null : this.getDefaultTemplateProfile();

    }

    /**
     * Gets the indentation string of the instance.
     *
     * @return The indentation string of the instance.
     *
     * @see #setIndentation(java.lang.String)
     */
    public final String getIndentation()
    {
        if ( this.indentation == null )
        {
            this.indentation = "    ";

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultIndentation",
                                                    StringEscapeUtils.escapeJava( this.indentation ) ), null );

            }
        }

        return this.indentation;
    }

    /**
     * Gets an indentation string for a given indentation level.
     *
     * @param level The indentation level to get an indentation string for.
     *
     * @return The indentation string for {@code level}.
     *
     * @throws IllegalArgumentException if {@code level} is negative.
     *
     * @see #getIndentation()
     */
    public final String getIndentation( final int level )
    {
        if ( level < 0 )
        {
            throw new IllegalArgumentException( Integer.toString( level ) );
        }

        Map<String, String> map = this.indentationCache == null ? null : this.indentationCache.get();

        if ( map == null )
        {
            map = new ConcurrentHashMap<String, String>( 8 );
            this.indentationCache = new SoftReference<Map<String, String>>( map );
        }

        final String key = this.getIndentation() + "|" + level;
        String idt = map.get( key );

        if ( idt == null )
        {
            final StringBuilder b = new StringBuilder( this.getIndentation().length() * level );

            for ( int i = level; i > 0; i-- )
            {
                b.append( this.getIndentation() );
            }

            idt = b.toString();
            map.put( key, idt );
        }

        return idt;
    }

    /**
     * Sets the indentation string of the instance.
     *
     * @param value The new indentation string of the instance or {@code null}.
     *
     * @see #getIndentation()
     */
    public final void setIndentation( final String value )
    {
        this.indentation = value;
    }

    /**
     * Gets the line separator of the instance.
     *
     * @return The line separator of the instance.
     *
     * @see #setLineSeparator(java.lang.String)
     */
    public final String getLineSeparator()
    {
        if ( this.lineSeparator == null )
        {
            this.lineSeparator = System.getProperty( "line.separator", "\n" );

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultLineSeparator",
                                                    StringEscapeUtils.escapeJava( this.lineSeparator ) ), null );

            }
        }

        return this.lineSeparator;
    }

    /**
     * Sets the line separator of the instance.
     *
     * @param value The new line separator of the instance or {@code null}.
     *
     * @see #getLineSeparator()
     */
    public final void setLineSeparator( final String value )
    {
        this.lineSeparator = value;
    }

    /**
     * Gets the locale of the instance.
     *
     * @return The locale of the instance.
     *
     * @see #setLocale(java.util.Locale)
     *
     * @since 1.2
     */
    public final Locale getLocale()
    {
        if ( this.locale == null )
        {
            this.locale = Locale.ENGLISH;

            if ( this.isLoggable( Level.CONFIG ) )
            {
                this.log( Level.CONFIG, getMessage( "defaultLocale", this.locale ), null );
            }
        }

        return this.locale;
    }

    /**
     * Sets the locale of the instance.
     *
     * @param value The new locale of the instance or {@code null}.
     *
     * @see #getLocale()
     *
     * @since 1.2
     */
    public final void setLocale( final Locale value )
    {
        this.locale = value;
    }

    /**
     * Gets a velocity template for a given name.
     * <p>This method searches templates at the following locations recursively in the shown order stopping whenever
     * a matching template is found.
     * <ol>
     *  <li><code>org/jomc/tools/templates/{@link #getTemplateProfile() profile}/{@link #getLocale() language}/<i>templateName</i></code></li>
     *  <li><code>org/jomc/tools/templates/{@link #getParentTemplateProfile(java.lang.String) parent profile}/{@link #getLocale() language}/<i>templateName</i></code></li>
     *  <li><code>org/jomc/tools/templates/{@link #getTemplateProfile() profile}/<i>templateName</i></code></li>
     *  <li><code>org/jomc/tools/templates/{@link #getParentTemplateProfile(java.lang.String) parent profile}/{@link #getLocale() language}/<i>templateName</i></code></li>
     * </ol></p>
     *
     * @param templateName The name of the template to get.
     *
     * @return The template matching {@code templateName}.
     *
     * @throws NullPointerException if {@code templateName} is {@code null}.
     * @throws FileNotFoundException if no such template is found.
     * @throws IOException if getting the template fails.
     *
     * @see #getTemplateProfile()
     * @see #getParentTemplateProfile(java.lang.String)
     * @see #getLocale()
     * @see #getTemplateEncoding(java.lang.String)
     * @see #getVelocityEngine()
     */
    public Template getVelocityTemplate( final String templateName ) throws FileNotFoundException, IOException
    {
        if ( templateName == null )
        {
            throw new NullPointerException( "templateName" );
        }

        return this.getVelocityTemplate( this.getTemplateProfile(), templateName );
    }

    /**
     * Notifies registered listeners.
     *
     * @param level The level of the event.
     * @param message The message of the event or {@code null}.
     * @param throwable The throwable of the event or {@code null}.
     *
     * @throws NullPointerException if {@code level} is {@code null}.
     *
     * @see #getListeners()
     * @see #isLoggable(java.util.logging.Level)
     */
    public void log( final Level level, final String message, final Throwable throwable )
    {
        if ( level == null )
        {
            throw new NullPointerException( "level" );
        }

        if ( this.isLoggable( level ) )
        {
            for ( int i = this.getListeners().size() - 1; i >= 0; i-- )
            {
                this.getListeners().get( i ).onLog( level, message, throwable );
            }
        }
    }

    private String getJavaPackageName( final String identifier )
    {
        if ( identifier == null )
        {
            throw new NullPointerException( "identifier" );
        }

        final int idx = identifier.lastIndexOf( '.' );
        return idx != -1 ? identifier.substring( 0, idx ) : "";
    }

    private Template findVelocityTemplate( final String location, final String encoding ) throws IOException
    {
        try
        {
            return this.getVelocityEngine().getTemplate( location, encoding );
        }
        catch ( final ResourceNotFoundException e )
        {
            if ( this.isLoggable( Level.FINER ) )
            {
                this.log( Level.FINER, getMessage( "templateNotFound", location ), null );
            }

            return null;
        }
        catch ( final ParseErrorException e )
        {
            String m = getMessage( e );
            m = m == null ? "" : " " + m;

            // JDK: As of JDK 6, "new IOException( message, cause )".
            throw (IOException) new IOException( getMessage( "invalidTemplate", location, m ) ).initCause( e );
        }
        catch ( final VelocityException e )
        {
            String m = getMessage( e );
            m = m == null ? "" : " " + m;

            // JDK: As of JDK 6, "new IOException( message, cause )".
            throw (IOException) new IOException( getMessage( "velocityException", location, m ) ).initCause( e );
        }
    }

    private java.util.Properties getTemplateProfileContextProperties( final String profileName, final String language )
        throws IOException
    {
        Map<String, java.util.Properties> map = this.templateProfileContextPropertiesCache == null
                                                ? null : this.templateProfileContextPropertiesCache.get();

        if ( map == null )
        {
            map = new ConcurrentHashMap<String, java.util.Properties>();
            this.templateProfileContextPropertiesCache = new SoftReference<Map<String, java.util.Properties>>( map );
        }

        final String key = profileName + "|" + language;
        java.util.Properties profileProperties = map.get( key );
        boolean suppressExceptionOnClose = true;

        if ( profileProperties == null )
        {
            InputStream in = null;
            URL url = null;
            profileProperties = new java.util.Properties();

            final String resourceName = TEMPLATE_PREFIX + profileName + ( language == null ? "" : "/" + language )
                                        + "/context.properties";

            try
            {
                url = this.getClass().getResource( "/" + resourceName );

                if ( url != null )
                {
                    in = url.openStream();

                    if ( this.isLoggable( Level.CONFIG ) )
                    {
                        this.log( Level.CONFIG, getMessage( "contextPropertiesFound", url.toExternalForm() ), null );
                    }

                    profileProperties.load( in );
                }
                else if ( this.getTemplateLocation() != null )
                {
                    if ( this.isLoggable( Level.CONFIG ) )
                    {
                        this.log( Level.CONFIG, getMessage( "contextPropertiesNotFound", resourceName ), null );
                    }

                    url = new URL( this.getTemplateLocation(), resourceName );
                    in = url.openStream();

                    if ( this.isLoggable( Level.CONFIG ) )
                    {
                        this.log( Level.CONFIG, getMessage( "contextPropertiesFound", url.toExternalForm() ), null );
                    }

                    profileProperties.load( in );
                }
                else if ( this.isLoggable( Level.CONFIG ) )
                {
                    this.log( Level.CONFIG, getMessage( "contextPropertiesNotFound", resourceName ), null );
                }

                suppressExceptionOnClose = false;
            }
            catch ( final FileNotFoundException e )
            {
                if ( this.isLoggable( Level.CONFIG ) )
                {
                    this.log( Level.CONFIG, getMessage( "contextPropertiesNotFound", url.toExternalForm() ), null );
                }
            }
            finally
            {
                map.put( key, profileProperties );

                try
                {
                    if ( in != null )
                    {
                        in.close();
                    }
                }
                catch ( final IOException e )
                {
                    if ( suppressExceptionOnClose )
                    {
                        this.log( Level.SEVERE, getMessage( e ), e );
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
        }

        return profileProperties;
    }

    private void mergeTemplateProfileContextProperties( final String profileName, final String language,
                                                        final VelocityContext velocityContext ) throws IOException
    {
        if ( profileName != null )
        {
            final java.util.Properties templateProfileProperties =
                this.getTemplateProfileContextProperties( profileName, language );

            for ( final Enumeration<?> e = templateProfileProperties.propertyNames(); e.hasMoreElements(); )
            {
                final String name = e.nextElement().toString();
                final String value = templateProfileProperties.getProperty( name );
                final String[] values = value.split( "\\|" );

                if ( !velocityContext.containsKey( name ) )
                {
                    final String className = values[0];

                    try
                    {
                        if ( values.length > 1 )
                        {
                            final Class<?> valueClass = Class.forName( className );
                            velocityContext.put( name,
                                                 valueClass.getConstructor( String.class ).newInstance( values[1] ) );
                        }
                        else if ( value.contains( "|" ) )
                        {
                            velocityContext.put( name, Class.forName( values[0] ).newInstance() );
                        }
                        else
                        {
                            velocityContext.put( name, value );
                        }
                    }
                    catch ( final InstantiationException ex )
                    {
                        // JDK: As of JDK 6, "new IOException( message, cause )".
                        throw (IOException) new IOException( getMessage(
                            "contextPropertiesException", profileName + ( language != null ? ", " + language : "" ) ) ).
                            initCause( ex );

                    }
                    catch ( final IllegalAccessException ex )
                    {
                        // JDK: As of JDK 6, "new IOException( message, cause )".
                        throw (IOException) new IOException( getMessage(
                            "contextPropertiesException", profileName + ( language != null ? ", " + language : "" ) ) ).
                            initCause( ex );

                    }
                    catch ( final InvocationTargetException ex )
                    {
                        // JDK: As of JDK 6, "new IOException( message, cause )".
                        throw (IOException) new IOException( getMessage(
                            "contextPropertiesException", profileName + ( language != null ? ", " + language : "" ) ) ).
                            initCause( ex );

                    }
                    catch ( final NoSuchMethodException ex )
                    {
                        // JDK: As of JDK 6, "new IOException( message, cause )".
                        throw (IOException) new IOException( getMessage(
                            "contextPropertiesException", profileName + ( language != null ? ", " + language : "" ) ) ).
                            initCause( ex );

                    }
                    catch ( final ClassNotFoundException ex )
                    {
                        // JDK: As of JDK 6, "new IOException( message, cause )".
                        throw (IOException) new IOException( getMessage(
                            "contextPropertiesException", profileName + ( language != null ? ", " + language : "" ) ) ).
                            initCause( ex );

                    }
                }
            }

            this.mergeTemplateProfileContextProperties( this.getParentTemplateProfile( profileName ), language,
                                                        velocityContext );

        }
    }

    private java.util.Properties getTemplateProfileProperties( final String profileName ) throws IOException
    {
        Map<String, java.util.Properties> map = this.templateProfilePropertiesCache == null
                                                ? null : this.templateProfilePropertiesCache.get();

        if ( map == null )
        {
            map = new ConcurrentHashMap<String, java.util.Properties>();
            this.templateProfilePropertiesCache = new SoftReference<Map<String, java.util.Properties>>( map );
        }

        java.util.Properties profileProperties = map.get( profileName );
        boolean suppressExceptionOnClose = true;

        if ( profileProperties == null )
        {
            InputStream in = null;
            profileProperties = new java.util.Properties();

            final String resourceName = TEMPLATE_PREFIX + profileName + "/profile.properties";
            URL url = null;

            try
            {
                url = this.getClass().getResource( "/" + resourceName );

                if ( url != null )
                {
                    in = url.openStream();

                    if ( this.isLoggable( Level.CONFIG ) )
                    {
                        this.log( Level.CONFIG, getMessage( "templateProfilePropertiesFound", url.toExternalForm() ),
                                  null );

                    }

                    profileProperties.load( in );
                }
                else if ( this.getTemplateLocation() != null )
                {
                    if ( this.isLoggable( Level.CONFIG ) )
                    {
                        this.log( Level.CONFIG, getMessage( "templateProfilePropertiesNotFound", resourceName ), null );
                    }

                    url = new URL( this.getTemplateLocation(), resourceName );
                    in = url.openStream();

                    if ( this.isLoggable( Level.CONFIG ) )
                    {
                        this.log( Level.CONFIG, getMessage( "templateProfilePropertiesFound", url.toExternalForm() ),
                                  null );

                    }

                    profileProperties.load( in );
                }
                else if ( this.isLoggable( Level.CONFIG ) )
                {
                    this.log( Level.CONFIG, getMessage( "templateProfilePropertiesNotFound", resourceName ), null );
                }

                suppressExceptionOnClose = false;
            }
            catch ( final FileNotFoundException e )
            {
                if ( this.isLoggable( Level.CONFIG ) )
                {
                    this.log( Level.CONFIG, getMessage( "templateProfilePropertiesNotFound", url.toExternalForm() ),
                              null );

                }
            }
            finally
            {
                map.put( profileName, profileProperties );

                try
                {
                    if ( in != null )
                    {
                        in.close();
                    }
                }
                catch ( final IOException e )
                {
                    if ( suppressExceptionOnClose )
                    {
                        this.log( Level.SEVERE, getMessage( e ), e );
                    }
                    else
                    {
                        throw e;
                    }
                }
            }
        }

        return profileProperties;
    }

    private Set<String> getJavaKeywords()
    {
        Reader in = null;
        Set<String> set = this.javaKeywordsCache == null ? null : this.javaKeywordsCache.get();

        try
        {
            if ( set == null )
            {
                in = new InputStreamReader( this.getClass().getResourceAsStream(
                    "/" + this.getClass().getPackage().getName().replace( ".", "/" ) + "/JavaKeywords.txt" ), "UTF-8" );

                set = new CopyOnWriteArraySet<String>( IOUtils.readLines( in ) );

                this.javaKeywordsCache = new SoftReference<Set<String>>( set );
            }
        }
        catch ( final IOException e )
        {
            throw new IllegalStateException( getMessage( e ), e );
        }
        finally
        {
            try
            {
                if ( in != null )
                {
                    in.close();
                }
            }
            catch ( final IOException e )
            {
                throw new IllegalStateException( getMessage( e ), e );
            }
        }

        return set;
    }

    private Template getVelocityTemplate( final String tp, final String tn ) throws IOException
    {
        Template template = null;

        if ( tp != null )
        {
            final String key = this.getLocale() + "|" + this.getTemplateProfile() + "|"
                               + this.getDefaultTemplateProfile() + "|" + tn;

            Map<String, TemplateData> map = this.templateCache == null
                                            ? null : this.templateCache.get();

            if ( map == null )
            {
                map = new ConcurrentHashMap<String, TemplateData>( 32 );
                this.templateCache = new SoftReference<Map<String, TemplateData>>( map );
            }

            TemplateData templateData = map.get( key );

            if ( templateData == null )
            {
                templateData = new TemplateData();

                if ( !StringUtils.EMPTY.equals( this.getLocale().getLanguage() ) )
                {
                    templateData.location = TEMPLATE_PREFIX + tp + "/" + this.getLocale().getLanguage() + "/" + tn;
                    templateData.template =
                        this.findVelocityTemplate( templateData.location, this.getTemplateEncoding( tp ) );

                }

                if ( templateData.template == null )
                {
                    templateData.location = TEMPLATE_PREFIX + tp + "/" + tn;
                    templateData.template =
                        this.findVelocityTemplate( templateData.location, this.getTemplateEncoding( tp ) );

                }

                if ( templateData.template == null )
                {
                    template = this.getVelocityTemplate( this.getParentTemplateProfile( tp ), tn );

                    if ( template == null )
                    {
                        map.put( key, new TemplateData() );
                        throw new FileNotFoundException( getMessage( "noSuchTemplate", tn ) );
                    }
                }
                else
                {
                    if ( this.isLoggable( Level.FINER ) )
                    {
                        this.log( Level.FINER, getMessage( "templateInfo", tn, templateData.location ), null );
                    }

                    template = templateData.template;
                    map.put( key, templateData );
                }
            }
            else if ( templateData.template == null )
            {
                throw new FileNotFoundException( getMessage( "noSuchTemplate", tn ) );
            }
            else
            {
                if ( this.isLoggable( Level.FINER ) )
                {
                    this.log( Level.FINER, getMessage( "templateInfo", tn, templateData.location ), null );
                }

                template = templateData.template;
            }
        }

        return template;
    }

    private static String getMessage( final String key, final Object... arguments )
    {
        return MessageFormat.format( ResourceBundle.getBundle(
            JomcTool.class.getName().replace( '.', '/' ) ).getString( key ), arguments );

    }

    private static String getMessage( final Throwable t )
    {
        return t != null ? t.getMessage() != null ? t.getMessage() : getMessage( t.getCause() ) : null;
    }

    /** @since 1.3 */
    private static class TemplateData
    {

        private String location;

        private Template template;

    }

}

