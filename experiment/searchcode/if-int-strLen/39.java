List<String> res = new LinkedList<String>();
int len = nums.length;
if (len == 0)
return res;
if (len == 1) {
res.add(&quot;&quot; + nums[0]);
return res;
}

int first = 0;
int last = 0;
int strlen = 0;
for (int i = 1; i < len; i++) {

