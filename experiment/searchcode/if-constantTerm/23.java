/**************************************************************************************************
 * Copyright (C) 2012  nextreamlabs                                                               *
 * This program is free software: you can redistribute it and/or modify                           *
 * it under the terms of the GNU General Public License as published by                           *
 * the Free Software Foundation, either version 3 of the License, or                              *
 * (at your option) any later version.                                                            *
 * This program is distributed in the hope that it will be useful,                                *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
 * GNU General Public License for more details.                                                   *
 * You should have received a copy of the GNU General Public License                              *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.                          *
 **************************************************************************************************/

package org.nextreamlabs.simplex.ui;

import org.nextreamlabs.simplex.model.component.ConstraintSign;

import java.math.BigDecimal;

/**
 * Contract for an UI
 */
public interface Ui {
  public Boolean start();
  public Boolean setConstraintSign(Integer constraintIndex, ConstraintSign sign);
  public Boolean setConstraintVariable(Integer constraintIndex, Integer id, BigDecimal coefficient);
  public Boolean setConstraintConstantTerm(Integer constraintIndex, BigDecimal constantTerm);
  public Boolean setObjfuncVariable(Integer id, BigDecimal coefficient);
  public Boolean setStatusText(String text);
}

