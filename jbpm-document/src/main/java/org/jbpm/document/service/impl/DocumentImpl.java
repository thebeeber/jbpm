package org.jbpm.document.service.impl;

import org.apache.commons.io.FileUtils;
import org.jbpm.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class DocumentImpl implements Document {
    private Logger log = LoggerFactory.getLogger(DocumentImpl.class);
    private String identifier;
    private String name;
    private long size;
    private Date lastModified;

    private Map<String, String> attributes;

    public DocumentImpl(String identifier, String name, long size, Date lastModified) {
        this.identifier = identifier;
        this.name = name;
        this.size = size;
        this.lastModified = lastModified;

        attributes = new HashMap<String, String>();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public String getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    @Override
    public void addAttribute(String attributeName, String attributeValue) {
        attributes.put(attributeName, attributeValue);
    }

    @Override
    public byte[] getContent() {
        try {
            return FileUtils.readFileToByteArray(new File(this.identifier));
        } catch (IOException e) {
            this.log.error("Error reading file content: ", e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "Document{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", lastModified=" + lastModified +
                ", attributes=" + attributes +
                '}';
    }
}