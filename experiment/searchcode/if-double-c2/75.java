Exception e = CoordinateUtil.getLastException();
if (e != null)  throw e;
Assert.assertNotNull(c2,&quot; a value must be returned&quot;);
Assert.assertTrue(c2.x.doubleValue() < 415729,&quot;x must be arround 415729 : &quot;+c2.x);
Assert.assertTrue(c2.y.doubleValue() < 2002669,&quot;y must be arround 2002669 : &quot;+c2.y);

