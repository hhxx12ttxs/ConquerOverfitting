package com.bigbear.adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Chứa danh sách các giờ trong một ngày. Có tổng cộng 6 giờ từ 1-2 cho đến 11-12
 * @author luanvu
 */
public class ListHours {
	private List<HoursEntity> list=new ArrayList<HoursEntity>();
	private static HoursEntity defaultHours=new HoursEntity();
	public ListHours() {
	}
	
	public List<HoursEntity> getList() {
		return list;
	}
	public void setList(List<HoursEntity> list) {
		this.list = list;
	}
	public int size(){
		return list==null?0:list.size();
	}
	public void add(HoursEntity ett){
		if(list==null){
			throw new NullPointerException("List is null");
		}
		list.add(ett);
	}
	public boolean remove(HoursEntity ett){
		return list.remove(ett);
	}
	public boolean remove(String hours){
		if(hours==null){
			throw new NullPointerException("Hours is null");
		}
		for(HoursEntity ett: list){
			if(hours.equals(ett.getHours())){
				list.remove(ett);
				return true;
			}
		}
		return false;
	}
	public static HoursEntity getDefaultHours(String hours) {
		defaultHours.setHours(hours);
		return defaultHours;
	}
	public HoursEntity get(String hours){
		if(hours==null){
			throw new NullPointerException("Hours is null");
		}
		for(HoursEntity ett: list){
			if(hours.equals(ett.getHours())){
				return ett;
			}
		}
		return getDefaultHours(hours);
	}
}

