keys = linea.split(&quot;\t&quot;);
if(!abbrs.containsKey(keys[0]))
abbrs.put(keys[0], keys[1]);
}

issnRaf.close();
public synchronized static CompletarPublicacion getInstnacia(String path) {

if (instancia == null)
instancia = new CompletarPublicacion(path);

