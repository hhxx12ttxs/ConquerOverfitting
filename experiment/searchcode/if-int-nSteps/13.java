package com.test;

public class ChildNSteps {

int countSteps(int n){

if (n<0){
return 0;
}
else if (n==0){
return 1;
}

return countSteps(n-1) + countSteps(n-2)+ countSteps(n-3);

