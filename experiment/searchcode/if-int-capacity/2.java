return _capacity;
}

public void SetCapacity(int newCapacity) {
if (newCapacity == _capacity) {
newBuffer = new byte[newCapacity];
if (_capacity > 0) {
int len = _capacity;

