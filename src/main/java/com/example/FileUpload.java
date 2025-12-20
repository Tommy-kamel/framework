package com.example;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

public class FileUpload {
    private Part part;

    public FileUpload(Part part) {
        this.part = part;
    }

    public String getFileName() {
        return part.getSubmittedFileName();
    }

    public long getSize() {
        return part.getSize();
    }

    public String getContentType() {
        return part.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    public void saveTo(String path) throws IOException {
        part.write(path);
    }
}