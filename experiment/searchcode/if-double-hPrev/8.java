double dby[] = new double[vocab_size];
double dhnext[];
double hprev[] = new double[hidden_size];

double hs[][] = new double[this.seq_lenght+1][this.hidden_size];
targets = Arrays.copyOfRange(data,this.p+1,this.p+this.seq_lenght+1);

if(n % 100 == 0) {
double sample[] = sample(this.hprev, inputs[0], 22);
System.out.println(&quot;&quot;);

