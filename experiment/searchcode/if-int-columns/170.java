/**
* Copyright (c) 2010, Digg, Inc.
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
*
*    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
*    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
*    * Neither the name of the Digg, Inc. nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* @author David Taylor <david@tinystatemachine.com>
*/

package models;

import java.util.*;

import javax.persistence.*;

import play.db.jpa.*;

@Entity
public class Dashboard extends Model {

	public String name;
	public String widths;
 		
	@OneToMany(mappedBy="dashboard", cascade=CascadeType.REMOVE)
	public Set<Assignment> assignments;
	
	@Transient
	public Status status;

	private Long updated;

	public Dashboard(String name, String widths, long updated) {
		this.name = name;
		this.widths = widths;
	}
	
	public Dashboard save() {
		this.updated = new Date().getTime();
		return super.save();
	}

	/**
	 * @return list of relative column widths
	 */
	public List<Integer> columnWidths() {

		List<Integer> ws = new ArrayList<Integer>();

		if (widths != null) 
			try {
				for (String s : widths.split(","))
					ws.add(Integer.parseInt(s.trim()));
			} catch (NumberFormatException e) {}
			
		if(ws.size() < 1) {
			ws.add(50);
			ws.add(50);
		}
	
		return ws;
	}
	
	
	public int columns() {
		return columnWidths().size();		
	}
	
	/**
	 * Flattens the 2D list of lists of widgets into a simple set of all widgets
	 * @return set of all widgets on this dashboard
	 */
	public Set<WidgetInstance> allWidgets() {
		Set<WidgetInstance> flat = new HashSet<WidgetInstance>();
		for(List<Assignment> col : widgets())
			for(Assignment a : col)
				flat.add(a.widget);
		return flat;
	}
	
	/**
	 * Builds a 2D, list of lists of widgets, representing a list of columns of widgets
	 * @return columns of widgets
	 */
	public List<List<Assignment>> widgets() {
		List<List<Assignment>> cols = new ArrayList();
		
		int columns = columns(); 
		
		for(int i = 0; i<Math.max(1, columns); i++) {
			cols.add(new ArrayList<Assignment>());
		}
		for( Assignment a : assignments) {
			if(a.col -1 >= columns ) {
				 a.col = 1;
				 a.save();
			}
			cols.get(a.col - 1).add(a);
		}
		
		List<List<Assignment>> sorted = new ArrayList();
		for(int i = 0; i < columns; i++) {
			sorted.add(new ArrayList());
		}
		
		for(List<Assignment> col : cols) {
			Collections.sort(col);
			for(Assignment a : col) {
				
				if(a.col > sorted.size()-1)
					for(int i = sorted.size(); i < a.col; i++) {
						sorted.add(new ArrayList());
					}
				sorted.get(a.col-1).add(a);
			}
		}
		return sorted;
	}
	
	public String toString() {
		return name;
	}
	
	public Status getStatus() {
		 boolean dark = Setting.get("hiddenMode", "false").equals("true");
		 return new Status(id+"_"+updated, dark );
	}
	
	public static class Status {
		public String id;
		public boolean hide;
		
		public Status(String id,  boolean hide) {
			this.id = id;
			this.hide = hide;
		}
		
	}

}

