package org.jbpm.document.marshalling;

import org.drools.core.common.DroolsObjectInputStream;
import org.jbpm.document.Document;
import org.jbpm.document.service.DocumentStorageService;
import org.jbpm.document.service.impl.DocumentStorageServiceImpl;
import org.kie.api.marshalling.ObjectMarshallingStrategy;

import java.io.*;

public class DocumentMarshallingStrategy implements ObjectMarshallingStrategy {

    private DocumentStorageService documentStorageService;

    @Override
    public boolean accept(Object o) {
        return o instanceof Document;
    }

    @Override
    public void write(ObjectOutputStream os, Object object) throws IOException {
        Document document = (Document) object;

        if (document != null && document.getContent() != null) {
            getDocumentStorageService().saveDocument(document, document.getContent());
        }
        os.writeUTF(document.getIdentifier());
        os.writeUTF(object.getClass().getCanonicalName());
    }

    public Object read(ObjectInputStream os) throws IOException, ClassNotFoundException {
        String objectId = os.readUTF();
        String canonicalName = os.readUTF();
        try {
            Document doc = getDocumentStorageService().getDocument(objectId);
            Document document = (Document) Class.forName(canonicalName).newInstance();

            document.setIdentifier(objectId);
            document.setName(doc.getName());
            document.setSize(doc.getSize());
            document.setLastModified(doc.getLastModified());
            document.setAttributes(doc.getAttributes());
            document.setContent(doc.getContent());
            return document;
        } catch(Exception e) {
            throw new RuntimeException("Cannot read document", e);
        }
    }

    @Override
    public byte[] marshal(Context context, ObjectOutputStream objectOutputStream, Object o) throws IOException {
        Document document = (Document) o;
        getDocumentStorageService().saveDocument(document, document.getContent());
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(buff);
        oos.writeUTF(document.getIdentifier());
        oos.writeUTF(document.getClass().getCanonicalName());
        oos.close();
        return buff.toByteArray();
    }

    @Override
    public Object unmarshal(Context context, ObjectInputStream objectInputStream, byte[] object, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        DroolsObjectInputStream is = new DroolsObjectInputStream(new ByteArrayInputStream(object), classLoader);
        // first we read out the object id and class name we stored during marshaling
        String objectId = is.readUTF();
        String canonicalName = is.readUTF();

        Document document = null;
        try {
            document = (Document) Class.forName(canonicalName).newInstance();
            Document storedDoc = getDocumentStorageService().getDocument(objectId);

            document.setIdentifier(storedDoc.getIdentifier());
            document.setName(storedDoc.getName());
            document.setLastModified(storedDoc.getLastModified());
            document.setSize(storedDoc.getSize());
            document.setAttributes(storedDoc.getAttributes());
            document.setContent(storedDoc.getContent());
        } catch (Exception e) {
            throw new RuntimeException("Cannot read document from storage service", e);
        }
        return document;
    }

    @Override
    public Context createContext() {
        return null;
    }

    public DocumentStorageService getDocumentStorageService() {
        if (documentStorageService == null) {
            documentStorageService = new DocumentStorageServiceImpl();
        }
        return documentStorageService;
    }
}
