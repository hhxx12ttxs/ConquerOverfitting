/**
*
*/
package com.samsoft.trueyes.core.domain;

import java.time.LocalDate;

/**
*
* If something is time bound effective.
*
* @author sambhav.jain
*
*/
public interface Effectivity {

LocalDate from();

LocalDate to();
}

