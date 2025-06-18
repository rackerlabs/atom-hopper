package org.atomhopper.abdera.parser;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.*;

import javax.xml.namespace.QName;
import java.util.List;


public abstract class AbderaEntryBuilder{

    public static Entry EntryBuiderMethod(EntryJsonPOJO root) {
        Abdera abderaObj = new Abdera();
        Factory factory = abderaObj.getFactory();
        Entry entryObj = factory.newEntry();

        entryObj.declareNS("http://www.w3.org/2005/Atom", "atom");
        entryObj.declareNS("http://www.w3.org/2001/XMLSchema", "xsd");

        entryObj.setTitle(root.getEntry().getTitle());
        entryObj.setId("urn:uuid:" + root.getEntry().getContent().getEvent().getId());
        //entryObj.setUpdated(root.getEntry().getUpdated());
        //entryObj.setPublished(root.getEntry().getPublished());

        //handling the categories
        List<EntryJsonPOJO.CategoryJSON> jsonCategories = root.getEntry().getCategories();
        List<Category> abdera_entry_categories = CategoryUtils.collectAllCategories(jsonCategories, root.getEntry().getContent().getEvent());
        for(Category category: abdera_entry_categories){
            entryObj.addCategory(category);
        }

        //making the content node of entry
        Content content = factory.newContent();
        content.setContentType(Content.Type.XML);

        //making Event node
        EntryJsonPOJO.Event event_data = root.getEntry().getContent().getEvent();
        Element event = factory.newElement(new QName("http://docs.rackspace.com/core/event", "event"));
        if (event_data.getId() != null) event.setAttributeValue("id", event_data.getId());
        if (event_data.getVersion() != null) event.setAttributeValue("version", event_data.getVersion());
        if (event_data.getType() != null) event.setAttributeValue("type", event_data.getType());
        if (event_data.getTenantId() != null) event.setAttributeValue("tenantId", event_data.getTenantId());
        if (event_data.getStartTime() != null) event.setAttributeValue("startTime", event_data.getStartTime());
        if (event_data.getRegion() != null) event.setAttributeValue("region", event_data.getRegion());
        if (event_data.getResourceId() != null) event.setAttributeValue("resourceId", event_data.getResourceId());
        if (event_data.getDataCenter() != null) event.setAttributeValue("dataCenter", event_data.getDataCenter());
        if (event_data.getEndTime() != null) event.setAttributeValue("endTime", event_data.getEndTime());

        //handling the optional attributes
        if(event_data.getResourceName() != null) event.setAttributeValue("resourceName", event_data.getResourceName());
        if(event_data.getSeverity() != null) event.setAttributeValue("severity", event_data.getSeverity());
        if(event_data.getResourceURI() != null) event.setAttributeValue("resourceURI", event_data.getResourceURI());
        if(event_data.getRootAction() != null) event.setAttributeValue("resourceAction", event_data.getRootAction());
        if(event_data.getReferenceId() != null) event.setAttributeValue("referenceId", event_data.getReferenceId());
        if(event_data.getEventTime() != null) event.setAttributeValue("eventTime", event_data.getEventTime());
        if(event_data.getEnvironment() != null) event.setAttributeValue("environment", event_data.getEnvironment());


        //creating the product node dynamically

        Element productElement = event_data.getProduct();
        //adding the product node in the event node
        ExtensibleElement extensibleElement = (ExtensibleElement) event;
        ((ExtensibleElement) event).addExtension(productElement);

        //adding event to content
        content.setValueElement(event);

        //adding content to entry
        entryObj.setContent(event);

        return entryObj;
    }


}
