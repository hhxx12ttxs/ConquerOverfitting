package importnew;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test;

/*
 * Java8简明教程
 */
public class Java8Tutorial{
	static int outerStaticNum;
    int outerNum;

	//允许在接口中有默认方法实现
	@Test
	public void test(){
		Formula formula = new Formula(){
			@Override
			public double calculate(int a){
				return sqrt(a*100);
			}
		};
		System.out.println(formula.calculate(100));
		System.out.println("直接调用接口的default方法"+formula.sqrt(16));
	}

	/*
	 * 函数式接口,每一个lambda都能够通过一个特定的接口,与一个给定的类型进行匹配,一个所谓的函数式接口必须要有且仅有一个抽象方法声明
	 */
	@Test
	public void test1(){
		Converter<String, Integer> converter = (from) -> Integer.valueOf(from);
		Integer converted = converter.convert("123");
		System.out.println(converted);
		
		//通过静态方法引用,使之更加简洁,Java 8 允许你通过::关键字获取方法或者构造函数的的引用
		Converter<String, Integer> converter2 = Integer::valueOf;
		Integer converted2 = converter2.convert("123");
		System.out.println(converted2);
	}
	
	//对象的方法进行引用
	@Test
	public void test2(){
		Something something = new Something();
		Converter<String,String> converte = something::startsWith;
		String coverted = converte.convert("Java");
		System.out.println(coverted);
	}
	
	//通过构造函数引用 将create方法与Person的构造函数绑定
	@Test
	public void test7(){
		PersonFactory<Person> personFactory = Person::new;
		Person person = personFactory.create("Peter", "Parker");
		System.out.println(person);
	}
	
	/*
	 * Lambda的范围
	 */
	//访问lambda表达式外部的final局部变量
	@Test
	public void test3(){
		final int i=1;
		Converter<Integer,Integer> converter = (from) -> from+i;
		System.out.println(converter.convert(2));
	}
	
	//变量i并不需要一定是final,但是i在编译的时候被隐式地当做final变量来处理,所以不能再给i赋值
	@Test
	public void test4(){
		int i=1;
		Converter<Integer,Integer> converter = (from) -> from+i;
		System.out.println(converter.convert(2));
		//wrong: i=3;
	}
	
	//默认方法default无法在lambda表达式内部被访问。因此下面的代码是无法通过编译的：
	@Test
	public void test5(){
		//wrong: Formula formula = (a) -> sqrt(a*100);
	}
	
	//lambda表达式的内部能获取到对成员变量或静态变量的读写权
	@Test
	public void test6(){
		Converter<Integer,Integer> converter = (from) -> {outerStaticNum = 100;return from+outerStaticNum;};
		Converter<Integer,Integer> converter2 = (from) -> {outerNum = 200;return from+outerNum;};
		System.out.println(converter.convert(10));
		System.out.println(converter2.convert(20));
		outerStaticNum=500;
		outerNum=600;
		System.out.println(outerStaticNum+","+outerNum);
	}
	
	/*
	 * Streamsjava.util.Stream表示了某一种元素的序列,在这些元素上可以进行各种操作.
	 * Stream操作可以是中间操作,也可以是完结操作.
	 * 完结操作会返回一个某种类型的值，而中间操作会返回流对象本身，并且你可以通过多次调用同一个流操作方法来将操作结果串起来（就像StringBuffer的append方法一样)
	 */
	@Test
	public void test8(){
		List<String> stringCollection = new ArrayList<>();
		stringCollection.add("ddd2");
		stringCollection.add("aaa2");
		stringCollection.add("bbb1");
		stringCollection.add("aaa1");
		stringCollection.add("bbb3");
		stringCollection.add("ccc");
		stringCollection.add("bbb2");
		stringCollection.add("ddd1");
		
		//Filter和Sorted操作
		stringCollection.stream().filter((s) -> s.startsWith("a")).sorted().forEach(System.out::println);
		Consumer<String> greeter = (p) -> System.out.print(p+",");
		stringCollection.stream().map(String::toUpperCase).sorted().forEach(greeter);
		
		//Match操作
		boolean anyStartsWithA = stringCollection.stream().anyMatch((s) -> s.startsWith("a"));
		System.out.println("anyStartsWithA is: "+anyStartsWithA);
			 
		boolean allStartsWithA = stringCollection.stream().allMatch((s) -> s.startsWith("a"));
		System.out.println("allMatchA is: "+allStartsWithA);
			 
		boolean noneStartsWithZ = stringCollection.stream().noneMatch((s) -> s.startsWith("z"));
		System.out.println("noneStartsWithZ is: "+noneStartsWithZ);
		
		//Count
		long startWithB = stringCollection.stream().filter((s)->s.startsWith("b")).count();
		System.out.println("startWithB is: "+startWithB);
		
		//Reduce 该操作是一个终结操作,它能够通过某一个方法,对元素进行削减操作.该操作的结果会放在一个Optional变量里返回.
		Optional<String> reduced =stringCollection.stream().sorted().reduce((s1, s2) -> s1 + "#" + s2);
		reduced.ifPresent(System.out::println);
	}
	
	/*
	 * Parallel Streams 流操作可以是顺序的,也可以是并行的.顺序操作通过单线程执行,而并行操作则通过多线程执行.
	 */
	@Test
	public void test9(){
		int max = 1000000;
		List<String> values = new ArrayList<>(max);
		for (int i = 0; i < max; i++) {
		    UUID uuid = UUID.randomUUID();
		    values.add(uuid.toString());
		}
		
		calculateTime(values,false);
		calculateTime(values,true);
	}
	
	public void calculateTime(List<String> list, boolean isParallel){
		long t0 = System.nanoTime();
		long count;
		if(isParallel){
			count = list.parallelStream().sorted().count();
		}else{
			count = list.stream().sorted().count();
		}
		System.out.println(count);
		long t1 = System.nanoTime();
		long millis = TimeUnit.NANOSECONDS.toMillis(t1 - t0);
		System.out.println(String.format("Sort took: %d ms", millis));
	}
	
	@Test
	public void test10(){
		Map<Integer, String> map = new HashMap<>();
		for (int i = 0; i < 10; i++){
		    map.putIfAbsent(i, "val" + i);
		}
		map.forEach((id, val) -> System.out.println(val));
		
		//存在才计算
		map.computeIfPresent(3, (num, val) -> val + num);
		System.out.println(map.get(3));
		
		map.computeIfPresent(9, (num, val) -> null);
		System.out.println(map.containsKey(9));
		
		//不存在才计算
		map.computeIfAbsent(23, num -> "val" + num);
		System.out.println(map);
		
		//因为3已经存在,所以不会计算
		map.computeIfAbsent(3, num -> "bam");
		System.out.println(map);
	}
	
	/*
	 * Clock提供了对当前时间和日期的访问功能
	 * Clock是对当前时区敏感的,并可用于替代System.currentTimeMillis()方法来获取当前的毫秒时间
	 * 当前时间线上的时刻可以用Instance类来表示,Instance也能够用于创建原先的java.util.Date对象
	 */
	@Test
	public void test11(){
		Clock clock = Clock.systemDefaultZone();
		long millis = clock.millis();
		System.out.println("millis is: "+millis);
		Instant instant = clock.instant();
		Date legacyDate = Date.from(instant);
		System.out.println("legacyDate is: "+legacyDate);
	}
	
	@Test
	public void test12(){
		System.out.println(ZoneId.getAvailableZoneIds());
		// prints all available timezone ids
		ZoneId zone1 = ZoneId.of("Europe/Berlin");
		ZoneId zone2 = ZoneId.of("Brazil/East");
		System.out.println(zone1.getRules());
		System.out.println(zone2.getRules());
	}
	
	/*
	 *本地时间表示了一个独一无二的时间,例如：2014-03-11.这个时间是不可变的,与LocalTime是同源的
	 *下面的例子演示了如何通过加减日,月,年等指标来计算新的日期
	 *每一次操作都会返回一个新的时间对象
	 */
	@Test
	public void test13(){
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		LocalDate yesterday = tomorrow.minusDays(2);
		System.out.println("tomorrow is: "+tomorrow);
		System.out.println("yesterday is: "+yesterday);
		LocalDate independenceDay = LocalDate.of(2014, Month.JULY, 4);
		DayOfWeek dayOfWeek = independenceDay.getDayOfWeek();
		System.out.println(dayOfWeek);
	}
	
	/*
	 * LocalDateTime表示的是日期-时间,它将刚才介绍的日期对象和时间对象结合起来,形成了一个对象实例
	 * LocalDateTime是不可变的,与LocalTime和LocalDate的工作原理相同.
	 * 我们可以通过调用方法来获取日期时间对象中特定的数据域
	 */
	@Test
	public void test14(){
		LocalDateTime localDateTime = LocalDateTime.of(2014, Month.OCTOBER, 24, 23, 59, 59);
		DayOfWeek day = localDateTime.getDayOfWeek();
		System.out.println("Day of week: "+day);
		
		Month month = localDateTime.getMonth();
		System.out.println("Month: "+month);
		
		long minute = localDateTime.getLong(ChronoField.MINUTE_OF_DAY);
		System.out.println("Minute of Day: "+minute);
		
		//如果再加上的时区信息,LocalDateTime能够被转换成Instance实例,Instance能够被转换成以前的java.util.Date对象
		Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
		Date legacyDate = Date.from(instant);
		System.out.println("Legacy Date: "+legacyDate);
		
		//Format
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm");
		LocalDateTime parsed = LocalDateTime.parse("Nov 03, 2014 - 07:13", formatter);
		String string = formatter.format(parsed);
		System.out.println("Format Date is: "+string);
	}
	
	
	//TODO something wrong with this
	@Test
	public void test15(){
		Hint hint0 = School.class.getAnnotation(Hint.class);
		System.out.println(hint0);
		
		Hints hints1 = School2.class.getAnnotation(Hints.class);
		System.out.println(hints1.value()[0].value());
		 
		Hint[] hints2 = School.class.getAnnotationsByType(Hint.class);
		System.out.println(hints2.length); 
		
		for(Hint hint:hints2){
			System.out.println(hint.value());
		}
		
	}
}

interface Formula {
    double calculate(int a);
    default double sqrt(int a) {
        return Math.sqrt(a);
    }
}

//函数式接口
@FunctionalInterface
interface Converter<F, T> {
    T convert(F from);
}

class Something {
	String startsWith(String s){
		return String.valueOf(s.charAt(0));
	}
}

class Person {
    String firstName;
    String lastName;
    Person() {}
    Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    @Override
    public String toString(){
    	return "firstName: "+firstName+", "+"lastName: "+lastName;
    }
}

interface PersonFactory<P extends Person> {
    P create(String firstName, String lastName);
}

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@interface Hints {
    Hint[] value();
}

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@Repeatable(Hints.class)
@interface Hint {
    String value();
}

@Hints({@Hint("hint1"),@Hint("hint2")})
class School{}
//使用可重复注解(新方法)
@Hint("hint1")
@Hint("hint2")
class School2{}
