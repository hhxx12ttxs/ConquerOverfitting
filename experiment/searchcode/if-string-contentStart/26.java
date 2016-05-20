/**   Copyright 2009 Jeroen Benckhuijsen
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.atlassian.confluence.ext.code;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.ext.code.descriptor.DescriptorFacade;
import com.atlassian.confluence.ext.code.languages.impl.LanguageDescriptorFacadeMock;
import com.atlassian.confluence.ext.code.languages.impl.LanguageRegistryImpl;
import com.atlassian.confluence.ext.code.render.ContentFormatter;
import com.atlassian.confluence.ext.code.themes.impl.ThemeDescriptorFacadeMock;
import com.atlassian.confluence.ext.code.themes.impl.ThemeRegistryImpl;
import com.atlassian.confluence.ext.code.util.Constants;
import com.atlassian.confluence.languages.LocaleManager;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.setup.settings.Settings;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.renderer.RenderContextOutputType;
import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.V2Renderer;
import com.atlassian.renderer.v2.V2SubRenderer;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * @author Jeroen Benckhuijsen Unit test-case for {@link NewCodeMacro}
 * 
 */
public final class NewCodeMacroTestCase extends TestCase {

    
    private NewCodeMacro newCodeMacro;
    
    private ContentFormatter contentFormatter;

    private LanguageRegistryImpl languageRegistry;
    
    private ThemeRegistryImpl themeRegistry;

    // Confluence objects
    @Mock
    private WebResourceManager webResourceManager;
    
    @Mock
    private SettingsManager settingsManager;
    
    @Mock
    private LocaleManager localeManager;

    @Mock
    private ConversionContext conversionContextWordOutputType;
    
    @Mock
    private Page page;
    
    @Mock
    private PageContext pageContext;
    
    @Mock
    private ConversionContext conversionContexDisplayOutputType;

    @Mock
    private DescriptorFacade descriptorFacade;

    @Mock
    private ConversionContext conversionContextPDFOutputType;

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception 
    {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        
        when(settingsManager.getGlobalSettings()).thenReturn(new Settings());
        when(descriptorFacade.listLocalization()).thenReturn(new ArrayList<String>());

        // Create the macro for testing
        newCodeMacro = new NewCodeMacro();
        newCodeMacro.setPdlEnabled(false);
        contentFormatter = new ContentFormatter();
        languageRegistry = new LanguageRegistryImpl();
        themeRegistry = new ThemeRegistryImpl();

        LanguageDescriptorFacadeMock langFacadeMock = new LanguageDescriptorFacadeMock();
        langFacadeMock.setupMock(languageRegistry);
        languageRegistry.afterPropertiesSet();

        ThemeDescriptorFacadeMock themeFacadeMock = new ThemeDescriptorFacadeMock();
        themeFacadeMock.setupMock(themeRegistry);
        themeRegistry.afterPropertiesSet();

        contentFormatter.setWebResourceManager(webResourceManager);
        contentFormatter.setLanguageRegistry(languageRegistry);
        contentFormatter.setThemeRegistry(themeRegistry);
        contentFormatter.setLocaleManager(localeManager);
        contentFormatter.setDescriptorFacade(descriptorFacade);
        contentFormatter.setSettingsManager(settingsManager);
        newCodeMacro.setContentFormatter(contentFormatter);

        SubRenderer subRenderer = new V2SubRenderer(new V2Renderer());
        newCodeMacro.setSubRenderer(subRenderer);
        
        page = new Page();
        pageContext = page.toPageContext();
        conversionContextWordOutputType = new DefaultConversionContext(pageContext)
        {
        	public String getOutputType()
            {
        		return RenderContextOutputType.WORD;
            }
        };

        conversionContextPDFOutputType = new DefaultConversionContext(pageContext)
        {
            public String getOutputType()
            {
                return RenderContextOutputType.PDF;
            }
        };
        
        conversionContexDisplayOutputType = new DefaultConversionContext(pageContext)
        {
        	public String getOutputType()
            {
        		return RenderContextOutputType.DISPLAY;
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception 
    {
    	newCodeMacro = null;
        contentFormatter = null;
        languageRegistry = null;
        themeRegistry = null;
        webResourceManager = null;
        settingsManager = null;
        localeManager = null;
        conversionContextWordOutputType = null;
        conversionContextPDFOutputType = null;
        page = null;
        pageContext = null;
        conversionContexDisplayOutputType = null;
        descriptorFacade = null;
        super.tearDown();
    }

    private static final String SH_START = "<script type=\"syntaxhighlighter\" class=\"";
    private static final String CSS_END = "\"><![CDATA[";
    private static final String SH_END = "]]></script>";
    private static final String PRE_START = "<pre class=";
    private static final String PRE_END = "</pre>";

    /**
     * Retrieve the CSS class from the output.
     * 
     * @param output
     *            The rendered output by the macro
     * @return The CSS class within the &lt;pre&gt; tag
     */
    private String getCssClass(final String output) {
        int shStart = output.indexOf(SH_START);
        int cssStart = shStart + SH_START.length();
        int cssEnd = output.indexOf(CSS_END, cssStart);

        return output.substring(cssStart, cssEnd);
    }

    /**
     * Retrieve the content from the output.
     * 
     * @param output
     *            The rendered output by the macro
     * @return The content within the output
     */
    private String getContent(final String output) {
        int shStart = output.indexOf(SH_START);
        int cssStart = shStart + SH_START.length();
        int cssEnd = output.indexOf(CSS_END, cssStart);
        int contentStart = cssEnd + CSS_END.length();
        int shEnd = output.indexOf(SH_END, contentStart);

        return output.substring(contentStart, shEnd);
    }

    /**
     * Test whether the &lt;script&gt; tag is written correctly.
     */
    public void testRenderedContent() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            int shStart = output.indexOf(SH_START);
            int contentStart = output.indexOf("CONTENT");
            int shEnd = output.indexOf(SH_END);

            assertTrue(shStart != -1);
            assertTrue(contentStart != -1);
            assertTrue(shEnd != -1);
            assertTrue(shStart < contentStart);
            assertTrue(contentStart < shEnd);
        } catch (MacroExecutionException e) {
        	 fail();
		}
    }

    /**
     * Test exporting of the content.
     */
    public void testExportPDFContent() throws MacroExecutionException {
        /*
         * As default, we expect Confluence to behave correctly with respect to
         * rendering of our exported content.
         */
        mockNormalRendering(true);

        Map<String, String> parameters = new HashMap<String, String>();

        String output = newCodeMacro.execute(parameters, "CONTENT", conversionContextPDFOutputType);
        assertTrue(output.contains(PRE_START));
        assertTrue(output.contains("CONTENT"));
        assertTrue(output.contains(PRE_END));
    }

    public void testExportWordContent() throws MacroExecutionException {
        mockNormalRendering(true);

        Map<String, String> parameters = new HashMap<String, String>();

        String output = newCodeMacro.execute(parameters, "CONTENT", conversionContextWordOutputType);
        assertTrue(output.contains(PRE_START));
        assertTrue(output.contains("CONTENT"));
        assertTrue(output.contains(PRE_END));

    }

    /**
     * NCODE-135 - Test leading white space is not trimmed
     */
    public void testLeadingWhiteSpaceNotTrim() {
        mockNormalRendering(false);

        String input = "\t\t\tFirst Line\t\r\n" + // Starting with white space
                "Second \t\tline\n" + // Special white spaces within a line
                "\r\n" + // Some ways to specify a newline
                "\r" + "\n" + "\t\r" + "\t\r\n" + "\t\n" + "Some Other Line\n" + // A
                // simple
                // line
                "             \n" + // An empty line
                "Ending with whitespace\n" + "            "; // Ending with
        // whitespace

        Map<String, String> parameters = new HashMap<String, String>();

        try {
            String output = newCodeMacro.execute(parameters, input, conversionContexDisplayOutputType);
            String content = getContent(output);
            assertEquals(input, content);
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test if a language will always be set.
     */
    public void testDefaultLanguage() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);
            assertTrue(cssClass.contains("brush: java"));
        } catch (MacroExecutionException e) {
            fail();
        }

        /*
         * Test for a specified default language
         */

        mockNormalRendering(false);

        parameters = new HashMap<String, String>();

        try {
            contentFormatter.updateDefaultLanguage("vbnet");
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);
            assertTrue(cssClass.contains("brush: vbnet"));
        } catch (MacroExecutionException e) {
            fail();
        } finally {
            contentFormatter.updateDefaultLanguage(null);
        }
    }

    /**
     * Test if a language will always be set.
     */
    public void testDefaultLanguageParameter() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("0", "vbnet");

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);
            assertTrue(cssClass.contains("brush: vbnet"));
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test whether we have support for the <i>none</i> language. This language
     * is supported by the original code plugin, but not a valid alias for the
     * <i>plain</i> formatter.
     */
    public void testNoneLanguage() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("lang", "none");

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);
            assertTrue(cssClass.contains("brush: plain"));
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test if a specified language will be honored.
     */
    public void testLanguageSpecified() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("lang", "vbnet");

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);

            assertTrue(cssClass.contains("brush: vbnet"));
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test if languages are not case sensitive.
     */
    public void testLanguageCase() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("lang", "Java");

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);

            assertTrue(cssClass.contains("brush: java"));
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test is unknown languages result in a MacroException.
     */
    public void testUnknownLanguage() {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("lang", "NO_LANG");

        String result = null;
        try {
            result = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            fail();
        } catch (MacroExecutionException e) {
            // Excepted
            assertNull(result);
        }
    }

    /**
     * Test if the language actionscript is supported, which differs in the
     * alias used by the previous code macro and the new code macro.
     */
    public void testLanguageActionscript() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("lang", "actionscript");

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);

            assertTrue(cssClass.contains("brush: actionscript3"));
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test if parameters are used correctly in the CSS style.
     */
    public void testParameters() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("collapse", "true");
        parameters.put("firstline", "5");
        parameters.put("linenumbers", "true");

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);

            assertTrue(cssClass.contains("collapse: true"));
            assertTrue(cssClass.contains("first-line: 5"));
            assertTrue(cssClass.contains("gutter: true"));
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test invalid values for parameters.
     */
    public void testInvalidParameters() {
        testInvalidParameter("collapse");
        testInvalidParameter("firstline");
        testInvalidParameter("linenumbers");
    }

    /**
     * Test a single invalid parameter.
     * 
     * @param parameter
     *            The parameter to test
     */
    private void testInvalidParameter(final String parameter)
    {
    	Map<String, String> parameters = new HashMap<String, String>();
    	parameters.put(parameter, "INVALID PARAMETER VALUE");
    	String result = null;
    	try {
    		result = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
    		fail();
    	} catch (MacroExecutionException e) {
    		// expected
    		assertNull(result);
    	}
    }

    /**
     * Test if the controls are shown when collapse is true.
     */
    public void testControlsOnCollapse() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("collapse", "true");

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);

            assertTrue(cssClass.contains("collapse: true"));
        } catch (MacroExecutionException e) {
            fail();
        }

        mockNormalRendering(false);

        parameters.put("collapse", "false");

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);

            assertTrue(cssClass.contains("collapse: false"));
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test if the toolbar isn't shown when we're exporting.
     */
    public void testControlsOnExport() {
        mockNormalRendering(true);

        Map<String, String> parameters = new HashMap<String, String>();

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContextWordOutputType);
            assertFalse(output.contains("DIV class=\"toolbar\""));
        } catch (MacroExecutionException e) {
            fail();
        }

        mockNormalRendering(true);

        parameters = new HashMap<String, String>();
        parameters.put("controls", "true");

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContextWordOutputType);
            assertFalse(output.contains("DIV class=\"toolbar\""));
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test if themes are applied.
     */
    public void testDefaultThemeing() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();

        try {
            newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
        } catch (MacroExecutionException e) {
            fail();
        }

    }

    /**
     * Test if the default theme layout is applied.
     * See NCODE-88
     */
    public void testDefaultThemeLayout() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("title", "Test title");

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);

            /*
             * Default Theme CSS is applied at the level of the panel.
             * CSS: border-color: grey;border-style: dashed;border-width: 1px;
             */
            String PANEL_START = "<div class=\"code panel\" style=\"";
            int panelStart = output.indexOf(PANEL_START);
            int cssStart = panelStart + PANEL_START.length();
            int cssEnd = output.indexOf("\">", cssStart);

            String cssClass = output.substring(cssStart, cssEnd);
            
            // Verify layout defaults for Confluence theme
            assertTrue(cssClass.contains("border-color: grey"));
            assertTrue(cssClass.contains("border-style: dashed"));
            assertTrue(cssClass.contains("border-width: 1px"));
            
            /*
             * Title layout is applied at the level of the title.
             * CSS: border-bottom-width: 1px;border-bottom-style: dashed;border-bottom-color: grey;background-color: lightGrey;
             */
            String TITLE_START = "<div class=\"codeHeader panelHeader\" style=\"";
            int titleStart = output.indexOf(TITLE_START);
            int titleCssStart = titleStart + TITLE_START.length();
            int titleCssEnd = output.indexOf("\">", titleCssStart);
            String titleCssClass = output.substring(titleCssStart, titleCssEnd);
            
            assertTrue(titleCssClass.contains("border-bottom-width: 1px"));
            assertTrue(titleCssClass.contains("border-bottom-style: dashed"));
            assertTrue(titleCssClass.contains("border-bottom-color: grey"));
            assertTrue(titleCssClass.contains("background-color: lightGrey"));
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test if themes are applied.
     */
    public void testThemeing() {
        mockNormalRendering(false, "eclipse");

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("theme", "eclipse");

        try {
            newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Test if the default theme is applied.
     */
    public void testDefaultTheme() {
        mockNormalRendering(false, "eclipse");

        Map<String, String> parameters = new HashMap<String, String>();

        try {
            contentFormatter.updateDefaultTheme("eclipse");
            newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
        } catch (MacroExecutionException e) {
            fail();
        } finally {
            contentFormatter.updateDefaultTheme(null);
        }
    }

    /**
     * Test if a invalid theme is handled correctly.
     */
    public void testInvalidTheme() {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("theme", "INVALID_THEME");

        String result = null;
        try {
            result = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            fail();
        } catch (MacroExecutionException e) {
            assertNull(result);
        }
    }

    /**
     * Test whether parameters are set correctly if not specified if the default
     * for the macro differs from the default of the Syntax Highlighter. This
     * concerns:
     * <ul>
     * <li>linenumbers</li>
     * <li>controls</li>
     * </ul>
     * 
     * See NCODE-39 for details.
     */
    public void testNonDefaultParameters() {
        mockNormalRendering(false);

        Map<String, String> parameters = new HashMap<String, String>();

        try {
            String output = newCodeMacro.execute(parameters, "CONTENT", conversionContexDisplayOutputType);
            String cssClass = getCssClass(output);

            assertTrue(cssClass.contains("gutter: false"));
        } catch (MacroExecutionException e) {
            fail();
        }
    }

    /**
     * Call the methods executed during normal rendering on the mock
     * implementations.
     * 
     * @param export
     *            Whether we're mocking an export or not
     */
    private void mockNormalRendering(final boolean export) {
        mockNormalRendering(export, "confluence");
    }

    /**
     * Call the methods executed during normal rendering on the mock
     * implementations.
     * 
     * @param export
     *            Whether we're mocking an export or not
     * @param theme
     *            The theme which will be used
     */
    private void mockNormalRendering(final boolean export, final String theme) {
        webResourceManager
                .requireResource("confluence.web.resources:jquery");
        webResourceManager.requireResource("confluence.web.resources:ajs");
        webResourceManager.requireResource(Constants.PLUGIN_KEY
                + ":syntaxhighlighter");
        webResourceManager.requireResource(Constants.PLUGIN_KEY
                + ":syntaxhighlighter-brushes");
        webResourceManager.requireResource(Constants.PLUGIN_KEY
                + ":sh-theme-" + theme);
        when(localeManager.getLocale(null)).thenReturn(new Locale("nl"));
        webResourceManager.requireResource(Constants.PLUGIN_KEY
                + ":syntaxhighlighter-lang-nl");

        if (export) {
            webResourceManager.requireResource(Constants.PLUGIN_KEY
                    + ":syntaxhighlighter-export");
        }
    }

}

