public List<IncogitoSession> sessionsToObjects(int defaultYear) {
if(null == json) {
return new ArrayList<IncogitoSession>();
return SessionTranslator.translateSessions((List<HashMap<String, Object>>) map.get(&quot;sessions&quot;), defaultYear);
} catch (IOException e) {
e.printStackTrace(); //LOG?
}

return null;
}
}

