System.out.println(&quot;Please enter an amount of dog years:&quot;);
int dog_years = sc.nextInt();
int human_years = 0;
if (dog_years > 1) {
human_years += 13;
human_years += ((dog_years - 1) * (16/3));

