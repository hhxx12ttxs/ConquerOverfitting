package cs142.lab04;




public class Binary {

public int toBinary(int index, int value)
{
int y = value;
int x = y;
int a = x / 16;
x = x -(16*a);
int b = x / 8;

