/*
 * Copyright (c) 2010-2012 The Amdatu Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.amdatu.web.rest.doc.swagger;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.amdatu.web.rest.doc.Description;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import com.google.gson.Gson;

/**
 * Listens to services in the framework and analyzes them for documentation. Also implements
 * the Servlet API so you can map this component to an endpoint and use it to show all
 * REST APIs that are available in the framework.
 */
public class SwaggerServlet extends HttpServlet implements ManagedService {
	private static final String ENDPOINT_KEY = "endpoint";
	private static final String DEFAULT_ENDPOINT = "";
	private final List<Object> m_restServices = new ArrayList<Object>();
	private String m_restEndpoint = DEFAULT_ENDPOINT;

	public void addService(ServiceReference ref, Object service) {
		synchronized (m_restServices) {
			m_restServices.add(service);
		}
	}
	
	public void removeService(ServiceReference ref, Object service) {
		synchronized (m_restServices) {
			m_restServices.remove(service);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getPathInfo();
		String baseURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
		resp.setContentType("application/json");
		PrintWriter writer = resp.getWriter();
		if (path == null || path.equals(DEFAULT_ENDPOINT) || path.equals("/")) {
			Object[] services;
			synchronized (m_restServices) {
				services = m_restServices.toArray();
			}
			writer.append(createResourceListingFor(baseURL, req.getServletPath(), services));
		}
		else {
			Object[] services;
			synchronized (m_restServices) {
				services = m_restServices.toArray();
			}
			for (Object s : services) {
				Class clazz = s.getClass();
				Path rootPath = (Path) clazz.getAnnotation(Path.class);
				if (rootPath != null && path.equals("/" + rootPath.value())) {
					String result = createDocumentationFor(baseURL, s);
					if (result != null && result.length() > 0) {
						writer.append(result);
					}
				}
			}
		}
	}

	public String createDocumentationFor(String baseURL, Object instance) {
		Class clazz = instance.getClass();
		Path rootPath = (Path) clazz.getAnnotation(Path.class);
		Description rootDescription = (Description) clazz.getAnnotation(Description.class);
				
		if (rootPath != null) {
			List<SwaggerAPIPath> apis = new ArrayList<SwaggerAPIPath>();
			SwaggerModel models = new SwaggerModel();
			SwaggerAPI api = new SwaggerAPI(baseURL + m_restEndpoint, "/" + rootPath.value(), apis, models);
						
			Method[] methods = clazz.getDeclaredMethods();
			Arrays.sort(methods, new Comparator<Method>() {
				@Override
				public int compare(Method o1, Method o2) {
					String n1 = o1.getName();
					String n2 = o2.getName();
					Path p1 = o1.getAnnotation(Path.class);
					Path p2 = o2.getAnnotation(Path.class);
					n1 = (p1 != null) ? p1.value() : n1;
					n2 = (p2 != null) ? p2.value() : n2;
					return (n1.compareTo(n2));
				}
			});
			for (Method method : methods) {
				GET get = method.getAnnotation(GET.class);
				POST post = method.getAnnotation(POST.class);
				PUT put = method.getAnnotation(PUT.class);
				DELETE delete = method.getAnnotation(DELETE.class);
				Path path = method.getAnnotation(Path.class);
				Description doc = method.getAnnotation(Description.class);
				Class<?> returnType = method.getReturnType();
				String responseClass = convertToSwaggerType(returnType, models);
				List<SwaggerParameter> ps = new ArrayList<SwaggerParameter>();
				Annotation[][] parametersAnnotations = method.getParameterAnnotations();
				Class<?>[] parameterTypes = method.getParameterTypes();
				for (int i = 0; i < parameterTypes.length; i++) {
					Annotation[] parameterAnnotations = parametersAnnotations[i];
					Class parameterType = parameterTypes[i];
					String pathParameterName = null;
					String queryParameterName = null;
					String headerParameterName = null;
					String formParameterName = null;
					String parameterDocumentation = null;
					for (Annotation a : parameterAnnotations) {
						if (a instanceof PathParam) {
							pathParameterName = ((PathParam) a).value();
						}
						else if (a instanceof QueryParam) {
							queryParameterName = ((QueryParam) a).value();
						}
						else if (a instanceof HeaderParam) {
							headerParameterName = ((HeaderParam) a).value();
						}
						else if (a instanceof FormParam) {
							formParameterName = ((FormParam) a).value();
						}
						/**/
						if (a instanceof Description) {
							parameterDocumentation = ((Description) a).value();
						}
					}
					if (pathParameterName != null) {
						ps.add(new SwaggerParameter("path", pathParameterName, parameterDocumentation, convertToSwaggerType(parameterType, models)));
					}
					else if (queryParameterName != null) {
						ps.add(new SwaggerParameter("query", queryParameterName, parameterDocumentation, convertToSwaggerType(parameterType, models)));
					}
					else if (headerParameterName != null) {
						ps.add(new SwaggerParameter("header", headerParameterName, parameterDocumentation, convertToSwaggerType(parameterType, models)));
					}
					else if (formParameterName != null) {
						ps.add(new SwaggerParameter("body", formParameterName, parameterDocumentation, convertToSwaggerType(parameterType, models)));
					}
					else {
						ps.add(new SwaggerParameter("body", parameterType.getSimpleName(), parameterDocumentation, convertToSwaggerType(parameterType, models)));
					}
				}
				List<SwaggerOperation> ops = new ArrayList<SwaggerOperation>();
				apis.add(new SwaggerAPIPath("/" + rootPath.value() + (path == null ? DEFAULT_ENDPOINT : "/" + path.value()), (doc == null ? DEFAULT_ENDPOINT : doc.value()), ops));
				
				if (get instanceof GET || post instanceof POST || put instanceof PUT || delete instanceof DELETE) {
					ops.add(new SwaggerOperation(
						(get instanceof GET ? HttpMethod.GET : DEFAULT_ENDPOINT) + 
						(post instanceof POST ? HttpMethod.POST : DEFAULT_ENDPOINT) + 
						(put instanceof PUT ? HttpMethod.PUT : DEFAULT_ENDPOINT) + 
						(delete instanceof DELETE ? HttpMethod.DELETE : DEFAULT_ENDPOINT),
						method.getName(), 
						responseClass, 
						ps,
						(doc == null ? null : doc.value())
					));
				}
			}
			Gson gson = new Gson();
			return gson.toJson(api);
		}
		return null;
	}

	private String convertToSwaggerType(Class type, SwaggerModel models) {
		if (Integer.TYPE.equals(type)) {
			return "int";
		}
		else if (String.class.equals(type)) {
			return "string";
		}
		else if (Boolean.TYPE.equals(type)) {
			return "boolean";
		}
		else if (Byte.TYPE.equals(type)) {
			return "byte";
		}
		else if (Long.TYPE.equals(type)) {
			return "long";
		}
		else if (Float.TYPE.equals(type)) {
			return "float";
		}
		else if (Double.TYPE.equals(type)) {
			return "double";
		}
		else if (Date.class.equals(type)) {
			return "Date";
		}
		else {
			// it's a custom type, we need to create a model for it (if it does not already exist)
			if (!models.containsKey(type.getName())) {
				Map<String, SwaggerModelProperty> mp = new HashMap<String, SwaggerModelProperty>();
				SwaggerModelType mt = new SwaggerModelType(type.getName(), mp);
				models.put(type.getName(), mt);
				Field[] fields = type.getDeclaredFields();
				for (Field f : fields) {
					if (!Modifier.isStatic(f.getModifiers())) {
						Class<?> fieldType = f.getType();
						Description description = f.getAnnotation(Description.class);
						String swaggerType = convertToSwaggerType(fieldType, models);
						SwaggerModelProperty smp = new SwaggerModelProperty(swaggerType, (description != null ? description.value() : null));
						mp.put(f.getName(), smp);
					}
				}
			}
			return type.getName();
		}
	}

	public String createResourceListingFor(String baseURL, String path, Object[] instances) {
		ArrayList<SwaggerResource> rs = new ArrayList<SwaggerResource>(); 
		for (Object instance : instances) {
			StringBuilder builder = new StringBuilder();
			Class clazz = instance.getClass();
			Path rootPath = (Path) clazz.getAnnotation(Path.class);
			Description rootDescription = (Description) clazz.getAnnotation(Description.class);
			
			if (rootPath != null) {
				String root = rootPath.value();
				String description = rootDescription != null ? rootDescription.value() : "";
				
				if (!root.startsWith("/")) {
					root = "/" + root;
				}
				rs.add(new SwaggerResource(path + root, description));
			}
		}
		SwaggerResources r = new SwaggerResources(baseURL, rs);
		Gson gson = new Gson();
		return gson.toJson(r);
	}

	@Override
	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties == null) {
			m_restEndpoint  = DEFAULT_ENDPOINT;
		}
		else {
			Object endpoint = properties.get(ENDPOINT_KEY);
			if (endpoint instanceof String) {
				m_restEndpoint = (String) endpoint;
			}
			else {
				throw new ConfigurationException(ENDPOINT_KEY, "has to be a string.");
			}
		}
	}
}

/* Data Objects for JSON, as defined in the spec: https://github.com/wordnik/swagger-core/wiki */

class SwaggerResources {
	private final String apiVersion = "1.0";
	private final String swaggerVersion = "1.1";
	private String basePath;
	private List<SwaggerResource> apis;
	
	public SwaggerResources(String basePath, List<SwaggerResource> apis) {
		this.basePath = basePath;
		this.apis = apis;
	}
}

class SwaggerResource {
	private String path;
	private String description;
	
	public SwaggerResource(String path, String description) {
		this.path = path;
		this.description = description;
	}
}

class SwaggerAPI {
	private final String apiVersion = "1.0";
	private final String swaggerVersion = "1.1";
	private String basePath;
	private String resourcePath;
	private List<SwaggerAPIPath> apis;
	private SwaggerModel models;
	
	public SwaggerAPI(String basePath, String resourcePath, List<SwaggerAPIPath> apis, SwaggerModel models) {
		this.basePath = basePath;
		this.resourcePath = resourcePath;
		this.apis = apis;
		this.models = models;
	}
}

class SwaggerAPIPath {
	private String path;
	private String description;
	private List<SwaggerOperation> operations;

	public SwaggerAPIPath(String path, String description, List<SwaggerOperation> operations) {
		this.path = path;
		this.description = description;
		this.operations = operations;
	}
}

class SwaggerOperation {
	private String httpMethod;
	private String nickname;
	private String responseClass;
	private List<SwaggerParameter> parameters;
	private String summary;
	
	public SwaggerOperation(String httpMethod, String nickname, String responseClass, List<SwaggerParameter> parameters, String summary) {
		this.httpMethod = httpMethod;
		this.nickname = nickname;
		this.responseClass = responseClass;
		this.parameters = parameters;
		this.summary = summary;
	}
}

class SwaggerParameter {
	private String paramType;
	private String name;
	private String description;
	private String dataType;
	
	public SwaggerParameter(String paramType, String name, String description, String dataType) {
		this.paramType = paramType;
		this.name = name;
		this.description = description;
		this.dataType = dataType;
	}
}

class SwaggerModel extends HashMap<String, SwaggerModelType> {
	
}

class SwaggerModelType {
	private String id;
	private Map<String, SwaggerModelProperty> properties;
	public SwaggerModelType(String id, Map<String, SwaggerModelProperty> properties) {
		this.id = id;
		this.properties = properties;
	}
}

class SwaggerModelProperty {
	private String type;
	private String description;
	
	public SwaggerModelProperty(String type, String description) {
		this.type = type;
		this.description = description;
	}
	/* TODO more properties */
}

