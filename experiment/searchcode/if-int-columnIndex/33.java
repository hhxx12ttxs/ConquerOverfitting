return columnIndex == 0;
}

public Object _getValue(int columnIndex)
{
if(columnIndex == 0) return getName();
else return &quot;&quot;;
}

public void _setValue(int columnIndex, Object object)
{
if(columnIndex == 0) setName((String)object);
}
}

