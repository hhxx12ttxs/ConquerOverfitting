package com.jeasonzhao.commons.data;

import java.io.Serializable;

import com.jeasonzhao.commons.utils.Algorithms;
import com.jeasonzhao.commons.utils.DataTypes;

public class Cell implements Serializable
{
    private static final long serialVersionUID = 1L;
    private CellTypes cellType = CellTypes.Normal;
    private boolean visible = true;
    private int colIndex = 0;
    private int rowIndex = 0;
    private int rowSpan = 0;
    private int colSpan = 0;
    private String text = null;
    private Object cellValue = null;
    private DataTypes dataType = DataTypes.STRING;
    private Object valueForSort = null;
    private String extraText;
    private Object tag=null;
    public Cell()
    {
    }

    public Cell(Object value)
    {
        this.setValue(value);
    }

    public Cell(Object value,String strText)
    {
        this.setValue(value);
        this.setText(strText);
    }

    public Cell(Cell cell)
    {
        copyFrom(cell);
    }

    private void copyFrom(Cell cell)
    {
        if(null == cell)
        {
            return;
        }
        this.text = cell.text;
        this.cellValue = cell.cellValue;
        this.colIndex = cell.colIndex;
        this.rowIndex = cell.rowIndex;
        this.rowSpan = cell.rowSpan;
        this.colSpan = cell.colSpan;
        this.dataType = cell.dataType;
        this.cellType = cell.cellType;
        this.visible = cell.visible;
        this.valueForSort = cell.valueForSort;
        this.extraText = cell.extraText;
    }

    public Cell cleanSpans()
    {
        this.colSpan = 0;
        this.rowSpan = 0;
        return this;
    }

    public CellTypes getCellType()
    {
        return this.cellType;
    }

    public String toString()
    {
        return this.getText();
    }

    public String getExtraText()
    {
        return extraText;
    }

    public Cell setCellType(CellTypes cellType)
    {
        this.cellType = cellType;
        return this;
    }

    public Cell setExtraText(String extraText)
    {
        this.extraText = extraText;
        return this;
    }

    public int getColIndex()
    {
        return colIndex;
    }

    public int getColSpan()
    {
        return colSpan;
    }

    public Object getValue()
    {
        return cellValue;
    }

    public int getRowSpan()
    {
        return rowSpan;
    }

    public int getRowIndex()
    {
        return rowIndex;
    }

    public Cell setValue(Object v)
    {
        if(null != v && v instanceof Cell)
        {
            this.copyFrom((Cell) v);
        }
        else
        {
            this.setValue(v);
        }
        if(null != this.cellValue)
        {
            this.setDataType(DataTypes.from(this.cellValue.getClass()));
        }
        return this;
    }

    public Object getValueForSort()
    {
        return this.valueForSort;
    }

    public String getRealSettingText()
    {
        return this.text;
    }

    public String getText()
    {
        if(null == text && null != cellValue)
        {
            return cellValue.toString();
        }
        else
        {
            return text;
        }
    }

    public Object getTag()
    {
        return tag;
    }

    public Cell setText(String Text)
    {
        this.text = Text;
        return this;
    }

    public Cell setRowSpan(int RowSpan)
    {
        this.rowSpan = RowSpan;
        return this;
    }

    public Cell setRowIndex(int RowIndex)
    {
        this.rowIndex = RowIndex;
        return this;
    }

    public Cell setColIndex(int ColIndex)
    {
        this.colIndex = ColIndex;
        return this;
    }

    public Range getRange()
    {
        return this.getPosition().createRange(this.rowSpan,this.colSpan);
    }

    public DataTypes getDataType()
    {
        return dataType;
    }

    public Cell setDataType(DataTypes DataType)
    {
        this.dataType = DataType;
        return this;
    }

    public Cell setPosition(int nRow,int nCol)
    {
        this.rowIndex = nRow;
        this.colIndex = nCol;
        return this;
    }

    public Position getPosition()
    {
        return new Position(this.rowIndex,this.colIndex);
    }

    public Cell setPosition(Position p)
    {
        this.rowIndex = null == p ? 0 : p.getRowIndex();
        this.colIndex = null == p ? 0 : p.getColIndex();
        return this;
    }

    public Cell setColSpan(int colSpan)
    {
        this.colSpan = colSpan;
        return this;
    }

    public void setTag(Object tag)
    {
        this.tag = tag;
    }

    public boolean isVisible()
    {
        return this.visible;
    }

    public Cell setVisible(boolean v)
    {
        this.visible = v;
        return this;
    }

    public Cell setValueForSort(Object obj)
    {
        this.valueForSort = obj;
        return this;
    }

    public void clearData()
    {
        this.cellValue = null;
        this.text = null;
        this.valueForSort = null;
    }

    public int compareTo(Cell cell)
    {
        if(null == cell)
        {
            return -1;
        }
        else if(null == this.getValue() || null == cell.getValue())
        {
            return null == this.getValue() ? -1 : 1;
        }
        else
        {
            int retValue = -1;
            if(this.valueForSort != null || cell.valueForSort != null)
            {
                Object ct = this.valueForSort == null
                    ? this.cellValue : this.valueForSort;
                Object ct2 = cell.valueForSort == null
                    ? cell.cellValue : cell.valueForSort;
                DataTypes dt = this.valueForSort == null
                    ? DataTypes.from(cell.valueForSort.getClass())
                    : DataTypes.from(this.valueForSort.getClass());
                retValue = Algorithms.compare(dt,ct,ct2);
            }
            else
            {
                retValue = Algorithms.compare(this.getDataType(),this.getValue(),cell.getValue());
            }
            return retValue;
        }
    }

    public boolean equals(Object obj)
    {
        if(null != obj)
        {
            if(obj instanceof Cell)
            {
                Cell cell = (Cell) obj;
                if(null == cell || cell.cellValue == null || this.cellValue == null)
                {
                    return false;
                }
                else
                {
                    return cell.cellValue.equals(this.cellValue);
                }
            }
            else
            {
                return super.equals(obj);
            }
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return super.hashCode();
    }
}

