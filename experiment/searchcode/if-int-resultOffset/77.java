public byte[] getBytes(int count) {
byte[] result = new byte[count];
int resultOffset = 0;
int bufferCount = this._bufferEndIndex - this._bufferStartIndex;
while (resultOffset < count) {
int needCount = count - resultOffset;
this._buffer = this.func();

