import java.util.ArrayList;
import java.util.List;


public class If implements Comando {

	private Expressao expr;
	private List<AcaoSemantica> conteudo;
	private List<ElseIf> elseif;
	private Else elseS;
	
	public If(Expressao expr, List<AcaoSemantica> conteudo,
			List<ElseIf> elseif, Else elseS) {
		this.expr = expr;
		this.conteudo = conteudo;
		this.elseif = elseif;
		this.elseS = elseS;
	}

	@Override
	public String getCode() {
		StringBuffer sb = new StringBuffer();
		String label = ParserUtils.novoLabel();
		String labelFinal = null;
		String s1 = expr.getCode();
		Integer regIf = null;
		List<Integer> regsUsados = new ArrayList<Integer>();
		
		if (expr.possuiRegistradorFinal()) {
			regIf = expr.registradorFinal();
			sb.append(s1);
		} else {
			regIf = ParserUtils.novoRegistrador();
			sb.append("LD R" + regIf + ", " + s1);
		}
		sb.append(System.lineSeparator());

		sb.append("JZ " + label + ", R" + regIf);
		sb.append(System.lineSeparator());
		
		for (AcaoSemantica as : conteudo) {
			sb.append(as.getCode());
			sb.append(System.lineSeparator());	
		}
		
		if (!elseif.isEmpty() || (elseS != null && !elseS.getConteudo().isEmpty())) {
			labelFinal = ParserUtils.novoLabel();
			sb.append("JMP " + labelFinal);
			sb.append(System.lineSeparator());
		}
		
		if (!elseif.isEmpty()) {
			for (ElseIf eif : elseif) {
				sb.append(label + ": ");
				String s = eif.getExpr().getCode();
				Integer regEIf = null;
				if (eif.getExpr().possuiRegistradorFinal()) {
					regEIf = eif.getExpr().registradorFinal();
					sb.append(s);
				} else {
					regEIf = ParserUtils.novoRegistrador();
					sb.append("LD R" + regEIf + ", " + s);
				}
				label = ParserUtils.novoLabel();
				sb.append(System.lineSeparator());
				sb.append("JZ " + label + ", R" + regEIf);
				sb.append(System.lineSeparator());
				sb.append(eif.getCode());
				sb.append("JMP " + labelFinal);
				sb.append(System.lineSeparator());	
				regsUsados.add(regEIf);
			}
		}

		if (elseS != null && !elseS.getConteudo().isEmpty()) {
			sb.append(label+": ");
			sb.append(elseS.getCode());
		}

		sb.append((labelFinal == null ? label : labelFinal) +": ");
//		sb.append(System.lineSeparator());
		
		Integer[] re = new Integer[regsUsados.size()];
		for (int i = 0; i < re.length; i++) {
			re[i] = regsUsados.get(i);
		}
		ParserUtils.liberaRegistradores(re);
		return sb.toString();
	}
	
}

