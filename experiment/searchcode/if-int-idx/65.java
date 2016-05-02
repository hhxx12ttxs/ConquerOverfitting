package main.DexClass.Dataset;

public class ClassDefs {

	int size = 0;
	private int[] class_idx	        = null;
	private int[] access_flags	    = null;
	private int[] superclass_idx	  = null;
	private int[] interfaces_off	  = null;
	private int[] source_file_idx 	= null;
	private int[] annotations_off	  = null;
	private int[] class_data_off	  = null;
	private int[] static_values_off	= null;
	public int[] getClass_idx() {
		return class_idx;
	}
	public int getClass_idx(int idx){
		if (class_idx == null || class_idx.length < idx){
			return -1;
		} else {
			return class_idx[idx];
		}
	}
	public void setClass_idx(int[] class_idx) {
		this.class_idx = class_idx;
		size = class_idx.length;
	}
	public int[] getAccess_flags() {
		return access_flags;
	}
	public int getAccess_flags(int idx){
		if (access_flags == null || access_flags.length < idx){
			return -1;
		} else {
			return access_flags[idx];
		}
	}
	public void setAccess_flags(int[] access_flags) {
		this.access_flags = access_flags;
	}
	public int[] getSuperclass_idx() {
		return superclass_idx;
	}
	public int getSuperclass_idx(int idx){
		if (superclass_idx == null || superclass_idx.length < idx){
			return -1;
		} else {
			return superclass_idx[idx];
		}
	}
	public void setSuperclass_idx(int[] superclass_idx) {
		this.superclass_idx = superclass_idx;
	}
	public int[] getInterfaces_off() {
		return interfaces_off;
	}
	public int getInterfaces_off(int idx){
		if (interfaces_off == null || interfaces_off.length < idx){
			return -1;
		} else {
			return interfaces_off[idx];
		}
	}
	public void setInterfaces_off(int[] interfaces_off) {
		this.interfaces_off = interfaces_off;
	}
	public int[] getSource_file_idx() {
		return source_file_idx;
	}
	public int getSource_file_idx(int idx){
		if (source_file_idx == null || source_file_idx.length < idx){
			return -1;
		} else {
			return source_file_idx[idx];
		}
	}
	public void setSource_file_idx(int[] source_file_idx) {
		this.source_file_idx = source_file_idx;
	}
	public int[] getAnnotations_off() {
		return annotations_off;
	}
	public int getAnnotations_off(int idx){
		if (annotations_off == null || annotations_off.length < idx){
			return -1;
		} else {
			return annotations_off[idx];
		}
	}
	public void setAnnotations_off(int[] annotations_off) {
		this.annotations_off = annotations_off;
	}
	public int[] getClass_data_off() {
		return class_data_off;
	}
	public int getClass_data_off(int idx){
		if (class_data_off == null || class_data_off.length < idx){
			return -1;
		} else {
			return class_data_off[idx];
		}
	}
	public void setClass_data_off(int[] class_data_off) {
		this.class_data_off = class_data_off;
	}
	public int[] getStatic_values_off() {
		return static_values_off;
	}
	public int getStatic_values_off(int idx){
		if (static_values_off == null || static_values_off.length < idx){
			return -1;
		} else {
			return static_values_off[idx];
		}
	}
	public void setStatic_values_off(int[] static_values_off) {
		this.static_values_off = static_values_off;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
}

