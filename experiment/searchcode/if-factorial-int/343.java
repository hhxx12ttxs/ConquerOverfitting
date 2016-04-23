package com.mike.service;

import com.mike.model.FactorialModel;
import org.springframework.stereotype.Service;

/**
 * Created by Michael Brennan on 9/11/15.
 */
@Service
public class FactorialService {

    public FactorialModel factorial(int i){
        return new FactorialModel(getFactorial(i));
    }

    private int getFactorial(int i){

        if(i == 0){
            return 0;
        }
        if (i == 1){
            return 1;
        }
        return i * getFactorial(i -1); //recursive...COOL!
    }
}

