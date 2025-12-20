package com.example;

import java.io.IOException;
import java.io.PrintWriter;

import com.registry.AnnotatedRouteRegistry;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.servlet.annotation.MultipartConfig;
import java.lang.reflect.Method;
import com.example.ModelView;
import java.util.List;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Arrays;
import java.util.List;
import com.annotation.RequestParam;
import java.time.LocalDate;
import com.annotation.PathVariable;
import com.annotation.Json;
import com.example.FileUpload;

@MultipartConfig(
    maxFileSize = 10485760,      // 10 MB
    maxRequestSize = 20971520,   // 20 MB
    fileSizeThreshold = 1048576  // 1 MB
)
public class FrontServlet extends HttpServlet {

    private AnnotatedRouteRegistry registry;

    @Override
    public void init() throws ServletException {
        registry = new AnnotatedRouteRegistry();
        registry.scanAndRegister("com.example");
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String httpMethod = request.getMethod();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (path.endsWith(".jsp")) {
            // Check if JSP exists
            System.out.println("Checking resource: " + path + " exists: " + (getServletContext().getResource(path) != null));
            if (getServletContext().getResource(path) != null) {
                RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("jsp");
                dispatcher.forward(request, response);
            } else {
                out.println("<html><body>" + path + " - 404 Not Found</body></html>");
            }
        } else {
            if (registry.hasUrl(path, httpMethod)) {
                Method method = registry.getMethod(path, httpMethod);
                Object instance = registry.getInstance(path, httpMethod);
                String packageName = method.getDeclaringClass().getPackageName();
                String methodName = method.getName();
                if (method.getReturnType() == String.class) {
                    try {
                        List<String> pathParams = registry.getParams(path, httpMethod);
                        Object[] args = getArgs(method, request, pathParams);
                        String result = (String) method.invoke(instance, args);
                        if (method.isAnnotationPresent(Json.class)) {
                            response.setContentType("application/json");
                            out.println(result);
                        } else {
                            out.println("<html><body>Method: " + methodName + "<br>Package: " + packageName + "<br>Return Type: " + method.getReturnType().getSimpleName() + "<br>Result: " + result + "</body></html>");
                        }
                    } catch (Exception e) {
                        out.println("<html><body>Error: " + e.getMessage() + "</body></html>");
                    }
                } else if (method.getReturnType() == ModelView.class) {
                    try {
                        List<String> pathParams = registry.getParams(path, httpMethod);
                        Object[] args = getArgs(method, request, pathParams);
                        ModelView mv = (ModelView) method.invoke(instance, args);
                        String view = mv.getView();
                        request.setAttribute("data", mv.getData());
                        RequestDispatcher dispatcher = request.getRequestDispatcher(view);
                        dispatcher.forward(request, response);
                    } catch (Exception e) {
                        out.println("<html><body>Error: " + e.getMessage() + "</body></html>");
                    }
                } else if (method.getReturnType() == Map.class) {
                    try {
                        List<String> pathParams = registry.getParams(path, httpMethod);
                        Object[] args = getArgs(method, request, pathParams);
                        Map result = (Map) method.invoke(instance, args);
                        if (method.isAnnotationPresent(Json.class)) {
                            response.setContentType("application/json");
                            String json = mapToJson(result);
                        } else {
                            out.println("<html><body>Method: " + methodName + "<br>Package: " + packageName + "<br>Return Type: " + method.getReturnType().getSimpleName() + "<br>Result: " + result + "</body></html>");
                        }
                    } catch (Exception e) {
                        out.println("<html><body>Error: " + e.getMessage() + "</body></html>");
                    }
                }
            } else {
                out.println("<html><body>" + path + " - 404 Not Found</body></html>");
            }
        }
    }

    private Object[] getArgs(Method method, HttpServletRequest request, List<String> pathParams) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];
        int pathVarIndex = 0;
        for (int i = 0; i < params.length; i++) {
            Class<?> type = params[i].getType();
            if (type == Map.class) {
                Map<String, Object> map = new HashMap<>();
                Enumeration<String> paramNames = request.getParameterNames();
                while (paramNames.hasMoreElements()) {
                    String paramName = paramNames.nextElement();
                    String[] values = request.getParameterValues(paramName);
                    String paramValue = values != null && values.length > 0 ? (values.length == 1 ? values[0] : String.join(",", values)) : null;
                    map.put(paramName, paramValue);
                }
                args[i] = map;
            } else if (type == FileUpload.class) {
                String paramName = params[i].getName();
                try {
                    Part part = request.getPart(paramName);
                    if (part != null && part.getSize() > 0) {
                        args[i] = new FileUpload(part);
                    } else {
                        args[i] = null;
                    }
                } catch (Exception e) {
                    args[i] = null;
                }
            } else {
                String value = null;
                if (params[i].isAnnotationPresent(RequestParam.class)) {
                    RequestParam rp = params[i].getAnnotation(RequestParam.class);
                    String name = rp.value();
                    value = request.getParameter(name);
                } else if (params[i].isAnnotationPresent(PathVariable.class)) {
                    value = pathParams.get(pathVarIndex++);
                } else {
                    // For parameters without annotation, assume query param with param name
                    String name = params[i].getName();
                    value = request.getParameter(name);
                }
                if (type == String.class) {
                    args[i] = value;
                } else if (type == Integer.class || type == int.class) {
                    args[i] = value != null ? Integer.valueOf(value) : null;
                } else if (type == LocalDate.class) {
                    args[i] = value != null ? LocalDate.parse(value) : null;
                } else {
                    // Support for custom objects (POJO)
                    try {
                        Object instance = type.getDeclaredConstructor().newInstance();
                        Enumeration<String> paramNames = request.getParameterNames();
                        while (paramNames.hasMoreElements()) {
                            String paramName = paramNames.nextElement();
                            String[] values = request.getParameterValues(paramName);
                            String paramValue = values != null && values.length > 0 ? values[0] : null;
                            // Assume setter like setNom(String value) or setMatieres(List<String> value)
                            String setterName = "set" + paramName.substring(0, 1).toUpperCase() + paramName.substring(1);
                            try {
                                // Always try List setter first for parameters with values
                                if (values != null && values.length > 0) {
                                    try {
                                        java.lang.reflect.Method setterList = type.getMethod(setterName, List.class);
                                        setterList.invoke(instance, Arrays.asList(values));
                                        continue; // Skip to next param
                                    } catch (NoSuchMethodException eList) {
                                        // Fall back to single value
                                    }
                                }
                                // Try String setter
                                java.lang.reflect.Method setter = type.getMethod(setterName, String.class);
                                setter.invoke(instance, paramValue);
                            } catch (NoSuchMethodException e) {
                                // Setter not found, try Integer setter if value is number
                                if (paramValue != null && paramValue.matches("\\d+")) {
                                    try {
                                        java.lang.reflect.Method setterInt = type.getMethod(setterName, Integer.class);
                                        setterInt.invoke(instance, Integer.valueOf(paramValue));
                                    } catch (NoSuchMethodException e2) {
                                        // Ignore
                                    }
                                }
                            }
                        }
                        args[i] = instance;
                    } catch (Exception e) {
                        args[i] = null;
                    }
                }
            }
        }
        return args;
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof Map) {
                json.append(mapToJson((Map<String, Object>) value));
            } else {
                json.append("\"").append(value.toString()).append("\"");
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }
}