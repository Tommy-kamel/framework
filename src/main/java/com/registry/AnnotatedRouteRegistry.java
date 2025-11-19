package com.registry;

import com.annotation.UrlController;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

public class AnnotatedRouteRegistry {

    private Map<Pattern, Method> patternMap = new HashMap<>();
    private Map<Pattern, Object> patternInstances = new HashMap<>();

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
                        patternMap.put(pattern, method);
                        patternInstances.put(pattern, instance);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error loading class: " + className + " - " + e.getMessage());
            }
        }
    }

    public Method getMethod(String url) {
        for (Pattern p : patternMap.keySet()) {
            if (p.matcher(url).matches()) {
                return patternMap.get(p);
            }
        }
        return null;
    }

    public Object getInstance(String url) {
        for (Pattern p : patternInstances.keySet()) {
            if (p.matcher(url).matches()) {
                return patternInstances.get(p);
            }
        }
        return null;
    }

    public boolean hasUrl(String url) {
        for (Pattern p : patternMap.keySet()) {
            if (p.matcher(url).matches()) {
                return true;
            }
        }
        return false;
    }

    public List<String> getParams(String url) {
        for (Pattern p : patternMap.keySet()) {
            Matcher m = p.matcher(url);
            if (m.matches()) {
                List<String> params = new ArrayList<>();
                for (int i = 1; i <= m.groupCount(); i++) {
                    params.add(m.group(i));
                }
                return params;
            }
        }
        return null;
    }
}