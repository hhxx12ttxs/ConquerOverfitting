private static HashMap<String, Person> persons = new HashMap<String, Person>();

public static Person person(String name) {
if (!persons.containsKey(name)) persons.put(name, new Person(name));
return persons.get(name);

