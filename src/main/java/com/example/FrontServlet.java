package com.example;

import java.io.IOException;
import java.io.PrintWriter;

import com.registry.AnnotatedRouteRegistry;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import com.example.ModelView;
import java.util.List;
import java.lang.reflect.Parameter;
import com.annotation.RequestParam;
import java.time.LocalDate;
import com.annotation.PathVariable;

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
                        out.println("<html><body>Method: " + methodName + "<br>Package: " + packageName + "<br>Return Type: " + method.getReturnType().getSimpleName() + "<br>Result: " + result + "</body></html>");
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
                } else {
                    out.println("<html><body>Package: " + packageName + "<br>Return Type: " + method.getReturnType().getSimpleName() + "<br><span style='color:red;'>Type de retour non supporté</span></body></html>");
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
            Class<?> type = params[i].getType();
            if (type == String.class) {
                args[i] = value;
            } else if (type == Integer.class || type == int.class) {
                args[i] = value != null ? Integer.valueOf(value) : null;
            } else if (type == LocalDate.class) {
                args[i] = value != null ? LocalDate.parse(value) : null;
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }
}