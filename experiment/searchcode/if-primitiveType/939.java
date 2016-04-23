package org.javajavalang.target.PHP;

import java.util.HashMap;

import org.antlr.stringtemplate.StringTemplate;
import org.javajavalang.GenerationObserver;
import org.javajavalang.RuleHandler;
import org.javajavalang.Target;

public class PhpTarget extends Target {

	public static final String FILE_EXTENSION = ".php";

	@Override
	public String getName() {	
		return "PHP";
	}

	@Override
	public String getTargetFileExtension() {		 
		return FILE_EXTENSION;
	}
	
	@Override
	public void registerTargetHandlers(GenerationObserver observer) throws Exception {
		// register our handler list
		//observer.registerRuleHandler(new TypeRuleHandler());
	}

	/*class TypeRuleHandler extends RuleHandler {

		public TypeRuleHandler() {
			// report our name to super constructor
			super("type");
		}

		@Override
		public String handle(HashMap<String, Object> args) {
			// get args
			int primitiveType = (int) args.get("primitiveType");
			StringTemplate qualifiedTypeIdent = (StringTemplate) args.get("qualifiedTypeIdent");
			StringTemplate arrayDeclaratorList =  (StringTemplate) args.get("arrayDeclaratorList");
			
			// output
			StringBuilder res = new StringBuilder();
			if (primitiveType != null) {
				if (primitiveType.toString() == "") {
				  
				}				
			}
			return null;
		}
		
	}*/

	
}

