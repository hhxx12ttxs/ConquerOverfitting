package edu.ufpb.moodle.relatorio.exportador;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import edu.ufpb.moodle.relatorio.Atividade;
import edu.ufpb.moodle.relatorio.RelatorioDeUsuario;

public class Resumo {

	Map<LocalDate, Long> duracoes = new HashMap<LocalDate, Long>();
	private final RelatorioDeUsuario relatorio;
	
	public Resumo(RelatorioDeUsuario relatorio) {
		this.relatorio = relatorio;
	}

	public long getTimes(LocalDate date) {
		Long result = duracoes.get(date);
		return result == null ? 0 : result.longValue(); 
	}

	public void add(LocalDate date, long time) {
		duracoes.put(date, time);
	}


	public DateTime getEndDate() {
		DateTime end = relatorio.getAtividades().get(0).getInterval().getStart();
		
		for (Atividade atividade : relatorio.getAtividades()) {
			if(atividade.getInterval().getEnd().isAfter(end)){
				end = atividade.getInterval().getStart();
			}
		}
		
		return end;
	}

	public DateTime getStartDate() {
		DateTime start = relatorio.getAtividades().get(0).getInterval().getStart();
		
		for (Atividade atividade : relatorio.getAtividades()) {
			if(atividade.getInterval().getStart().isBefore(start)){
				start = atividade.getInterval().getStart();
			}
		}
		
		return start;
	}


}

