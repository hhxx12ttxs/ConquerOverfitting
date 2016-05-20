/*
 * Copyright 2008 Jeremy Mawson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.instinct.internal.report.html;

import com.googlecode.instinct.internal.util.ParamChecker;

public final class CamelCaseNameFormatter {
    public String convertToSentence(final String camelCase) {
        ParamChecker.checkNotNull(camelCase);
        if (camelCase.trim().equals("")) {
            return camelCase;
        }
        final StringBuilder builder = new StringBuilder();
        final int indexOfLastDot = camelCase.lastIndexOf('.');
        boolean firstChar = true;
        for (final Character character : camelCase.substring(indexOfLastDot + 1).toCharArray()) {
            if (!firstChar && Character.isUpperCase(character)) {
                builder.append(" ");
                builder.append(Character.toLowerCase(character));
            } else {
                builder.append(character);
            }
            firstChar = false;
        }
        return builder.toString();
    }
}

