package com.ocommerce.api.jpa.repositories;

import com.ocommerce.api.jpa.entities.Inventory;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

public interface InventoryRepository extends ListCrudRepository<Inventory, Long> {
}