int bottombuffer = 0;
int yoffset = 0;
int heightsegment = (getHeight() - bottombuffer - 5) / 3;
keywidth = (int)((float)width / (float)MIDIPiano.instance.num_keys);
int keywidthhalf = keywidth / 2;
int keywidthhalf2 = keywidth / 2;

if ((keywidthhalf * 2) < keywidth) keywidthhalf2++;

