TbTest tbTest1=null;
if(this.tbTest!=null&amp;&amp;this.tbTest.getTestId()!=null)
tbTest1=gogoServiceImpl.getObjectByClazz(TbTest.class, this.tbTest.getTestId());
if(tbTest1==null)
tbTest1=new TbTest();
TbPracticetitle tbPracticetitle1=null;

