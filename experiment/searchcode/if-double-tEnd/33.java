package lrg.insider.plugins.details;

import lrg.common.abstractions.entities.AbstractEntityInterface;
import lrg.common.abstractions.entities.GroupEntity;
import lrg.common.abstractions.entities.ResultEntity;
import lrg.common.abstractions.plugins.filters.FilteringRule;
import lrg.common.abstractions.plugins.filters.composed.NotComposedFilteringRule;
import lrg.insider.plugins.core.details.HTMLDetail;
import lrg.insider.plugins.filters.StatisticalThresholds;
import lrg.insider.plugins.filters.memoria.classes.IsInner;

public class OverviewPyramid{
    private AbstractEntityInterface currentSystem;
    private GroupEntity methodGroup;
    private GroupEntity globalFunctionGroup;
    //private final AbstractEntityInterface entity;
    
    private final String RED = "#CC0000";
    private final String BLUE = "#0000CC";
    private final String GREEN = "#006600";
    
    String anddColor;
    String ahitColor;

    String fout_callColor;
    String call_nomColor;

    String cyc_locColor;
    String loc_nomColor;
    String nom_nocColor;
    String noc_nopColor;

    
    public ResultEntity compute(AbstractEntityInterface anEntity) {
    	//this.entity = entity;
    	
    	String text = "<h1>" + "System Overview for " + anEntity.getName() + "</h1><hr><br>";

        initMembers(anEntity);
        text += "<table><tr><td valign=\"top\">";
        text += buildHTMLPyramid(anEntity);
        text += "</td><td valign=\"top\">";
        text += "</td></tr></table>";
       

        return new ResultEntity(text);
    }

    private void initMembers(AbstractEntityInterface theSystem) {
    	currentSystem = theSystem;
        methodGroup = currentSystem.getGroup("method group").applyFilter("model function");
        globalFunctionGroup = currentSystem.getGroup("global function group").applyFilter("model function");
    }

    public String buildHTMLPyramid(AbstractEntityInterface theSystem) {
        initMembers(theSystem);
        String result = "";
        double cyc = getCYCLO(), loc = getLOC(), nom = getNOM().size();
        double noc = getNOC().size(), nop = getNOP().size();
        double fout = getFANOUT(), call = getCALLS();
        double andd = getAVG_NDD(), ahit = getAVG_HIT();

        anddColor = getColor(andd, StatisticalThresholds.NDD_LOW, StatisticalThresholds.NDD_AVG, StatisticalThresholds.NDD_HIGH);
        ahitColor = getColor(ahit, StatisticalThresholds.HIT_LOW, StatisticalThresholds.HIT_AVG, StatisticalThresholds.HIT_HIGH);

        fout_callColor = getColor(fout / call, StatisticalThresholds.FOUT_CALL_LOW, StatisticalThresholds.FOUT_CALL_AVG, StatisticalThresholds.FOUT_CALL_HIGH);
        call_nomColor = getColor(call / nom, StatisticalThresholds.CALL_NOM_LOW, StatisticalThresholds.CALL_NOM_AVG, StatisticalThresholds.CALL_NOM_HIGH);

        cyc_locColor = getColor(cyc / loc, StatisticalThresholds.CYCLO_LOC_LOW, StatisticalThresholds.CYCLO_LOC_AVG, StatisticalThresholds.CYCLO_LOC_HIGH);
        loc_nomColor = getColor(loc / nom, StatisticalThresholds.LOC_NOM_LOW, StatisticalThresholds.LOC_NOM_AVG, StatisticalThresholds.LOC_NOM_HIGH);
        nom_nocColor = getColor(nom / noc, StatisticalThresholds.NOM_NOC_LOW, StatisticalThresholds.NOM_NOC_AVG, StatisticalThresholds.NOM_NOC_HIGH);
        noc_nopColor = getColor(noc / nop, StatisticalThresholds.NOC_NOP_LOW, StatisticalThresholds.NOC_NOP_AVG, StatisticalThresholds.NOC_NOP_HIGH);

        result += "<table border=0 cellpadding=0 cellspacing=0>";
        
        result += "<tr>";
        result += "<td>&nbsp;</td>";
        result += "<td>&nbsp;</td>";
        result += "<td>&nbsp;</td>";
        result += "<td>&nbsp;</td>";
        result += "<td bgcolor=#99FF99> NDD &nbsp;&nbsp;</td>";
        result += "<td bgcolor=" + anddColor+ "><div align=right><font color=#FFFFFF>  " + round(andd) + "&nbsp; </font></div></td>";
        result += "<td>&nbsp;</td>";
        result += "</tr>";

        result += "<tr>";
        result += "<td>&nbsp;</td>";
        result += "<td>&nbsp;</td>";
        result += "<td>&nbsp;</td>";
        result += "<td>&nbsp;</td>";
        result += "<td bgcolor=#99FF99> HIT &nbsp;&nbsp;</td>";
        result += "<td bgcolor=" + ahitColor+ "><div align=right><font color=#FFFFFF>  " + round(ahit) + "&nbsp; </font></div></td>";
        result += "<td>&nbsp;</td>";
        result += "</tr>";

        result += "<tr>";
        result += "<td>&nbsp;</td>";
        result += "<td>&nbsp;</td>";
        result += "<td>&nbsp;</td>";
        result += "<td bgcolor=" + noc_nopColor+ "><font color=#FFFFFF>  " + round(noc/nop) + "&nbsp; </font></td>";
        result += "<td bgcolor=#FFFF33> NOP &nbsp;</td>";
        result += "<td bgcolor=#FFFF33><div align=right> " + HTMLDetail.linkToNumber(getNOP()) + " &nbsp;</div></td>";
        result += "<td>&nbsp;</td>";
        result += "</tr>";
  
        result += "<tr>";
        result += "<td>&nbsp;</td>";
        result += "<td>&nbsp;</td>";
        result += "<td bgcolor=" + nom_nocColor+ "><font color=#FFFFFF>  " + round(nom / noc) + "&nbsp; </font></td>";
        result += "<td bgcolor=#FFFF33> NOC &nbsp;&nbsp;</td>";
        result += "<td bgcolor=#FFFF33>&nbsp;</td>";
        result += "<td bgcolor=#FFFF33><div align=right> " + HTMLDetail.linkToNumber(getNOC())+ " &nbsp;</div></td>";
        result += "<td>&nbsp;</td>";
        result += "</tr>";
 
        result += "<tr>";
        result += "<td>&nbsp;</td>";
        result += "<td bgcolor=" + loc_nomColor+ "><font color=#FFFFFF>  " + round(loc / nom) + "&nbsp; </font></td>";
        result += "<td bgcolor=#FFFF33> NOM &nbsp;&nbsp;</td>";
        result += "<td bgcolor=#FFFF33>&nbsp;</td>";
        result += "<td bgcolor=#FFFF33>&nbsp;</td>";
        result += "<td bgcolor=#FFFF33><div align=right> " + HTMLDetail.linkToNumber(getNOM()) + " &nbsp;</div></td>";
        result += "<td bgcolor=#99CCFF><div align=right> NOM &nbsp;</div></td>";
        result += "<td bgcolor=" + call_nomColor + "><div align=right><font color=#FFFFFF>  " + round(call / nom) + "&nbsp; </font></div></td>";
        result += "<td>&nbsp;</td>";
        result += "</tr>";

        result += "<tr>";
        result += "<td bgcolor=" + cyc_locColor + "><font color=#FFFFFF>  " + round(cyc / loc) + "&nbsp; </font></td>";
        result += "<td bgcolor=#FFFF33> LOC &nbsp;&nbsp;</td>";
        result += "<td bgcolor=#FFFF33>&nbsp;</td>";
        result += "<td bgcolor=#FFFF33>&nbsp;</td>";
        result += "<td bgcolor=#FFFF33>&nbsp;</td>";
        result += "<td bgcolor=#FFFF33><div align=right> " + intValue(loc) + " &nbsp;</div></td>";
        result += "<td bgcolor=#99CCFF>&nbsp;" + intValue(call) + " &nbsp;</td>";
        result += "<td bgcolor=#99CCFF><div align=right>&nbsp; CALL &nbsp;</div></td>";
        result += "<td bgcolor=" + fout_callColor + "><div align=right><font color=#FFFFFF>  " + round(fout / call) + "&nbsp; </font></div></td>";
        result += "</tr>";

        result += "<tr>";
        result += "<td bgcolor=#FFFF33> CYCLO &nbsp;&nbsp;</td>";
        result += "<td bgcolor=#FFFF33>&nbsp" + ";</td>";
        result += "<td bgcolor=#FFFF33>&nbsp;</td>";
        result += "<td bgcolor=#FFFF33>&nbsp;</td>";
        result += "<td bgcolor=#FFFF33>&nbsp;</td>";
        result += "<td bgcolor=#FFFF33><div align=right> " + intValue(cyc) + " &nbsp;</div></td>";
        result += "<td bgcolor=#99CCFF>&nbsp;" + intValue(fout) + "&nbsp; </td>";
        result += "<td bgcolor=#99CCFF>&nbsp;</td>";
        result += "<td bgcolor=#99CCFF><div align=right>&nbsp; FOUT &nbsp;</div></td>";
        result += "</tr>";
        result += "</table>";
        
        result += writeInterpretation(theSystem);
        
        return result;
    }


private String writeInterpretation(AbstractEntityInterface entity) {
		
		String result = "<font face=\"arial\" size=2> <h3>Interpretation of the Overview Pyramid for module <font COLOR=\"#FF6600\">"+ entity.getName() +"</font></h3>";  

		result += "<p><b>Class Hierarchies</b> tend to be " + writeClassHierarchyInterpretation() + "</p>";				
		result += "<p><b>Classes </b> tend to:</p>";	
		result += "<ul>";
		result += writeClassInterpretation();
		result += "</ul>";
		result += "<p><b>Methods</b> tend to:</p>";	
		result += "<ul>";
		result += writeMethodInterpretation();
		result += "</ul>";
		result += "<p> &nbsp;";
		return result;
	}


private String writeClassInterpretation() {
	String result = "<li>";
	if(nom_nocColor.equals(RED)) result += "be rather"+ formatWord("large", RED) +"(i.e. they define many methods);";
	else if (nom_nocColor.equals(BLUE)) result += "be rather"+ formatWord("small", BLUE) +"(i.e. have only a few methods);";
	else result += "contain an"+ formatWord("average", GREEN) +"number of methods;";
	
	result += "<li>be organized in ";
  	if(noc_nopColor.equals(RED)) result += "rather"+ formatWord("coarse-grained packages", RED) +"(i.e. many classes per package);";
   	else if (noc_nopColor.equals(BLUE)) result += "rather"+ formatWord("fine-grained packages", BLUE) +"(i.e. few classes per package);";
   	else result += formatWord("average-sized packages", GREEN) +";";
		
	return result;
}

	private String writeClassHierarchyInterpretation() {
		String result = "";
		String explanation = "(i.e. inheritance trees tend to have ";

		if(anddColor.equals(BLUE) && ahitColor.equals(BLUE)) return "rather <b>sparse</b> (i.e. there are mostly standalone classes and few inheritance relations)"; 
		
		if(ahitColor.equals(BLUE)) {
			result += formatWord("shallow", BLUE); 
			explanation += " only few depth-level(s) and ";
		}
		else if(ahitColor.equals(RED)) {
			result += formatWord("tall", RED);
			explanation += " many depth-levels and ";
		}
		else { 
			result += "of " + formatWord("average height", GREEN);
			// explanation += "several depth-levels";
		}

		result += " and "; 
//		explanation += " and ";
		
		if(anddColor.equals(RED)) {
			result += formatWord("wide", RED);
			explanation += "base-classes with many directly derived sub-classes)";
		}
		else if(anddColor.equals(BLUE)) {
			result +=formatWord("narrow", BLUE);
			explanation += "base-classes with few directly derived sub-classes)";
		}
		else {
			result += "of " + formatWord("average width", GREEN);
			explanation += "base-classes with several directly derived sub-classes)";
		}
				
		return result+explanation;
	}

	private String writeMethodInterpretation() {
    	String result = "<li>tend to ";
    	if(loc_nomColor.equals(RED)) result += "be rather"+ formatWord("long", RED);
    	else if (loc_nomColor.equals(BLUE)) result += "be rather"+ formatWord("short", BLUE);
    	else result += "be "+ formatWord("average", GREEN) +"in length ";
    	
    	if((loc_nomColor.equals(RED) && cyc_locColor.equals(BLUE)) ||
    	    (loc_nomColor.equals(BLUE) && cyc_locColor.equals(RED)))
    		result += " yet ";
    	else result += " and ";
    	
      	if(cyc_locColor.equals(RED)) result += "having a rather "+ formatWord("complex logic", RED) +"(i.e. many conditional branches);";
       	else if (cyc_locColor.equals(BLUE)) result += "having a rather "+ formatWord("simple logic", BLUE) +"(i.e. few conditional branches);";
       	else result += "having an "+ formatWord("average logical complexity", GREEN) +";";
    	
      	result += "<li> tend to call ";
    	if(call_nomColor.equals(RED)) result += formatWord("many methods", RED) + " (high coupling intensity) ";
    	else if (call_nomColor.equals(BLUE)) result += formatWord("few methods", BLUE) + " (low coupling intensity)";
    	else result += "an "+ formatWord("several methods", GREEN);

    	result += " from ";

    	if(fout_callColor.equals(RED)) result += formatWord("many other classes", RED) + " (high coupling dispersion); ";
    	else if (fout_callColor.equals(BLUE)) result += formatWord("few other classes", BLUE) + "(low coupling dispersion);";
    	else result += "an "+ formatWord("several other classes", GREEN) + ";";
    	
    	
    	return result;
	}

	private String formatWord(String word, String color) {
    	String result = "&nbsp;&nbsp; <b><font COLOR=\"" + color + "\">";
    	result += word +"&nbsp;&nbsp;</b></font> ";
    	return result;
    }  
    
    private double getCYCLO() {
        double cyclo;
        cyclo = ((Double) methodGroup.getProperty("CYCLO").aggregate("sum").getValue()).doubleValue();
        cyclo += ((Double) globalFunctionGroup.getProperty("CYCLO").aggregate("sum").getValue()).doubleValue();
        return cyclo;
    }

    private double getLOC() {
        double loc;
        loc = ((Double) methodGroup.getProperty("LOC").aggregate("sum").getValue()).doubleValue();
        loc += ((Double) globalFunctionGroup.getProperty("LOC").aggregate("sum").getValue()).doubleValue();
        return loc;
    }

    private GroupEntity getNOM() {
        return methodGroup.union(globalFunctionGroup);
    }

    private GroupEntity getNOC() {
        FilteringRule notInnerClass = new NotComposedFilteringRule(new IsInner());
        return currentSystem.getGroup("class group").applyFilter("model class").applyFilter(notInnerClass);
    }

    private GroupEntity getNOP() {
        return currentSystem.getGroup("package group").applyFilter("model package");
    }

    private double getAVG_NDD() {
        //return ((Double) currentSystem.getProperty("AVG_HIT").getValue()).doubleValue();
    	return ((Double) currentSystem.getProperty("AVG_NDD").getValue()).doubleValue();
    }

    private double getAVG_HIT() {
        //return ((Double) currentSystem.getProperty("AVG_NDD").getValue()).doubleValue();
    	return ((Double) currentSystem.getProperty("AVG_HIT").getValue()).doubleValue();
    }

    private double getCALLS() {
        double loc;
        loc = ((Double) methodGroup.getProperty("FANOUT").aggregate("sum").getValue()).doubleValue();
        loc += ((Double) globalFunctionGroup.getProperty("FANOUT").aggregate("sum").getValue()).doubleValue();
        return loc;
    }

    private double getFANOUT() {
        double loc;
        loc = ((Double) methodGroup.getProperty("FANOUTCLASS").aggregate("sum").getValue()).doubleValue();
        loc += ((Double) globalFunctionGroup.getProperty("FANOUTCLASS").aggregate("sum").getValue()).doubleValue();
        return loc;
    }

    private String round(double x) {
        String s = new String(x + "");
        int index = s.indexOf(".");
        if ((s.length() - index) > 2)
            return s.substring(0, index + 3);
        else
            return s;

    }

    private String intValue(double x) {
        return (int) x + "";
    }

    private String getColor(double value, double min, double avg, double max) {

        double dist_min = Math.abs(value - min), dist_avg = Math.abs(value - avg), dist_max = Math.abs(value - max);

        if (dist_min < dist_avg) return BLUE;
        if (dist_avg < dist_max) return GREEN;
        return RED;
    }
}

