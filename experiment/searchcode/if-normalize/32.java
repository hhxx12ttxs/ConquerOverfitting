package com.atlassian.plugins.osgi.test.asm;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.PrivateKey;
import java.util.Set;

import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.EmptyVisitor;
import org.osgi.framework.Bundle;
import org.springframework.osgi.util.BundleDelegatingClassLoader;

import junit.framework.TestCase;

/**
 * @since version
 */
public class BundleClassVisitor extends EmptyVisitor
{
    private final Bundle bundle;
    private boolean isWiredTest;
    private boolean isTestClass;
    private boolean inITPackage;
    private Set<Class<?>> unitTests;
    private Set<Class<?>> itTests;
    private String normalClassName;
    private URL myUrl;
    private URL[] allUrls;

    public BundleClassVisitor(Bundle bundle, URL url, URL[] urls, Set<Class<?>> unitTests, Set<Class<?>> itTests)
    {
        this.bundle = bundle;
        this.unitTests = unitTests;
        this.itTests = itTests;
        this.myUrl = url;
        this.allUrls = urls;
        this.isWiredTest = false;
        this.isTestClass = false;
        this.inITPackage = false;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces)
    {
        this.normalClassName = normalize(name);

        if (normalClassName.startsWith("it."))
        {
            inITPackage = true;
        }

        isTestClass = normalize(superName).equals(TestCase.class.getName());

        if (!isTestClass)
        {
            isTestClass = isSublassOf(superName, TestCase.class.getName());
        }

    }

    @Override
    public void visitEnd()
    {
        if (!isWiredTest && isTestClass)
        {
            try
            {
                Class<?> theClass = bundle.loadClass(normalClassName);
                if (inITPackage)
                {
                    itTests.add(theClass);
                }
                else
                {
                    unitTests.add(theClass);
                }
            }
            catch (Throwable t)
            {
                System.err.println("Error loading class from bundle: " + bundle.getSymbolicName() + " class: " + normalClassName);
                System.err.println("is it in a split package?");
            }
        }
    }
    

    @Override
    public AnnotationVisitor visitAnnotation(String annoName, boolean isVisible)
    {
        String normalName = normalize(annoName);
        if (RunWith.class.getName().equals(normalName))
        {
            return new RunWithAnnotationVisitor();
        }

        return null;
    }

    @Override
    public FieldVisitor visitField(int i, String s, String s1, String s2, Object o)
    {
        return null;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions)
    {
        return new TestMethodVisitor();
    }

    private boolean isSublassOf(String superName, String classToExtend)
    {
        boolean isSubclass = false;

        if (normalize(superName).equals("java.lang.Object"))
        {
            return isSubclass;
        }

        String path = superName.replace('.', '/');

        InputStream is = null;
        try
        {
            URL superUrl = bundle.getEntry(path + ".class");
            if (null != superUrl)
            {
                is = superUrl.openStream();
            }

            if (null != is)
            {

                ClassReader classReader = new ClassReader(is);
                isSubclass = normalize(classReader.getSuperName()).equals(classToExtend);
                if (!isSubclass)
                {
                    isSubclass = isSublassOf(classReader.getSuperName(), classToExtend);
                }
            }
        }
        catch (Exception e)
        {
            //don't care
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }

        return isSubclass;
    }

    static String normalize(String name)
    {
        if (name == null)
        {
            return null;
        }

        if (name.startsWith("L") && name.endsWith(";"))
        {
            name = name.substring(1, name.length() - 1);
        }

        if (name.endsWith(".class"))
        {
            name = name.substring(0, name.length() - ".class".length());
        }

        return name.replace('/', '.');
    }

    private class RunWithAnnotationVisitor extends EmptyVisitor
    {

        @Override
        public void visit(String name, Object value)
        {
            if (value instanceof Type)
            {
                Type type = (Type) value;
                
                if (AtlassianPluginsTestRunner.class.getName().equals(normalize(type.getInternalName())))
                {
                    isWiredTest = true;
                }
            }
        }
    }

    private class TestMethodVisitor extends EmptyVisitor
    {
        @Override
        public AnnotationVisitor visitAnnotation(String name, boolean visible)
        {
            if (Test.class.getName().equals(normalize(name)))
            {
                isTestClass = true;
            }

            return null;
        }
    }
}

