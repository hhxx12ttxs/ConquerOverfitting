package ctd.protocol.schema.support;

import ctd.protocol.message.validate.ValidateStatus;
import ctd.protocol.schema.constans.DataTypes;
import ctd.protocol.schema.exception.SchemaException;
import ctd.util.BeanUtils;


public class Attribute extends AbstractNode {
	private Meta meta;
	
	public Attribute(){
		this("string");
	};
	
	public Attribute(String primitiveType){
		if(!DataTypes.isSupportType(primitiveType)){
			throw new SchemaException("entry[" + id + "] type is not primitive type:" + type,this);
		}
		this.primitiveType = primitiveType;
		this.type = primitiveType;
	}
	
	public Attribute(Meta meta){
		this.meta = meta;
		this.primitiveType = meta.type;
		BeanUtils.copy(meta, this);
	}
	
	@Override
	public Object parseValue(Object v){
		return meta == null ? super.parseValue(v) : meta.parseValue(v);
	}
	
	@Override
	public String toDisplayValue(Object v){
		return meta == null ? super.toDisplayValue(v) : meta.toDisplayValue(v);
	}
	
	@Override
	public ValidateStatus validate(Object val){
		if(meta != null){
			ValidateStatus status = meta.validate(val);
			if(status.isNotOK()){
				return status;
			}
		}
		return super.validate(val);
	}
}

