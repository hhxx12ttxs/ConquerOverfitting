Scanner sc = new Scanner(System.in);
String line = sc.nextLine();

char firstChar = line.charAt(0);
if (Character.isLowerCase(firstChar))
{
System.out.println(Character.toUpperCase(firstChar) + line.substring(1));
}
else
{
System.out.println(line);
}
}
}

