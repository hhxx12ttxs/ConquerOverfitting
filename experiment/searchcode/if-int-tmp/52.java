BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
int maxans=0;
int tmp=0;
int a = Integer.parseInt(br.readLine());
int c = Integer.parseInt(br.readLine());

tmp = a*(b+c);
if (maxans < tmp)   maxans = tmp;

