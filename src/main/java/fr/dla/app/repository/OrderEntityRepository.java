package fr.dla.app.repository;


import fr.dla.app.domain.entities.OrderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the Order entity.
 */
@Repository
public interface OrderEntityRepository extends CrudRepository<OrderEntity, Integer> {

}
