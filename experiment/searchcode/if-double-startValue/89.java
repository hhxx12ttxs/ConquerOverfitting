/*******************************************************************************
 * JBoss, Home of Professional Open Source
 * Copyright 2010-2013, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *******************************************************************************/
package org.richfaces.tests.metamer.ftest.richInputNumberSlider;

import static org.richfaces.tests.metamer.ftest.webdriver.AttributeList.inputNumberSliderAttributes;
import static org.testng.Assert.assertEquals;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.spi.annotations.Page;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.support.FindBy;
import org.richfaces.tests.metamer.ftest.AbstractWebDriverTest;
import org.richfaces.tests.metamer.ftest.annotations.Inject;
import org.richfaces.tests.metamer.ftest.annotations.Use;
import org.richfaces.tests.metamer.ftest.webdriver.MetamerPage;
import org.richfaces.tests.metamer.ftest.webdriver.MetamerPage.WaitRequestType;
import org.richfaces.tests.page.fragments.impl.input.TextInputComponent.ClearType;
import org.richfaces.tests.page.fragments.impl.input.inputNumberSlider.RichFacesInputNumberSlider;

/**
 * Abstract test case for rich:inputNumberSlider.
 *
 * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 */
public abstract class AbstractSliderTest extends AbstractWebDriverTest {

    @Page
    MetamerPage page;
    //
    protected static final int DEFAULT_MAX_VALUE = 10;
    protected static final String DEFAULT_MAX_VALUE_STR = "10";
    protected static final int DEFAULT_MIN_VALUE = -10;
    protected static final String DEFAULT_MIN_VALUE_STR = "-10";
    //
    protected String[] correctNumbers = { "-10", "-5", "-1", "0", "1", "5", "10" };
    protected String[] smallNumbers = { "-11", "-15", "-100" };
    protected String[] bigNumbers = { "11", "15", "100" };
    protected String[] decimalNumbers = { "1.4999", "5.6", "7.0001", "-5.50001", "-9.9", "1.222e0", "0e0", "-5.50001e0" };
    //
    @Inject
    @Use(empty = true)
    protected String number;
    @Inject
    @Use(empty = true)
    protected Integer delay;
    //
    @FindBy(css = "span[id$=slider]")
    protected RichFacesInputNumberSlider slider;
    @FindBy(css = "span[id$=output]")
    protected WebElement output;

    protected Action moveWithSliderActionWithWaitRequest(final int pixels) {
        return new Action() {
            @Override
            public void perform() {
                MetamerPage.waitRequest(slider.getNumberSlider(), WaitRequestType.XHR)
                    .moveHandleToPointInTraceHorizontally(pixels);
            }
        };
    }

    public void testClickLeftArrow() {
        int startValue = slider.getInput().getIntValue();
        int clicks = 1;

        inputNumberSliderAttributes.set(InputNumberSliderAttributes.delay, 500);
        inputNumberSliderAttributes.set(InputNumberSliderAttributes.showArrows, Boolean.TRUE);

        MetamerPage.waitRequest(slider, WaitRequestType.XHR).decreaseWithArrows();

        Graphene.waitGui().until("Output was not updated.").element(output).text()
            .equalTo(String.valueOf(startValue - clicks));
    }

    public void testClickRightArrow() {
        int startValue = slider.getInput().getIntValue();
        int clicks = 1;

        inputNumberSliderAttributes.set(InputNumberSliderAttributes.delay, 500);
        inputNumberSliderAttributes.set(InputNumberSliderAttributes.showArrows, Boolean.TRUE);

        MetamerPage.waitRequest(slider, WaitRequestType.XHR).increaseWithArrows();

        Graphene.waitGui().until("Output was not updated.").element(output).text()
            .equalTo(String.valueOf(startValue + clicks));
    }

    public void testMoveWithSlider() {
        moveWithSliderActionWithWaitRequest(0).perform();
        assertEquals(slider.getInput().getStringValue(), "-10", "Input was not updated.");
        assertEquals(output.getText(), "-10", "Output was not updated.");

        moveWithSliderActionWithWaitRequest(35).perform();
        assertEquals(slider.getInput().getStringValue(), "-7", "Input was not updated.");
        assertEquals(output.getText(), "-7", "Output was not updated.");

        moveWithSliderActionWithWaitRequest(195).perform();
        assertEquals(slider.getInput().getStringValue(), "10", "Input was not updated.");
        assertEquals(output.getText(), "10", "Output was not updated.");
    }

    public void testTypeIntoInputBig() {
        typeToInputActionWithXHRWaitRequest(number).perform();

        assertEquals(output.getText(), DEFAULT_MAX_VALUE_STR, "Output was not updated.");
        assertEquals(slider.getInput().getIntValue(), DEFAULT_MAX_VALUE, "Input was not updated.");
    }

    public void testTypeIntoInputCorrect() {
        typeToInputActionWithXHRWaitRequest(number).perform();
        assertEquals(output.getText(), number, "Output was not updated.");
    }

    public void testTypeIntoInputDecimal() {
        typeToInputActionWithXHRWaitRequest(number).perform();

        Double newNumber = new Double(number);
        assertEquals(output.getText(), newNumber == 0 ? "0" : newNumber.toString(), "Output was not updated.");
        assertEquals(slider.getInput().getStringValue(), newNumber == 0 ? "0" : newNumber.toString(),
            "Input was not updated.");
    }

    public void testTypeIntoInputNotNumber() {
        typeToInputActionWithWaitRequest("RF 4", WaitRequestType.NONE).perform();

        assertEquals(output.getText(), "2", "Output should not be updated.");
        assertEquals(slider.getInput().getStringValue(), "2", "Input should not be updated.");
    }

    public void testTypeIntoInputSmall() {
        typeToInputActionWithXHRWaitRequest(number).perform();

        assertEquals(output.getText(), DEFAULT_MIN_VALUE_STR, "Output was not updated.");
        assertEquals(slider.getInput().getIntValue(), DEFAULT_MIN_VALUE, "Input was not updated.");
    }

    protected Action typeToInputActionWithWaitRequest(final String num, final WaitRequestType type) {
        return new Action() {
            @Override
            public void perform() {
                MetamerPage.waitRequest(slider.getInput().clear(ClearType.JS).fillIn(num), type).trigger("blur");
            }
        };
    }

    protected Action typeToInputActionWithXHRWaitRequest(final String num) {
        return typeToInputActionWithWaitRequest(num, WaitRequestType.XHR);
    }
}

