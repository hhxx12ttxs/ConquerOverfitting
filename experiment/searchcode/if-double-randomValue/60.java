lengthValue = Integer.valueOf(length);
}
if (lengthValue > 50) {
lengthValue = 50;
}
randomValue = RandomStringUtils.randomAlphabetic(lengthValue);
} else if (StringUtil.equals(typeClassName, &quot;Double&quot;) || StringUtil.equals(typeClassName, &quot;double&quot;)) {
randomValue = new Random().nextDouble();
} else if (StringUtil.equals(typeClassName, &quot;Float&quot;) || StringUtil.equals(typeClassName, &quot;float&quot;)) {

