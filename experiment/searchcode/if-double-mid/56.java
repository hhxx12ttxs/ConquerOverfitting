} else {
return -pow(-x,n);
}
}
// x > 0, n > 0
double mid = pow(x,n/2);
mid = mid * mid;
if(n % 2 == 1){
mid = mid * x;
}
return mid;
}
}

