ex.printStackTrace();
}
}

public double getScore(LinkedList<String> text) {
double pos_logsum, neg_logsum;
pos_logsum = Math.log(pos);
// System.out.println(pos_logsum+&quot; &quot;+neg_logsum);
double p = Math.exp(pos_logsum)
/ (Math.exp(pos_logsum) + Math.exp(neg_logsum));

