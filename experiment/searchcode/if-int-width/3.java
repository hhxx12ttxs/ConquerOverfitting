package boxSize;

public class Box {
public Box (int l, int w){
length = l;
width = w;
if (length > maxLength) maxLength = length;
if (width > maxWidth) maxWidth = width;
}

public static int maxLength = 0;

