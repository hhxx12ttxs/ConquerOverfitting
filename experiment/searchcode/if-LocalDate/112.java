package nl.depository.pim.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import nl.depository.pim.entities.EditorText;
import nl.depository.pim.entities.dto.DurationDTO;
import nl.depository.pim.entities.dto.LocalDateDTO;
import nl.depository.pim.entities.dto.LocalDateTimeDTO;
import nl.depository.pim.pages.types.EditorTextType;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.internal.translator.AbstractTranslator;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.services.BaseURLSource;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.EditBlockContribution;
import org.apache.tapestry5.services.FormSupport;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.orientechnologies.orient.core.serialization.serializer.object.OObjectSerializer;
import com.orientechnologies.orient.core.serialization.serializer.object.OObjectSerializerContext;
import com.orientechnologies.orient.core.serialization.serializer.object.OObjectSerializerHelper;

public class AppModule
{
	public static void bind(ServiceBinder binder)
	{
		binder.bind(Authenticator.class, BasicAuthenticator.class);
	}
	
	public DataService buildDataService(RegistryShutdownHub hub)
	{
		OObjectSerializerContext defaultContext = new OObjectSerializerContext();
		defaultContext.bind(new OObjectSerializer<LocalDate, LocalDateDTO>()
		{
			public LocalDateDTO serializeFieldValue(Object pojo, String fieldName, LocalDate fieldValue)
			{
				return new LocalDateDTO(fieldValue.toString());
			}

			public LocalDate unserializeFieldValue(Object pojo, String fieldName, LocalDateDTO fieldValue)
			{
				return new LocalDate(fieldValue.representation);
			}
		});
		defaultContext.bind(new OObjectSerializer<LocalDateTime, LocalDateTimeDTO>()
		{
			public LocalDateTimeDTO serializeFieldValue(Object pojo, String fieldName, LocalDateTime fieldValue)
			{
				return new LocalDateTimeDTO(fieldValue.toString());
			}

			public LocalDateTime unserializeFieldValue(Object pojo, String fieldName, LocalDateTimeDTO fieldValue)
			{
				return new LocalDateTime(fieldValue.representation);
			}
		});
		defaultContext.bind(new OObjectSerializer<Duration, DurationDTO>()
		{
			public DurationDTO serializeFieldValue(Object pojo, String fieldName, Duration fieldValue)
			{
				return new DurationDTO(fieldValue.toString());
			}

			public Duration unserializeFieldValue(Object pojo, String fieldName, DurationDTO fieldValue)
			{
				return new Duration(fieldValue.representation);
			}
		});
		OObjectSerializerHelper.bindSerializerContext(null, defaultContext);
		
		DataService service = new DataService();
		service.registerEntityClasses("nl.depository.pim.entities");
		hub.addRegistryShutdownListener(service);
		return service;
	}
	
	public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
	{
		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "nl,en");
		configuration.add(SymbolConstants.PRODUCTION_MODE, "false");
		configuration.add(SymbolConstants.APPLICATION_VERSION, "1.0-SNAPSHOT");
		configuration.add(SymbolConstants.SECURE_ENABLED, "true");
		configuration.add(SymbolConstants.DEFAULT_STYLESHEET, "classpath:nl/depository/pim/default.css");
	}
	
	/**
	 * This is a service definition, the service will be named "TimingFilter". The interface,
	 * RequestFilter, is used within the RequestHandler service pipeline, which is built from the
	 * RequestHandler service configuration. Tapestry IoC is responsible for passing in an
	 * appropriate Logger instance. Requests for static resources are handled at a higher level, so
	 * this filter will only be invoked for Tapestry related requests.
	 * 
	 * <p>
	 * Service builder methods are useful when the implementation is inline as an inner class
	 * (as here) or require some other kind of special initialization. In most cases,
	 * use the static bind() method instead. 
	 * 
	 * <p>
	 * If this method was named "build", then the service id would be taken from the 
	 * service interface and would be "RequestFilter".  Since Tapestry already defines
	 * a service named "RequestFilter" we use an explicit service id that we can reference
	 * inside the contribution method.
	 */
	/*public RequestFilter buildTimingFilter(final Logger log)
	{
		return new RequestFilter()
		{
			public boolean service(Request request, Response response, RequestHandler handler) throws IOException
			{
				long startTime = System.currentTimeMillis();
				
				try
				{
					// The responsibility of a filter is to invoke the corresponding method
					// in the handler. When you chain multiple filters together,
					// each filter received a handler that is a bridge to the next filter.
					
					return handler.service(request, response);
				}
				finally
				{
					long elapsed = System.currentTimeMillis() - startTime;
					
					log.info(String.format("It took %d ms to handle %s", elapsed, request.getPath()));
				}
			}
		};
	}*/
	
	/**
	 * This is a contribution to the RequestHandler service configuration. This is how we extend
	 * Tapestry using the timing filter. A common use for this kind of filter is transaction
	 * management or security. The @Local annotation selects the desired service by type, but only
	 * from the same module.  Without @Local, there would be an error due to the other service(s)
	 * that implement RequestFilter (defined in other modules).
	 */
	/*public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration, @Local RequestFilter filter)
	{
		// Each contribution to an ordered configuration has a name, When necessary,
		// you may set constraints to precisely control the invocation order of the
		// contributed filter within the pipeline.
		
//		configuration.add("Timing", filter);
	}*/
	
	public static void contributeComponentRequestHandler(OrderedConfiguration<ComponentRequestFilter> configuration)
	{
//		configuration.addInstance("RequiresLogin", AuthenticationFilter.class);
	}
	
	/*final static Map<String, Class<?>> dataTypes;
	
	static
	{
		Class<?>[] dataTypeClasses = new Class<?>[]
		{
			LocalDate.class,
			LocalDateTime.class,
			Duration.class,
			EditorText.class,
			File.class,
			Folder.class,
			Icon.class,
			URL.class,
			WordList.class
		};
		
		dataTypes = new HashMap<String, Class<?>>();
		
		for (Class<?> dataTypeClass: dataTypeClasses)
		{
			String className = dataTypeClass.getSimpleName();
			className = className.substring(0, 1).toLowerCase() + className.substring(1);
			dataTypes.put(className, dataTypeClass);
		}
	}*/
	
	public static void contributeDefaultDataTypeAnalyzer(MappedConfiguration<Class<?>, String> configuration)
	{
		/*for (Entry<String, Class<?>> dataType: dataTypes.entrySet())
		{
			configuration.add(dataType.getValue(), dataType.getKey());
		}*/

		configuration.add(URL.class, "text");
		configuration.add(LocalDate.class, "date");
		configuration.add(LocalDateTime.class, "date");
		
		configuration.add(EditorText.class, "editorText");
	}
	
	public static void contributeBeanBlockSource(Configuration<BeanBlockContribution> configuration)
	{
		/*for (Entry<String, Class<?>> dataType: dataTypes.entrySet())
		{
			String dataTypePage = "types/" + dataType.getValue().getSimpleName() + "Type";
			configuration.add(new EditBlockContribution(dataType.getKey(), dataTypePage, dataType.getKey()));
		}*/
		
		configuration.add(new EditBlockContribution("editorText", "types/EditorTextType", "editorText"));
	}
	
	public void contributeTranslatorSource(MappedConfiguration<Class<?>, Translator<?>> configuration) throws Exception
	{
		/*for (Entry<String, Class<?>> dataType: dataTypes.entrySet())
		{
			String translatorClassName = "nl.depository.pim.pages.types." + dataType.getValue().getSimpleName() + "Type$Translator";
			Class<?> translatorClass = Class.forName(translatorClassName);
			Translator<?> translator = (Translator<?>) translatorClass.newInstance();
			configuration.add(dataType.getValue(), translator);
		}*/
		
		configuration.add(URL.class, new AbstractTranslator<URL>("url", URL.class, "url")
		{
			public String toClient(URL value)
			{
				return value != null ? value.toString() : null;
			}

			public URL parseClient(Field field, String clientValue, String message) throws ValidationException
			{
				URL url = null;
				
				try
				{
					url = new URL(clientValue);
				}
				catch (MalformedURLException e) {}
				
				return url;
			}

			public void render(Field field, String message, MarkupWriter writer, FormSupport formSupport) {}
		});
		
		configuration.add(EditorText.class, new EditorTextType.Translator());
	}
	
	@SuppressWarnings("rawtypes")
	public static void contributeTypeCoercer(Configuration<CoercionTuple> configuration)
	{
		// LocalDate <> Date
		configuration.add(new CoercionTuple<Date, LocalDate>(Date.class, LocalDate.class, new Coercion<Date, LocalDate>()
		{
			public LocalDate coerce(Date input)
			{
				return input != null ? new LocalDate(input) : null;
			}
		}));
		configuration.add(new CoercionTuple<LocalDate, Date>(LocalDate.class, Date.class, new Coercion<LocalDate, Date>()
		{
			public Date coerce(LocalDate input)
			{
				return input != null ? input.toDateTimeAtCurrentTime().toDate() : null;
			}
		}));
		
		// LocalDateTime <> Date
		configuration.add(new CoercionTuple<Date, LocalDateTime>(Date.class, LocalDateTime.class, new Coercion<Date, LocalDateTime>()
		{
			public LocalDateTime coerce(Date input)
			{
				return input != null ? new LocalDateTime(input) : null;
			}
		}));
		configuration.add(new CoercionTuple<LocalDateTime, Date>(LocalDateTime.class, Date.class, new Coercion<LocalDateTime, Date>()
		{
			public Date coerce(LocalDateTime input)
			{
				return input != null ? input.toDateTime().toDate() : null;
			}
		}));
	}
	
	public static void contributeIgnoredPathsFilter(Configuration<String> configuration)
	{
//		configuration.add(".*.js");
	}
	
	public static void contributeServiceOverride(MappedConfiguration<Class<?>, Object> configuration)
	{
		BaseURLSource source = new BaseURLSource()
		{
			public String getBaseURL(boolean secure)
			{
				String protocol = secure ? "https" : "http";
				
				int port = secure ? 8443 : 8080;
				
				return String.format("%s://localhost:%d", protocol, port);
			}
		};
		
		configuration.add(BaseURLSource.class, source);
	}
}

