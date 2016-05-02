package main.DexClass.Dataset;

import java.util.Arrays;
import java.util.List;


public class ClassDataItem {

	private int[] static_fields_size = null;
	private int[] instance_fields_size = null;
	private int[] direct_methods_size = null;
	private int[] virtual_methods_size = null;
	private EncodedField[][] static_field = null;
	private EncodedField[][] instance_field = null;  
	private EncodedMethod[][] direct_methods = null;
	private EncodedMethod[][] virtual_methods = null;
	private int[] offset = null;
	
	int[] addElement(int[] org, int added) {
	    int[] result = Arrays.copyOf(org, org.length +1);
	    result[org.length] = added;
	    return result;
	}
	EncodedField[][] addElement(EncodedField[][] org, EncodedField[] added) {
		EncodedField[][] result = Arrays.copyOf(org, org.length +1);
	    result[org.length] = added;
	    return result;
	}
	EncodedMethod[][] addElement(EncodedMethod[][] org, EncodedMethod[] added) {
		EncodedMethod[][] result = Arrays.copyOf(org, org.length +1);
	    result[org.length] = added;
	    return result;
	}
	
	public void add(int static_field_size, int instance_field_size, int direct_method_size,
			int virtual_method_size, EncodedField[] stati_field, EncodedField[] instanc_field,
			EncodedMethod[] direc_method, EncodedMethod[] virtua_method, int offse){
		if (static_fields_size == null){
			this.static_fields_size = new int[1];
			this.instance_fields_size = new int[1];
			this.direct_methods_size = new int[1];
			this.virtual_methods_size = new int[1];
			this.static_field = new EncodedField[1][];
			this.instance_field = new EncodedField[1][];  
			this.direct_methods = new EncodedMethod[1][];
			this.virtual_methods = new EncodedMethod[1][];
			this.offset = new int[1];
			
			this.static_fields_size[0] = static_field_size;
			this.instance_fields_size[0] = instance_field_size;
			this.direct_methods_size[0] = direct_method_size;
			this.virtual_methods_size[0] = virtual_method_size;
			this.static_field[0] = stati_field;
			this.instance_field[0] = instanc_field;  
			this.direct_methods[0] = direc_method;
			this.virtual_methods[0] = virtua_method;
			this.offset[0] = offse;
			
			return;
		}
		else {
			static_fields_size = addElement(static_fields_size,static_field_size);
			instance_fields_size = addElement(instance_fields_size,instance_field_size);
			direct_methods_size = addElement(direct_methods_size,direct_method_size);
			virtual_methods_size = addElement(virtual_methods_size,virtual_method_size);
			static_field = addElement(static_field,stati_field);
			instance_field = addElement(instance_field,instanc_field);
			direct_methods = addElement(direct_methods,direc_method);
			virtual_methods = addElement(virtual_methods,virtua_method);
			offset = addElement(offset,offse);
		}
		
	}
	public int[] getOffset() {
		return offset;
	}
	public int getOffset(int idx){
		if (offset == null || offset.length < idx){
			return -1;
		} else {
			return offset[idx];
		}
	}
	public void setOffset(int[] offset) {
		this.offset = offset;
	}
	public int[] getStatic_fields_size() {
		return static_fields_size;
	}
	public int getStatic_fields_size(int idx){
		if (static_fields_size == null || static_fields_size.length < idx || idx < 0){
			return -1;
		} else {
			return static_fields_size[idx];
		}
	}
	public void setStatic_fields_size(int[] static_fields_size) {
		this.static_fields_size = static_fields_size;
	}
	public int[] getInstance_fields_size() {
		return instance_fields_size;
	}
	public int getInstance_fields_size(int idx){
		if (instance_fields_size == null || instance_fields_size.length < idx || idx < 0){
			return -1;
		} else {
			return instance_fields_size[idx];
		}
	}
	public void setInstance_fields_size(int[] instance_fields_size) {
		this.instance_fields_size = instance_fields_size;
	}
	public int[] getDirect_methods_size() {
		return direct_methods_size;
	}
	public int getDirect_methods_size(int idx){
		if (direct_methods_size == null || direct_methods_size.length < idx || idx < 0){
			return -1;
		} else {
			return direct_methods_size[idx];
		}
	}
	public void setDirect_methods_size(int[] direct_methods_size) {
		this.direct_methods_size = direct_methods_size;
	}
	public int[] getVirtual_methods_size() {
		return virtual_methods_size;
	}
	public int getVirtual_methods_size(int idx){
		if (virtual_methods_size == null || virtual_methods_size.length < idx || idx < 0){
			return -1;
		} else {
			return virtual_methods_size[idx];
		}
	}
	public void setVirtual_methods_size(int[] virtual_methods_size) {
		this.virtual_methods_size = virtual_methods_size;
	}
	public EncodedField[][] getStatic_field() {
		return static_field;
	}
	public EncodedField[] getStatic_field(int idx){
		if (static_field == null || static_field.length < idx){
			return null;
		} else {
			return static_field[idx];
		}
	}
	public void setStatic_field(EncodedField[][] static_field) {
		this.static_field = static_field;
	}
	public EncodedField[][] getInstance_field() {
		return instance_field;
	}
	public EncodedField[] getInstance_field(int idx){
		if (instance_field == null || instance_field.length < idx){
			return null;
		} else {
			return instance_field[idx];
		}
	}
	public void setInstance_field(EncodedField[][] instance_field) {
		this.instance_field = instance_field;
	}
	public EncodedMethod[][] getDirect_methods() {
		return direct_methods;
	}
	public EncodedMethod[] getDirect_methods(int idx){
		if (direct_methods == null || direct_methods.length < idx || idx < 0){
			return new EncodedMethod[0];
		} else {
			return direct_methods[idx];
		}
	}
	public void setDirect_methods(EncodedMethod[][] direct_methods) {
		this.direct_methods = direct_methods;
	}
	public EncodedMethod[][] getVirtual_methods() {
		return virtual_methods;
	}
	public EncodedMethod[] getVirtual_methods(int idx){
		if (virtual_methods == null || virtual_methods.length < idx || idx < 0){
			return new EncodedMethod[0];
		} else {
			return virtual_methods[idx];
		}
	}
	public void setVirtual_methods(EncodedMethod[][] virtual_methods) {
		this.virtual_methods = virtual_methods;
	}
	public int getOffsetIdx(int off){
		if (offset == null || offset.length < 0){
			return -1;
		} else {
			for (int i=0;i<offset.length;i++){
				if (off == offset[i]){
					return i;
				}
			}
			return -1;
		}
	}
}

