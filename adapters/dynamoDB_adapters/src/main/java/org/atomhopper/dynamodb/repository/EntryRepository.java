package org.atomhopper.dynamodb.repository;

import org.atomhopper.dynamodb.model.PersistedEntry;
import org.springframework.data.repository.CrudRepository;

public interface EntryRepository extends CrudRepository<PersistedEntry,String> {
}
