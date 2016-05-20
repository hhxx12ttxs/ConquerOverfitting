/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at LICENSE.html or
 * http://www.sun.com/cddl.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this License Header
 * Notice in each file.
 *
 * If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s): Alexandre (Shura) Iline. (shurymury@gmail.com)
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 */

package org.jemmy.input;

import org.jemmy.interfaces.*;
import org.jemmy.interfaces.Caret.Direction;

/**
 *
 * @author shura
 */
public abstract class AbstractCaretOwner implements CaretOwner {
    private double error;

    public double allowedError() {
        return error;
    }

    public void allowError(double error) {
        if(error < 0) {
            throw new IllegalArgumentException("Precision could not be less than 0");
        }
        this.error = error;
    }

    public void to(double position) {
        caret().to(new ToPosition(this, position, error));
    }

    public static class ToPosition implements Direction {

        private double error;
        private double value;
        private CaretOwner caret;

        public ToPosition(CaretOwner caret, double value, double allowedError) {
            this.caret = caret;
            this.value = value;
            this.error = allowedError;
        }

        public ToPosition(CaretOwner caret, double value) {
            this(caret, value, 0);
        }

        public int to() {
            double diff = diff();
            return (Math.abs(diff) <= error) ? 0 : ((diff > 0) ? 1 : -1);
        }

        @Override
        public String toString() {
            return "value == " + position() + " with " + error + " error";
        }

        protected double diff() {
            return position() - caret.position();
        }

        public CaretOwner getCaret() {
            return caret;
        }

        /**
         *
         * @return
         */
        protected double position() {
            return value;
        }
    }

}

