/**
 * Copyright 2007 - 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * For more information visit
 *         http://wiki.architecturerules.org/ and
 *         http://blog.architecturerules.org/
 */
package org.architecturerules.domain;


import junit.framework.TestCase;

import java.io.File;


/**
 * <p><code>SourceDirectory</code> Tester.</p>
 *
 * @author mikenereson
 */
public class SourceDirectoryTest extends TestCase {

    /**
     * <p>Instance of a source to test against.</p>
     */
    private SourceDirectory source;

    public SourceDirectoryTest(String name) {
        super(name);
    }

    /**
     * @see TestCase#setName(String)
     */
    @Override
    public void setUp()
            throws Exception {

        super.setUp();

        source = new SourceDirectory();
    }


    /**
     * @see TestCase#tearDown()
     */
    @Override
    public void tearDown()
            throws Exception {

        source = null;

        super.tearDown();
    }


    /**
     * <p>Test for {@link SourceDirectory#equals(Object)} </p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testEquals()
            throws Exception {

        SourceDirectory that;

        source.setPath("core/target/classes");
        that = new SourceDirectory("core/target/classes");

        assertTrue(source.equals(that));
        assertTrue(source.hashCode() == that.hashCode());

        that = new SourceDirectory("web/target/classes");

        assertFalse(source.equals(that));
        assertFalse(source.hashCode() == that.hashCode());
    }


    /**
     * <p>Test for {@link SourceDirectory#SourceDirectory(String)} and {@link SourceDirectory#SourceDirectory(String,boolean)}
     * </p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testInterestingConstructors()
            throws Exception {

        /**
         * Test SourceDirectory#SourceDirectory(String)
         */
        final char separator = File.separatorChar;

        final String path = "core&target&classes".replace('&', separator);

        source = new SourceDirectory(path);

        assertEquals(path, source.getPath());

        /**
         * Test SourceDirectory#SourceDirectory(String, boolean)
         */
        source = new SourceDirectory("core/target/classes", true);
        assertEquals(path, source.getPath());
        assertTrue(source.shouldThrowExceptionWhenNotFound());

        source = new SourceDirectory("core/target/classes", false);
        assertEquals(path, source.getPath());
        assertFalse(source.shouldThrowExceptionWhenNotFound());

        /**
         * Test SourceDirectory#SourceDirectory(String, String)
         */
        source = new SourceDirectory("core/target/classes", "ignore");
        assertEquals(path, source.getPath());
        assertFalse(source.shouldThrowExceptionWhenNotFound());

        source = new SourceDirectory("core/target/classes", "exception");
        assertEquals(path, source.getPath());
        assertTrue(source.shouldThrowExceptionWhenNotFound());
    }


    /**
     * <p>Test for {@link SourceDirectory#SourceDirectory(String)} and {@link SourceDirectory#SourceDirectory(String,boolean)}
     * </p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testInterestingConstructors_illegalArguments()
            throws Exception {

        try {

            source = new SourceDirectory("");
            fail("expected IllegalArgumentException");
        } catch (final Exception e) {

            assertTrue(e instanceof IllegalArgumentException);
        }

        try {

            source = new SourceDirectory(null);
            fail("expected IllegalArgumentException");
        } catch (final Exception e) {

            assertTrue(e instanceof IllegalArgumentException);
        }

        try {

            source = new SourceDirectory("", true);
            fail("expected IllegalArgumentException");
        } catch (final Exception e) {

            assertTrue(e instanceof IllegalArgumentException);
        }

        try {

            source = new SourceDirectory(null, true);
            fail("expected IllegalArgumentException");
        } catch (final Exception e) {

            assertTrue(e instanceof IllegalArgumentException);
        }

        try {

            source = new SourceDirectory("core/target/classes", "monkey");
            fail("expected IllegalArgumentException");
        } catch (final Exception e) {

            assertTrue(e instanceof IllegalArgumentException);
        }

        try {

            source = new SourceDirectory("core/target/classes", "");
            fail("expected IllegalArgumentException");
        } catch (final Exception e) {

            assertTrue(e instanceof IllegalArgumentException);
        }

        try {

            source = new SourceDirectory("core/target/classes", null);
            fail("expected IllegalArgumentException");
        } catch (final Exception e) {

            assertTrue(e instanceof IllegalArgumentException);
        }
    }


    /**
     * <p>Test for {@link SourceDirectory#setPath(String)} and {@link SourceDirectory#getPath()} </p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testSetGetPath_windows()
            throws Exception {

        if (isWindowsPlatform()) {

            source.setPath("core/target/classes");
            assertEquals("core\\target\\classes", source.getPath());

            source.setPath("core\\target\\classes");
            assertEquals("core\\target\\classes", source.getPath());
        }
    }


    /**
     * <p>Test for {@link SourceDirectory#setPath(String)} and {@link SourceDirectory#getPath()} </p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testSetGetPath_unix()
            throws Exception {

        if (isUnixBasedPlatform()) {

            source.setPath("core/target/classes");
            assertEquals("core/target/classes", source.getPath());

            source.setPath("core\\target\\classes");
            assertEquals("core/target/classes", source.getPath());
        }
    }


    /**
     * <p>Test for {@link SourceDirectory#setPath(String)} and {@link SourceDirectory#getPath()} </p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testSetGetPath_illegalArguments()
            throws Exception {

        final char separator = File.separatorChar;
        final String path = "core&target&classes".replace('&', separator);

        source.setPath(path);

        assertEquals(path, source.getPath());
    }


    /**
     * <p>Test for {@link SourceDirectory#setNotFound(String)} </p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testSetNotFound()
            throws Exception {

        /* check the initial state*/
        assertFalse(source.shouldThrowExceptionWhenNotFound());

        /* change value */
        source.setNotFound("exception");

        /* check ending state */
        assertTrue(source.shouldThrowExceptionWhenNotFound());

        /* check the initial state*/
        assertTrue(source.shouldThrowExceptionWhenNotFound());

        /* change value */
        source.setNotFound("ignore");

        /* check ending state */
        assertFalse(source.shouldThrowExceptionWhenNotFound());
    }


    /**
     * <p>Test for {@link SourceDirectory#setNotFound(String)} when illegal arguments are passed</p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testSetNotFound_illegalArguments()
            throws Exception {

        try {

            source.setNotFound("");
            fail("expected IllegalArgumentException");
        } catch (Exception e) {

            assertTrue(e instanceof IllegalArgumentException);
        }

        try {

            source.setNotFound(null);
            fail("expected IllegalArgumentException");
        } catch (Exception e) {

            assertTrue(e instanceof IllegalArgumentException);
        }

        try {

            source.setNotFound("http://www.72miles.com");
            fail("expected IllegalArgumentException");
        } catch (Exception e) {

            assertTrue(e instanceof IllegalArgumentException);
        }
    }


    /**
     * <p>Test for {@link SourceDirectory#setShouldThrowExceptionWhenNotFound(boolean)} </p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testSetShouldThrowExceptionWhenNotFound()
            throws Exception {

        source.setShouldThrowExceptionWhenNotFound(true);
        assertTrue(source.shouldThrowExceptionWhenNotFound());

        source.setShouldThrowExceptionWhenNotFound(false);
        assertFalse(source.shouldThrowExceptionWhenNotFound());
    }


    /**
     * <p>Test for {@link SourceDirectory#replaceBackslashForOS(String)} </p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testFixSlashForOS_windows()
            throws Exception {

        if (isWindowsPlatform()) {

            final String backslashed = source.replaceBackslashForOS("src\\main\\resources");

            final String forwardSlashed = source.replaceBackslashForOS("src/main/resources");

            assertEquals("src\\main\\resources", backslashed);
            assertEquals("src\\main\\resources", forwardSlashed);
        }
    }


    /**
     * <p>Test for {@link SourceDirectory#replaceBackslashForOS(String)} </p>
     *
     * @throws Exception when <code>SourceDirectory</code> throws and unexpected <code>Exception</code>
     */
    public void testFixSlashForOS_unix()
            throws Exception {

        if (isUnixBasedPlatform()) {

            final String backslashed = source.replaceBackslashForOS("src\\main\\resources");

            final String forwardSlashed = source.replaceBackslashForOS("src/main/resources");

            assertEquals("src/main/resources", backslashed);
            assertEquals("src/main/resources", forwardSlashed);
        }
    }


    private boolean isWindowsPlatform() {

        return !isUnixBasedPlatform();
    }


    private boolean isUnixBasedPlatform() {

        return File.separator.equals("/");
    }
}

