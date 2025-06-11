package org.atomhopper.abdera.parser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EntryJsonPOJO {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Root {

        private EntryWrapper entry;

        public EntryWrapper getEntry() {
            return entry;
        }

        public void setEntry(EntryWrapper entry_data) {
            this.entry = entry_data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntryWrapper {
        //@title not sure
        private String title;
        private ContentWrapper content;

        private List<CategoryJSON> categories = new ArrayList<>();

        public List<CategoryJSON> getCategories() {
            return categories;
        }

        public void setCategories(List<CategoryJSON> categories) {
            this.categories = categories;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public ContentWrapper getContent() {
            return content;
        }

        public void setContent(ContentWrapper content) {
            this.content = content;
        }

    }

    public static class CategoryJSON{
        private String term;
        private String label;

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }


    public static class ContentWrapper {
        public Event event;

        public void setEvent(Event event) {
            this.event = event;
        }

        public Event getEvent() {
            return event;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {


        private String dataCenter;
        private String endTime;
        private String environment;
        private String startTime;
        private String eventTime;
        private String id;
        private String referenceId;
        private String region;
        private String resourceId;
        private String resourceName;
        private String resourceURI;
        private String rootAction;
        private String severity;
        private String tenantId;
        private String type;
        private String version;

        @JsonProperty("product")
        private HashMap<String, Object> productRaw;

        @JsonIgnore
        private Element product;

        public void setProductRaw(HashMap<String, Object> productRaw) {
            this.productRaw = productRaw;
            String namespace = "";
            if(productRaw.containsKey("@type")){
                namespace = String.valueOf(productRaw.get("@type"));
            }

            HashMap<String, Object> product_copy = new HashMap<>(productRaw);
            product_copy.remove("@type");

            this.product = mapToAbderaElement(product_copy, "product", namespace);
        }

        public Element getProduct(){
            return this.product;
        }

        private Element mapToAbderaElement(HashMap<String, Object> json_map, String rootName, String namespace){
            Factory factory = Abdera.getNewFactory();
            Element root = factory.newElement(new QName(namespace, rootName));

            for(Map.Entry<String, Object> entry: json_map.entrySet()){
                root.setAttributeValue(entry.getKey(), String.valueOf(entry.getValue()));
            }

            return root;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getRootAction() {
            return rootAction;
        }

        public void setRootAction(String rootAction) {
            this.rootAction = rootAction;
        }

        public String getResourceURI() {
            return resourceURI;
        }

        public void setResourceURI(String resourceURI) {
            this.resourceURI = resourceURI;
        }

        public String getResourceName() {
            return resourceName;
        }

        public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
        }

        public String getResourceId() {
            return resourceId;
        }

        public void setResourceId(String resourceId) {
            this.resourceId = resourceId;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getReferenceId() {
            return referenceId;
        }

        public void setReferenceId(String referenceId) {
            this.referenceId = referenceId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEventTime() {
            return eventTime;
        }

        public void setEventTime(String eventTime) {
            this.eventTime = eventTime;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getDataCenter() {
            return dataCenter;
        }

        public void setDataCenter(String dataCenter) {
            this.dataCenter = dataCenter;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
    }

}

