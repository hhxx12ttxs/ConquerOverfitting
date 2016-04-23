package cn.edu.pku.sei.plde.conqueroverfitting.boundary.model;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;

import java.util.Set;

/**
 * Created by yjxxtd on 4/23/16.
 */
public class BoundaryWithFreq {
    public TypeEnum variableSimpleType;
    public boolean isSimpleType = false;
    public String otherType;
    public String value;
    public int leftClose;
    public int rightClose;
    public int freq;

    public BoundaryWithFreq(TypeEnum variableSimpleType, boolean isSimpleType,
                        String otherType, String value, int leftClose, int rightClose, int freq) {
        this.variableSimpleType = variableSimpleType;
        this.isSimpleType = isSimpleType;
        this.otherType = otherType;
        this.value = value;
        this.leftClose = leftClose;//[k
        this.rightClose = rightClose;//k]
        this.freq = freq;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BoundaryWithFreq))
            return false;
        BoundaryWithFreq other = (BoundaryWithFreq) obj;
        if (isSimpleType != other.isSimpleType)
            return false;
        if (isSimpleType) {
            return variableSimpleType.equals(other.variableSimpleType)
                    && value.equals(other.value);
        } else {
            return otherType.equals(other.otherType)
                    && value.equals(other.value);
        }
    }

    public String getStringType(){
        if (isSimpleType && variableSimpleType==null){
            return "";
        }
        if (isSimpleType && otherType == null){
            return "";
        }
        return isSimpleType?variableSimpleType.toString():otherType;
    }
}
