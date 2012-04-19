package org.atomhopper.mongodb.query;

import org.springframework.data.mongodb.core.query.Query;

public interface CategoryCriteriaGenerator {

    void enhanceCriteria(Query ongoingQuery);
}
