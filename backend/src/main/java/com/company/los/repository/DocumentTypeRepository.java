package com.company.los.repository;

import com.company.los.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Баримт бичгийн төрлийн Repository
 * Document Type Repository Interface
 */
@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, String>  {

    // Суурь хайлтууд
    /**
     * Нэрээр хайх
     */
    List<DocumentType> findByIsActive(Boolean isActive);
    
    Optional<DocumentType> findByName(String name);

    /**
     * Нэр байгаа эсэхийг шалгах
     */
    boolean existsByName(String name);

    // Шаардлагатай эсэхээр хайх
    /**
     * Заавал шаардлагатай баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isRequired = true ORDER BY dt.name")
    List<DocumentType> findRequiredDocumentTypes();

    /**
     * Сонголттой баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isRequired = false ORDER BY dt.name")
    List<DocumentType> findOptionalDocumentTypes();

    /**
     * Шаардлагатай эсэхээр хайх (pageable)
     */
    Page<DocumentType> findByIsRequired(Boolean isRequired, Pageable pageable);

    // Нэрээр хайх
    /**
     * Нэрээр хайх (хэсэгчилсэн)
     */
    @Query("SELECT dt FROM DocumentType dt WHERE LOWER(dt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<DocumentType> findByNameContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Тайлбараар хайх
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "LOWER(COALESCE(dt.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<DocumentType> findByDescriptionContaining(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Ерөнхий хайлт
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "LOWER(dt.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(COALESCE(dt.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<DocumentType> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Баримттай холбоотой
    /**
     * Баримттай баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE SIZE(dt.documents) > 0")
    Page<DocumentType> findDocumentTypesWithDocuments(Pageable pageable);

    /**
     * Баримтгүй баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE SIZE(dt.documents) = 0")
    List<DocumentType> findDocumentTypesWithoutDocuments();

    /**
     * Баримтын тоогоор хайх
     */
    @Query("SELECT dt FROM DocumentType dt WHERE SIZE(dt.documents) >= :minDocuments")
    List<DocumentType> findDocumentTypesWithMinimumDocuments(@Param("minDocuments") int minDocuments);

    // Статистик
    /**
     * Шаардлагатай эсэхээр тоолох
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN dt.isRequired = true THEN 1 END) as requiredTypes, " +
           "COUNT(CASE WHEN dt.isRequired = false THEN 1 END) as optionalTypes, " +
           "COUNT(dt) as totalTypes " +
           "FROM DocumentType dt")
    Object[] getDocumentTypeStats();

    /**
     * Баримтын тоогоор статистик
     */
    @Query("SELECT dt.name, SIZE(dt.documents) as documentCount FROM DocumentType dt " +
           "ORDER BY SIZE(dt.documents) DESC")
    List<Object[]> getDocumentCountByType();

    /**
     * Хамгийн их ашиглагддаг баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE SIZE(dt.documents) > 0 " +
           "ORDER BY SIZE(dt.documents) DESC")
    Page<DocumentType> findMostUsedDocumentTypes(Pageable pageable);

    /**
     * Хамгийн бага ашиглагддаг баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt " +
           "ORDER BY SIZE(dt.documents) ASC")
    Page<DocumentType> findLeastUsedDocumentTypes(Pageable pageable);

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT dt FROM DocumentType dt WHERE " +
           "(:isRequired IS NULL OR dt.isRequired = :isRequired) AND " +
           "(:hasDocuments IS NULL OR (SIZE(dt.documents) > 0) = :hasDocuments) AND " +
           "(:minDocuments IS NULL OR SIZE(dt.documents) >= :minDocuments) AND " +
           "(:maxDocuments IS NULL OR SIZE(dt.documents) <= :maxDocuments)")
    Page<DocumentType> findByAdvancedFilters(
            @Param("isRequired") Boolean isRequired,
            @Param("hasDocuments") Boolean hasDocuments,
            @Param("minDocuments") Integer minDocuments,
            @Param("maxDocuments") Integer maxDocuments,
            Pageable pageable);

    // Bulk операциуд
    /**
     * Олон баримтын төрлийг шаардлагатай гэж тэмдэглэх
     */
    @Modifying
    @Query("UPDATE DocumentType dt SET dt.isRequired = true, dt.updatedBy = :updatedBy " +
           "WHERE dt.id IN :documentTypeIds")
    int markAsRequired(@Param("documentTypeIds") List<String> documentTypeIds,
                     @Param("updatedBy") String updatedBy);

    /**
     * Олон баримтын төрлийг сонголттой гэж тэмдэглэх
     */
    @Modifying
    @Query("UPDATE DocumentType dt SET dt.isRequired = false, dt.updatedBy = :updatedBy " +
           "WHERE dt.id IN :documentTypeIds")
    int markAsOptional(@Param("documentTypeIds") List<String> documentTypeIds,
                     @Param("updatedBy") String updatedBy);

    /**
     * Тайлбар өөрчлөх
     */
    @Modifying
    @Query("UPDATE DocumentType dt SET dt.description = :description, dt.updatedBy = :updatedBy " +
           "WHERE dt.id = :documentTypeId")
    int updateDescription(@Param("documentTypeId") String documentTypeId,
                        @Param("description") String description,
                        @Param("updatedBy") String updatedBy);

    // Validation
    /**
     * Нэр давхцаж байгаа эсэхийг шалгах
     */
    @Query("SELECT COUNT(dt) > 0 FROM DocumentType dt WHERE " +
           "LOWER(dt.name) = LOWER(:name) AND dt.id != :excludeId")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("excludeId") String excludeId);

    // Business logic
    /**
     * Зээлийн төрлөөр шаардлагатай баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isRequired = true")
    List<DocumentType> findRequiredForAllLoans();

    /**
     * Системд хэрэглэгддэг бүх баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt ORDER BY dt.isRequired DESC, dt.name ASC")
    List<DocumentType> findAllOrderedByRequiredAndName();

    /**
     * Идэвхтэй баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isActive = true ORDER BY dt.name")
    List<DocumentType> findActiveDocumentTypes();

    /**
     * Идэвхгүй баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.isActive = false")
    List<DocumentType> findInactiveDocumentTypes();

    // Data quality
    /**
     * Тайлбаргүй баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.description IS NULL OR dt.description = ''")
    List<DocumentType> findDocumentTypesWithoutDescription();

    /**
     * Нэр алдаатай баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.name IS NULL OR dt.name = ''")
    List<DocumentType> findDocumentTypesWithInvalidNames();

    // Dashboard статистик
    /**
     * Dashboard-ийн статистик
     */
    @Query("SELECT " +
           "COUNT(dt) as totalTypes, " +
           "COUNT(CASE WHEN dt.isRequired = true THEN 1 END) as requiredTypes, " +
           "COUNT(CASE WHEN SIZE(dt.documents) > 0 THEN 1 END) as usedTypes, " +
           "COUNT(CASE WHEN SIZE(dt.documents) = 0 THEN 1 END) as unusedTypes, " +
           "AVG(SIZE(dt.documents)) as avgDocumentsPerType " +
           "FROM DocumentType dt")
    Object[] getDashboardStats();

    // Cleanup
    /**
     * Ашиглагдаагүй баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE SIZE(dt.documents) = 0 AND dt.isRequired = false")
    List<DocumentType> findUnusedNonRequiredTypes();

    /**
     * Устгах боломжтой баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE SIZE(dt.documents) = 0 AND " +
           "dt.isRequired = false AND dt.isActive = false")
    List<DocumentType> findDeletableDocumentTypes();

    // Template methods
    /**
     * Стандарт баримтын төрлүүд үүсгэх
     */
    @Query("SELECT COUNT(dt) FROM DocumentType dt WHERE dt.name IN " +
           "('NATIONAL_ID', 'INCOME_STATEMENT', 'BANK_STATEMENT', 'LOAN_APPLICATION')")
    int countStandardDocumentTypes();

    /**
     * Дутуу стандарт баримтын төрлүүд
     */
    @Query("SELECT CASE " +
           "WHEN NOT EXISTS (SELECT 1 FROM DocumentType dt WHERE dt.name = 'NATIONAL_ID') THEN 'NATIONAL_ID' " +
           "WHEN NOT EXISTS (SELECT 1 FROM DocumentType dt WHERE dt.name = 'INCOME_STATEMENT') THEN 'INCOME_STATEMENT' " +
           "WHEN NOT EXISTS (SELECT 1 FROM DocumentType dt WHERE dt.name = 'BANK_STATEMENT') THEN 'BANK_STATEMENT' " +
           "WHEN NOT EXISTS (SELECT 1 FROM DocumentType dt WHERE dt.name = 'LOAN_APPLICATION') THEN 'LOAN_APPLICATION' " +
           "ELSE NULL END as missingType " +
           "FROM DocumentType dt LIMIT 1")
    List<String> findMissingStandardTypes();

    // Performance monitoring
    /**
     * Өнөөдөр үүсгэсэн баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt WHERE DATE(dt.createdAt) = CURRENT_DATE")
    List<DocumentType> findCreatedToday();

    /**
     * Сүүлийн өөрчлөлт хийсэн баримтын төрлүүд
     */
    @Query("SELECT dt FROM DocumentType dt ORDER BY dt.updatedAt DESC")
    Page<DocumentType> findRecentlyModified(Pageable pageable);
}