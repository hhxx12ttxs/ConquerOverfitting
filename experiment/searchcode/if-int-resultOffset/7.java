int [] inMessage;
if (message.length % 7 == 0) {
inMessage = message;
} else {
int requiredMessageLength = message.length + (7 - message.length % 7);
int [] result = new int[encodedLength];

int resultOffset = 0;
int bitPosition = 6;

