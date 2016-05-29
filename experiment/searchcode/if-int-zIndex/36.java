private static void deepRecursion(String path,int zindex) {
zindex++;
System.out.println(getSpace(zindex)+path);
File file = new File(path);
System.out.println(getSpace(zindex)+f.getName());
}
}

}

private static String getSpace(int zindex) {
StringBuilder sb = new StringBuilder();

