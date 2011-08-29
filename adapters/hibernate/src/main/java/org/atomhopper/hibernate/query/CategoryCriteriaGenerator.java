package org.atomhopper.hibernate.query;

import org.hibernate.Criteria;

public interface CategoryCriteriaGenerator {

    void enhanceCriteria(Criteria ongoingCriteria);
}
