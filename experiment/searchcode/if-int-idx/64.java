package main.DexClass.Dataset;

import java.util.*;

public class CodeItem {
	//private List li = new List();
	private int[] offset = null;
	private int[] registers_size = null;
	private int[] ins_size = null;
	private int[] outs_size = null;
	private int[] tries_size = null;
	private int[] debug_info_off = null;
	private int[] insns_size = null;
	private int[][] insns = null;
	private int[] padding = null;
	private TryItem[][] tries = null;
	private EncodedCatchHandlerList[] echl = null;
	public int getIdxByOff(int off){
		int i=0;
		for(;i<offset.length;i++){
			if (offset[i] == off)
				return i;
		}
		return -1;
	}
	public int[] getInsnsByOff(int off){
		int i=0;
		for(;i<offset.length;i++){
			if (offset[i] == off)
				return insns[i];
		}
		return new int[0];
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
	public int[] getInsns(int idx){
		if (insns == null || insns.length < idx){
			return null;
		} else {
			return insns[idx];
		}
	}
	public TryItem[] getTries(int idx){
		if (tries == null || tries.length < idx){
			return null;
		} else {
			return tries[idx];
		}
	}
	public int[] getRegisters_size() {
		return registers_size;
	}
	public int getRegisters_size(int idx){
		if (registers_size == null || registers_size.length < idx){
			return -1;
		} else {
			return registers_size[idx];
		}
	}
	public void setRegisters_size(int[] registers_size) {
		this.registers_size = registers_size;
	}
	public int[] getIns_size() {
		return ins_size;
	}
	public int getIns_size(int idx){
		if (ins_size == null || ins_size.length < idx){
			return -1;
		} else {
			return ins_size[idx];
		}
	}
	public void setIns_size(int[] ins_size) {
		this.ins_size = ins_size;
	}
	public int[] getOuts_size() {
		return outs_size;
	}
	public int getOuts_size(int idx){
		if (outs_size == null || outs_size.length < idx){
			return -1;
		} else {
			return outs_size[idx];
		}
	}
	public void setOuts_size(int[] outs_size) {
		this.outs_size = outs_size;
	}
	public int[] getTries_size() {
		return tries_size;
	}
	public int getTries_size(int idx){
		if (tries_size == null || tries_size.length < idx){
			return -1;
		} else {
			return tries_size[idx];
		}
	}
	public void setTries_size(int[] tries_size) {
		this.tries_size = tries_size;
	}
	public int[] getDebug_info_off() {
		return debug_info_off;
	}
	public int getDebug_info_off(int idx){
		if (debug_info_off == null || debug_info_off.length < idx){
			return -1;
		} else {
			return debug_info_off[idx];
		}
	}
	public void setDebug_info_off(int[] debug_info_off) {
		this.debug_info_off = debug_info_off;
	}
	public int[] getInsns_size() {
		return insns_size;
	}
	public int getInsns_size(int idx){
		if (insns_size == null || insns_size.length < idx){
			return -1;
		} else {
			return insns_size[idx];
		}
	}
	public void setInsns_size(int[] insns_size) {
		this.insns_size = insns_size;
	}
	public int[][] getInsns() {
		return insns;
	}
	public void setInsns(int[][] insns) {
		this.insns = insns;
	}
	public int[] getPadding() {
		return padding;
	}
	public void setPadding(int[] padding) {
		this.padding = padding;
	}
	public TryItem[][] getTries() {
		return tries;
	}
	public void setTries(TryItem[][] tries) {
		this.tries = tries;
	}
	public EncodedCatchHandlerList[] getEchl() {
		return echl;
	}
	public EncodedCatchHandlerList getEchl(int idx){
		if (echl == null || echl.length < idx){
			return null;
		} else {
			return echl[idx];
		}
	}
	public void setEchl(EncodedCatchHandlerList[] echl) {
		this.echl = echl;
	}
	
}

