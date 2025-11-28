package com.registry;

import com.annotation.UrlController;
import com.annotation.HttpMethod;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

public class AnnotatedRouteRegistry {

    private List<RouteInfo> routes = new ArrayList<>();

    public static class RouteInfo {
        public Pattern pattern;
        public Method method;
        public Object instance;
        public String httpMethod;
    }

    public void scanAndRegister(String packageName) {
        // For simplicity, we'll manually add known classes
        String[] classes = {"com.example.TestController"};
        for (String className : classes) {
            try {
                Class<?> clazz = Class.forName(className);
                Object instance = clazz.getDeclaredConstructor().newInstance();
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(UrlController.class)) {
                        UrlController annotation = method.getAnnotation(UrlController.class);
                        String url = annotation.value();
                        if (url.isEmpty()) {
                            url = "/";
                        }
                        String patternStr = url.replaceAll("\\{[^}]+\\}", "(.+)");
                        Pattern pattern = Pattern.compile(patternStr);
                        String httpMethod = "GET"; // default
                        if (method.isAnnotationPresent(HttpMethod.class)) {
                            HttpMethod hm = method.getAnnotation(HttpMethod.class);
                            httpMethod = hm.value();
                        }
                        RouteInfo ri = new RouteInfo();
                        ri.pattern = pattern;
                        ri.method = method;
                        ri.instance = instance;
                        ri.httpMethod = httpMethod;
                        routes.add(ri);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error loading class: " + className + " - " + e.getMessage());
            }
        }
    }

    public Method getMethod(String url, String httpMethod) {
        for (RouteInfo ri : routes) {
            if (ri.httpMethod.equals(httpMethod) && ri.pattern.matcher(url).matches()) {
                return ri.method;
            }
        }
        return null;
    }

    public Object getInstance(String url, String httpMethod) {
        for (RouteInfo ri : routes) {
            if (ri.httpMethod.equals(httpMethod) && ri.pattern.matcher(url).matches()) {
                return ri.instance;
            }
        }
        return null;
    }

    public boolean hasUrl(String url, String httpMethod) {
        for (RouteInfo ri : routes) {
            if (ri.httpMethod.equals(httpMethod) && ri.pattern.matcher(url).matches()) {
                return true;
            }
        }
        return false;
    }

    public List<String> getParams(String url, String httpMethod) {
        for (RouteInfo ri : routes) {
            if (ri.httpMethod.equals(httpMethod)) {
                Matcher m = ri.pattern.matcher(url);
                if (m.matches()) {
                    List<String> params = new ArrayList<>();
                    for (int i = 1; i <= m.groupCount(); i++) {
                        params.add(m.group(i));
                    }
                    return params;
                }
            }
        }
        return null;
    }
}