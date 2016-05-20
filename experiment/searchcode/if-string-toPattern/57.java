package ixtab.ktlocale.modification;


import ixtab.ktlocale.MessageFormatResources;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;

public class NonStringRemoverMapModificationStrategy implements
		MapModificationStrategy {

	@Override
	public void updateResourceBundleMap(String packageName, String className, SortedMap<String, Object> map) {
		Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			Object v = entry.getValue();
			if (v == null || !(v instanceof String || isArray1(v) || isArray2(v) || isArray3(v))) {
				if (v instanceof MessageFormat) {
					MessageFormat f = (MessageFormat) v;
//					if (f.toPattern().contains(",date,")) {
//						System.err.println(packageName+"."+className+"#"+entry.getKey());
//						System.err.println(f.toPattern());
//					}
					entry.setValue(f.toPattern());
					MessageFormatResources.add(packageName, className, entry.getKey());
				} else {
					it.remove();
				}
			}
		}
	}

	private boolean isArray1(Object o) {
		return String[].class.isAssignableFrom(o.getClass());
	}
	private boolean isArray2(Object o) {
		return String[][].class.isAssignableFrom(o.getClass());
	}
	private boolean isArray3(Object o) {
		return String[][][].class.isAssignableFrom(o.getClass());
	}
}

