    throws CellNotFoundException{
        if(false)
 * 
 * OpenXLS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
            CellRange[] rngz = this.getCellRanges();
            if (rngz!=null) {  
            for(int t=0;t<rngz.length;t++) {
            return null;
 * You should have received a copy of the GNU Lesser General Public
 * License along with OpenXLS.  If not, see
 * <http://www.gnu.org/licenses/>.
// 20100217 KSC: try a better way (that can handle 3D refs and complex cell ranges)
String loc= myName.getLocation();// may contain one or more ranges, separated by \",\"'s if complex
WorkSheetHandle[] sheets = this.getReferencedSheets();

