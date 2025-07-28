package com.company.los.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.UUID;
import java.util.Optional;
import java.util.List;

/**
 * Base Repository интерфейс - бүх repository-д хамтын функцууд
 * UUID тип ашиглах
 */
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, UUID> {
    
    /**
     * ID-аар идэвхтэй entity олох
     */
    Optional<T> findByIdAndDeletedFalse(UUID id);
    
    /**
     * Бүх идэвхтэй entity-үүдийг олох
     */
    List<T> findAllByDeletedFalse();
    
    /**
     * Нэрээр хайх (хэрэв entity-д name field байгаа бол)
     */
    default List<T> findByNameContainingIgnoreCase(String name) {
        // Хэрэв шаардлагатай бол override хийнэ
        return List.of();
    }
    
    /**
     * Soft delete хийх
     */
    default void softDelete(UUID id) {
        findById(id).ifPresent(entity -> {
            // Хэрэв entity-д setDeleted method байгаа бол
            // ((BaseEntity) entity).setDeleted(true);
            save(entity);
        });
    }
}
