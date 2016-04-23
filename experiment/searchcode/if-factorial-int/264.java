package ru.pavelnix;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Testing class factorial
 */
public class FactorialTest {

    /**
     * The factorial should not exist if the number is negative
     */
    @Test
    public void factorialShouldNotExistIfNumberNegative() {
        //Arrange
        Factorial factorial = new Factorial(-1);

        //Act
        boolean validate = factorial.getValidate();

        //Assert
        assertFalse(validate);
    }

    /**
     * The factorial should equals one if the number is zero
     */
    @Test
    public void factorialShouldOneIfNumberZero() {
        //Arrange
        Factorial factorial = new Factorial(0);

        //Act
        int valueOfFactorial = factorial.countFactorial();

        //Assert
        assertThat(valueOfFactorial, is(1));
    }

    /**
     * The factorial should be counted by the formula 1*2*..*N if the number more zero
     */
    @Test
    public void factorialShouldCountedIfNumberMoreZero() {
        //Arrange
        Factorial factorial = new Factorial(10);

        //Act
        int valueOfFactorial = factorial.countFactorial();

        //Assert
        assertThat(valueOfFactorial, is(3628800));
    }
}

