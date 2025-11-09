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
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (path.endsWith(".jsp")) {
            // Check if JSP exists
            if (getServletContext().getResource(path) != null) {
                RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("jsp");
                dispatcher.forward(request, response);
            } else {
                out.println("<html><body>" + path + " - 404 Not Found</body></html>");
            }
        } else {
            if (registry.hasUrl(path)) {
                Method method = registry.getMethod(path);
                Object instance = registry.getInstance(path);
                String packageName = method.getDeclaringClass().getPackageName();
                try {
                    String result = (String) method.invoke(instance);
                    out.println("<html><body>Package: " + packageName + "<br>Result: " + result + "</body></html>");
                } catch (Exception e) {
                    out.println("<html><body>Error: " + e.getMessage() + "</body></html>");
                }
            } else {
                out.println("<html><body>" + path + " - 404 Not Found</body></html>");
            }
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        service(request, response);
    }
}