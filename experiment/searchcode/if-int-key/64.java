package crossocket;

class Cipher {

static void cipher(byte[] data, Integer key){
if(key!=null){
int nKey = key;
for(int i=0;i<data.length;i++){

