if(lowerBound < 0)
upperBound += (1 + Math.abs(lowerBound));
else if(upperBound > lowerBound)
upperBound = (upperBound+1) - lowerBound;
}

number = random.nextDouble() * upperBound;

if(number < lowerBound)

