package com.company.los.repository;

import com.company.los.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Баримтын төрөл Repository
 * Document Type Repository Interface
 * 
 * Зөвхөн DocumentType entity-д байгаа field-үүдийг ашиглана:
 * - id, name, description, isRequired, isActive, documents
 * - createdAt, updatedAt, createdBy, updatedBy, isDeleted (BaseEntity-ээс)
 */
@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, UUID> {

    // Суурь хайлтууд - Basic Queries
    /**
     * Нэрээр хайх
     * Find by name
     */
    Optional<DocumentType> findByName(String name);

    /**
     * Нэр байгаа эсэхийг шалгах
     * Check if name exists
     */
    boolean existsByName(String name);

    // Идэвх байдлаар хайх - Active status queries
    /**
     * Идэвхтэй төрлүүд
     * Find active types
     */
    List<DocumentType> findByIsActiveTrue();

    /**
     * Идэвхгүй төрлүүд
     * Find inactive types
     */
    List<DocumentType> findByIsActiveFalse();

    /**
     * Идэвх байдлаар хайх
     * Find by active status
     */
    Page<DocumentType> findByIsActive(Boolean isActive, Pageable pageable);

    // Шаардлагатай эсэхээр хайх - Required status queries
    /**
     * Заавал шаардлагатай төрлүүд
     * Find required types
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isRequired = true AND dt.isActive = true")
    List<DocumentType> findByIsRequiredTrue();

    /**
     * Заавал шаардлагагүй төрлүүд
     * Find not required types
     */
    List<DocumentType> findByIsRequiredFalse();

    /**
     * Шаардлагатай эсэхээр хайх
     * Find by required status
     */
    Page<DocumentType> findByIsRequired(Boolean isRequired, Pageable pageable);

    // Хайлтын тоо - Count queries
    /**
     * Идэвхтэй төрлүүдийн тоо
     * Count active types
     */
    long countByIsActiveTrue();

    /**
     * Шаардлагатай төрлүүдийн тоо
     * Count required types
     */
    long countByIsRequiredTrue();

    // Огноогоор хайх - Date queries
    /**
     * Үүссэн огноогоор хайх
     * Find by creation date range
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.createdAt BETWEEN :startDate AND :endDate")
    Page<DocumentType> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate, 
                                            Pageable pageable);

    /**
     * Шинэчлэгдсэн огноогоор хайх
     * Find by update date range
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.updatedAt BETWEEN :startDate AND :endDate")
    Page<DocumentType> findByUpdatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate, 
                                            Pageable pageable);

    /**
     * Сүүлийн үүссэн төрлүүд
     * Find recently created
     */
    @Query("SELECT dt FROM DocumentType dt ORDER BY dt.createdAt DESC")
    Page<DocumentType> findRecentlyCreated(Pageable pageable);

    // Дэвшилтэт хайлт - Advanced search
    /**
     * Нэр эсвэл тайлбараар хайх
     * Search by name or description
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "LOWER(dt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(dt.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<DocumentType> searchByTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Энгийн дэвшилтэт филтер хайлт
     * Simple advanced filters using only existing fields
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "(:isRequired IS NULL OR dt.isRequired = :isRequired) AND " +
           "(:isActive IS NULL OR dt.isActive = :isActive) AND " +
           "(:minDocuments IS NULL OR SIZE(dt.documents) >= :minDocuments) AND " +
           "(:maxDocuments IS NULL OR SIZE(dt.documents) <= :maxDocuments)")
    Page<DocumentType> findByAdvancedFilters(
            @Param("isRequired") Boolean isRequired,
            @Param("isActive") Boolean isActive,
            @Param("minDocuments") Integer minDocuments,
            @Param("maxDocuments") Integer maxDocuments,
            Pageable pageable);

    // Статистик - Statistics
    /**
     * Төрөл тутмын статистик
     * Type statistics
     */
    @Query("SELECT " +
           "COUNT(dt) as totalTypes, " +
           "COUNT(CASE WHEN dt.isActive = true THEN 1 END) as activeTypes, " +
           "COUNT(CASE WHEN dt.isRequired = true THEN 1 END) as requiredTypes " +
           "FROM DocumentType dt")
    Object[] getTypeStatistics();

    /**
     * Баримттай холбогдсон статистик
     * Document association statistics
     */
    @Query("SELECT dt.name, SIZE(dt.documents) as documentCount FROM DocumentType dt " +
           "ORDER BY SIZE(dt.documents) DESC")
    List<Object[]> getDocumentCountByType();

    // Validation queries
    /**
     * Нэр давхардсан эсэхийг шалгах (өөрийгөө эс тооцох)
     * Check name uniqueness excluding self
     */
    @Query("SELECT COUNT(dt) > 0 FROM DocumentType dt WHERE " +
           "dt.name = :name AND dt.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") UUID excludeId);

    // Maintenance queries
    /**
     * Устгаж болох төрлүүд (баримт байхгүй)
     * Types that can be deleted (no documents)
     */
    @Query("SELECT dt FROM DocumentType dt WHERE SIZE(dt.documents) = 0")
    List<DocumentType> findTypesWithoutDocuments();

    /**
     * Хэрэглэгдээгүй төрлүүд
     * Unused types for specified days
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "dt.createdAt < :oldDate AND SIZE(dt.documents) = 0")
    List<DocumentType> findUnusedTypes(@Param("oldDate") LocalDateTime oldDate);

    /**
     * Архивлах төрлүүд
     * Types to archive
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "dt.isActive = false AND dt.updatedAt < :archiveDate")
    List<DocumentType> findTypesToArchive(@Param("archiveDate") LocalDateTime archiveDate);

    // Business logic queries (simplified without non-existent fields)
    /**
     * Шаардлагатай баримтын төрлүүд (энгийн хувилбар)
     * Required document types (simple version)
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isRequired = true AND dt.isActive = true")
    List<DocumentType> findRequiredDocumentTypes();

    /**
     * Идэвхтэй баримтын төрлүүд
     * Active document types
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isActive = true ORDER BY dt.name ASC")
    List<DocumentType> findActiveDocumentTypes();

    /**
     * Дутуу баримттай төрлүүд
     * Types with missing documents
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "dt.isRequired = true AND dt.isActive = true AND " +
           "SIZE(dt.documents) = 0")
    List<DocumentType> findRequiredTypesWithoutDocuments();

    /**
     * Хэт их баримттай төрлүүд
     * Types with too many documents
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "SIZE(dt.documents) > :threshold " +
           "ORDER BY SIZE(dt.documents) DESC")
    List<DocumentType> findTypesWithManyDocuments(@Param("threshold") int threshold);

    // Business methods adapted for current entity structure
    /**
     * Зээлийн хүсэлтийн шаардлагатай баримтын төрлүүд
     * Required document types for loan applications
     * (Simplified version without applicableLoanTypes field)
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isRequired = true AND dt.isActive = true")
    List<DocumentType> findRequiredForLoanType(@Param("loanType") String loanType);

    /**
     * Харилцагчийн баримтын төрлүүд
     * Document types for customers
     * (Simplified version without applicableCustomerTypes field)
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isActive = true")
    List<DocumentType> findApplicableForCustomerType(@Param("customerType") String customerType);

    // Additional utility queries
    /**
     * Нэрээр дараалалтайгаар авах
     * Find all ordered by name
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isActive = true ORDER BY dt.name ASC")
    List<DocumentType> findAllActiveOrderByName();

    /**
     * Баримтын тоогоор дараалалтайгаар авах
     * Find all ordered by document count
     */
    @Query("SELECT dt FROM DocumentType dt ORDER BY SIZE(dt.documents) DESC, dt.name ASC")
    List<DocumentType> findAllOrderByDocumentCount();

    /**
     * Тодорхой хэмжээнээс их баримттай төрлүүд
     * Types with more than specified number of documents
     */
    @Query("SELECT dt FROM DocumentType dt WHERE SIZE(dt.documents) >= :minCount")
    List<DocumentType> findTypesWithMinDocuments(@Param("minCount") int minCount);

    /**
     * Тодорхой хэмжээнээс бага баримттай төрлүүд
     * Types with less than specified number of documents
     */
    @Query("SELECT dt FROM DocumentType dt WHERE SIZE(dt.documents) < :maxCount")
    List<DocumentType> findTypesWithMaxDocuments(@Param("maxCount") int maxCount);
}