package com.registry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PackageScanner {

    public static List<String> getClassesInPackage(String packageName) {
        List<String> classes = new ArrayList<>();
        // Simple implementation - in a real scenario, use classpath scanning
        classes.add("com.example.TestController");
        return classes;
    }
}