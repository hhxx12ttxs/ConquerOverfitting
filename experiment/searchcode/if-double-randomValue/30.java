probabilitiesSum = sum;
}

@Override
protected GENERATE_TYPE doNext()
{
double randomValue = random.nextUniform( 0, 1 );
for ( int i = 0; i < itemProbabilities.length; i++ )
{
Tuple2<Double,GENERATE_TYPE> item = itemProbabilities[i];
if ( randomValue < (item._1() / probabilitiesSum) )

