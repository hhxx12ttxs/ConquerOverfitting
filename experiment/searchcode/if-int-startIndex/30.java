int startindex;

if (args.length >= 1 &amp;&amp; args[0].equals(&quot;-D&quot;)) {
debug = true;
startindex = 0;
}
if (args.length >= startindex + 1) {
vm = new VirtualMachine();
vm.load(args[startindex]);

