n_w += this.getCoOccurrence(g); //co-occurrence of this word with word g
}

double n_w_times_p_g=probability_g*((double)n_w) ;

if (n_w_times_p_g > 0.0001){ //estimate when denominator is too small
this.chiSquare = chiSquare;
}

public double getChiSquare(){
return chiSquare;
}

}

