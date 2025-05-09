
package org.atomhopper.util.jsonparsingwork;
import java.util.*;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Category;
//collects the categories , manipulates and returns the list
public class CategoryUtils {

    public static List<Category> collectAllCategories(List<EntryJsonPOJO.CategoryJSON> jsonCategories, EntryJsonPOJO.Event event) {
        Factory factory = Abdera.getNewFactory();
        Map<String, Category> categoryMap = new HashMap<>();

        // Step 1: Add all user-provided categories first
        if (jsonCategories != null) {
            for (EntryJsonPOJO.CategoryJSON jsonCategory : jsonCategories) {
                if (jsonCategory.getTerm() != null) {
                    Category abderaCategory = factory.newCategory();
                    abderaCategory.setTerm(jsonCategory.getTerm());
                    if (jsonCategory.getLabel() != null) {
                        abderaCategory.setLabel(jsonCategory.getLabel());
                    }

                    // Store using raw term (not the key portion of key:value)
                    categoryMap.put(jsonCategory.getTerm(), abderaCategory);
                }
            }
        }

        // Step 2: Define standard keys and check if already provided
        Map<String, String> standardTerms = new LinkedHashMap<>();
        standardTerms.put("type", event != null ? event.getType() : null);
        standardTerms.put("dataCenter", event != null ? event.getDataCenter() : null);
        standardTerms.put("tenantId", event != null ? event.getTenantId() : null);
        standardTerms.put("region", event != null ? event.getRegion() : null);

        for (Map.Entry<String, String> entry : standardTerms.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // If not already provided by user (like type:..., region:...), add it
            boolean userProvided = categoryMap.keySet().stream().anyMatch(term -> term.startsWith(key + ":"));

            if (!userProvided && value != null) {
                Category stdCategory = factory.newCategory();
                stdCategory.setTerm(key + ":" + value);
                categoryMap.put(key + ":" + value, stdCategory);
            }
        }

        // Step 3: Add rid:<resourceId> if not already present
        if (event != null && event.getResourceId() != null) {
            boolean ridPresent = categoryMap.keySet().stream().anyMatch(term -> term.startsWith("rid:"));
            if (!ridPresent) {
                Category ridCategory = factory.newCategory();
                ridCategory.setTerm("rid:" + event.getResourceId());
                categoryMap.put("rid:" + event.getResourceId(), ridCategory);
            }
        }

        return new ArrayList<>(categoryMap.values());
    }

}

