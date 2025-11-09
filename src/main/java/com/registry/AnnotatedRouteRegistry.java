package com.registry;

import com.annotation.UrlController;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AnnotatedRouteRegistry {

    private Map<String, Method> urlMap = new HashMap<>();
    private Map<String, Object> instances = new HashMap<>();

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
                        urlMap.put(url, method);
                        instances.put(url, instance);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error loading class: " + className + " - " + e.getMessage());
            }
        }
    }

    public Method getMethod(String url) {
        return urlMap.get(url);
    }

    public Object getInstance(String url) {
        return instances.get(url);
    }

    public boolean hasUrl(String url) {
        return urlMap.containsKey(url);
    }
}