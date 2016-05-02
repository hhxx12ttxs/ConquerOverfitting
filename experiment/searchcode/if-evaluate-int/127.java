/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2009-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

/**
 * The type of hit count condition.
 *
 * @author Nathan Fiedler
 */
public enum HitCountConditionType {

    EQUAL {

        @Override
        public boolean evaluate(int user, int hit) {
            return user == hit;
        }
    },
    GREATER {

        @Override
        public boolean evaluate(int user, int hit) {
            return user < hit;
        }
    },
    MULTIPLE {

        @Override
        public boolean evaluate(int user, int hit) {
            return hit % user == 0;
        }
    };

    /**
     * Evaluates the condition to determine if it is satisfied.
     *
     * @param  user  user-specified value.
     * @param  hit   breakpoint hit count.
     * @return  true if satisfied, false otherwise.
     */
    public abstract boolean evaluate(int user, int hit);
}

