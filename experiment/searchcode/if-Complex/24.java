package kpfu.terentyev.quantum.emulator.Gates;

import kpfu.terentyev.quantum.emulator.Complex;
public ControlledUGate (Complex [][] uMatrix) throws Exception{
if (uMatrix.length!=2 || (uMatrix.length==2 &amp;&amp; (uMatrix[0].length!=2 || uMatrix[1].length!=2))){

