double x = getX();
double chiSquare = Math.pow(x, 2);

double root1 = mu
+ ((Math.pow(mu, 2)*chiSquare)/(2*lambda))
+ Math.pow(mu, 2)*Math.pow(chiSquare, 2));

double root2 = Math.pow(mu, 2)/root1;

double probRoot1 = mu/(mu+root1);

