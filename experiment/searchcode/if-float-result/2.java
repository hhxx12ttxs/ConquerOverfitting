floatFormDataResult.addFloatFormDataResult(number);
if(floatFormDataResult.getMaxValue() != number) {
fail(&quot;FloatFormDataResult.addFloatFormDataResult&quot;);
Float maxNumber = new Float(100);
floatFormDataResult.addFloatFormDataResult(maxNumber);
if(floatFormDataResult.getMaxValue() != maxNumber) {

