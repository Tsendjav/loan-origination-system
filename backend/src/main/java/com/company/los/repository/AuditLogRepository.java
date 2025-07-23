package com.company.los.repository;

import com.company.los.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Аудитын лог Repository
 * Audit Log Repository Interface
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    // Суурь хайлтууд
    /**
     * Хүснэгтийн нэрээр хайх
     */
    Page<AuditLog> findByTableName(String tableName, Pageable pageable);

    /**
     * Бичлэгийн ID-гаар хайх
     */
    Page<AuditLog> findByRecordId(String recordId, Pageable pageable);

    /**
     * Үйлдлээр хайх
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Өөрчлөгчөөр хайх
     */
    Page<AuditLog> findByChangedBy(String changedBy, Pageable pageable);

    // Огноогоор хайх
    /**
     * Тодорхой хугацааны аудит лог
     */
    @Query("SELECT al FROM AuditLog al WHERE al.changedAt BETWEEN :startDate AND :endDate")
    Page<AuditLog> findByChangedAtBetween(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);

    /**
     * Өнөөдрийн аудит лог
     */
    @Query("SELECT al FROM AuditLog al WHERE DATE(al.changedAt) = CURRENT_DATE")
    List<AuditLog> findTodayAuditLogs();

    /**
     * Сүүлийн аудит лог
     */
    @Query("SELECT al FROM AuditLog al ORDER BY al.changedAt DESC")
    Page<AuditLog> findRecentAuditLogs(Pageable pageable);

    /**
     * Хуучин аудит лог
     */
    @Query("SELECT al FROM AuditLog al WHERE al.changedAt < :cutoffDate")
    List<AuditLog> findOldAuditLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Үйлдлээр хайх
    /**
     * INSERT үйлдлүүд
     */
    @Query("SELECT al FROM AuditLog al WHERE al.action = 'INSERT'")
    Page<AuditLog> findInsertActions(Pageable pageable);

    /**
     * UPDATE үйлдлүүд
     */
    @Query("SELECT al FROM AuditLog al WHERE al.action = 'UPDATE'")
    Page<AuditLog> findUpdateActions(Pageable pageable);

    /**
     * DELETE үйлдлүүд
     */
    @Query("SELECT al FROM AuditLog al WHERE al.action = 'DELETE'")
    Page<AuditLog> findDeleteActions(Pageable pageable);

    // Хүснэгт болон үйлдлээр хайх
    /**
     * Тодорхой хүснэгтийн тодорхой үйлдэл
     */
    @Query("SELECT al FROM AuditLog al WHERE al.tableName = :tableName AND al.action = :action")
    Page<AuditLog> findByTableNameAndAction(@Param("tableName") String tableName,
                                          @Param("action") String action,
                                          Pageable pageable);

    /**
     * Тодорхой бичлэгийн бүх өөрчлөлт
     */
    @Query("SELECT al FROM AuditLog al WHERE al.tableName = :tableName AND al.recordId = :recordId " +
           "ORDER BY al.changedAt DESC")
    List<AuditLog> findRecordHistory(@Param("tableName") String tableName, @Param("recordId") String recordId);

    // Хэрэглэгчийн үйл ажиллагаа
    /**
     * Хэрэглэгчийн үйл ажиллагааны түүх
     */
    @Query("SELECT al FROM AuditLog al WHERE al.changedBy = :username " +
           "ORDER BY al.changedAt DESC")
    Page<AuditLog> findUserActivity(@Param("username") String username, Pageable pageable);

    /**
     * Хэрэглэгчийн хугацааны үйл ажиллагаа
     */
    @Query("SELECT al FROM AuditLog al WHERE al.changedBy = :username AND " +
           "al.changedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY al.changedAt DESC")
    Page<AuditLog> findUserActivityBetween(@Param("username") String username,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    /**
     * Хэрэглэгчийн өнөөдрийн үйл ажиллагаа
     */
    @Query("SELECT al FROM AuditLog al WHERE al.changedBy = :username AND " +
           "DATE(al.changedAt) = CURRENT_DATE ORDER BY al.changedAt DESC")
    List<AuditLog> findUserTodayActivity(@Param("username") String username);

    // IP хаяг болон User Agent
    /**
     * IP хаягаар хайх
     */
    Page<AuditLog> findByIpAddress(String ipAddress, Pageable pageable);

    /**
     * Сэжигтэй IP хаягууд
     */
    @Query("SELECT al.ipAddress, COUNT(al) as activityCount FROM AuditLog al " +
           "WHERE al.changedAt >= :since " +
           "GROUP BY al.ipAddress " +
           "HAVING COUNT(al) > :threshold " +
           "ORDER BY activityCount DESC")
    List<Object[]> findSuspiciousIpAddresses(@Param("since") LocalDateTime since,
                                           @Param("threshold") long threshold);

    /**
     * User Agent статистик
     */
    @Query("SELECT al.userAgent, COUNT(al) FROM AuditLog al " +
           "GROUP BY al.userAgent ORDER BY COUNT(al) DESC")
    List<Object[]> getUserAgentStats();

    // Статистик
    /**
     * Хүснэгтээр статистик
     */
    @Query("SELECT al.tableName, COUNT(al) FROM AuditLog al " +
           "GROUP BY al.tableName ORDER BY COUNT(al) DESC")
    List<Object[]> getTableStats();

    /**
     * Үйлдлээр статистик
     */
    @Query("SELECT al.action, COUNT(al) FROM AuditLog al " +
           "GROUP BY al.action ORDER BY COUNT(al) DESC")
    List<Object[]> getActionStats();

    /**
     * Хэрэглэгчээр статистик
     */
    @Query("SELECT al.changedBy, COUNT(al) FROM AuditLog al " +
           "WHERE al.changedBy IS NOT NULL " +
           "GROUP BY al.changedBy ORDER BY COUNT(al) DESC")
    List<Object[]> getUserStats();

    /**
     * Өдрийн статистик
     */
    @Query("SELECT DATE(al.changedAt), COUNT(al) FROM AuditLog al " +
           "WHERE al.changedAt >= :startDate " +
           "GROUP BY DATE(al.changedAt) ORDER BY DATE(al.changedAt)")
    List<Object[]> getDailyStats(@Param("startDate") LocalDateTime startDate);

    /**
     * Цагийн статистик
     */
    @Query("SELECT HOUR(al.changedAt), COUNT(al) FROM AuditLog al " +
           "WHERE al.changedAt >= :startDate " +
           "GROUP BY HOUR(al.changedAt) ORDER BY HOUR(al.changedAt)")
    List<Object[]> getHourlyStats(@Param("startDate") LocalDateTime startDate);

    // Дэвшилтэт хайлт
    /**
     * Дэвшилтэт филтертэй хайлт
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "(:tableName IS NULL OR al.tableName = :tableName) AND " +
           "(:action IS NULL OR al.action = :action) AND " +
           "(:changedBy IS NULL OR al.changedBy = :changedBy) AND " +
           "(:recordId IS NULL OR al.recordId = :recordId) AND " +
           "(:ipAddress IS NULL OR al.ipAddress = :ipAddress) AND " +
           "(:startDate IS NULL OR al.changedAt >= :startDate) AND " +
           "(:endDate IS NULL OR al.changedAt <= :endDate)")
    Page<AuditLog> findByAdvancedFilters(
            @Param("tableName") String tableName,
            @Param("action") String action,
            @Param("changedBy") String changedBy,
            @Param("recordId") String recordId,
            @Param("ipAddress") String ipAddress,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Эрсдэлийн дүн шинжилгээ
    /**
     * Шөнийн үйл ажиллагаа
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "HOUR(al.changedAt) BETWEEN 22 AND 23 OR HOUR(al.changedAt) BETWEEN 0 AND 6")
    Page<AuditLog> findNightTimeActivity(Pageable pageable);

    /**
     * Амралтын өдрийн үйл ажиллагаа
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "DAYOFWEEK(al.changedAt) IN (1, 7)") // Sunday=1, Saturday=7
    Page<AuditLog> findWeekendActivity(Pageable pageable);

    /**
     * Олон DELETE үйлдэл хийсэн хэрэглэгчид
     */
    @Query("SELECT al.changedBy, COUNT(al) as deleteCount FROM AuditLog al " +
           "WHERE al.action = 'DELETE' AND al.changedAt >= :since " +
           "GROUP BY al.changedBy " +
           "HAVING COUNT(al) > :threshold " +
           "ORDER BY deleteCount DESC")
    List<Object[]> findUsersWithManyDeletes(@Param("since") LocalDateTime since,
                                          @Param("threshold") long threshold);

    /**
     * Бичлэг олон удаа өөрчлөгдсөн
     */
    @Query("SELECT al.tableName, al.recordId, COUNT(al) as changeCount FROM AuditLog al " +
           "WHERE al.changedAt >= :since " +
           "GROUP BY al.tableName, al.recordId " +
           "HAVING COUNT(al) > :threshold " +
           "ORDER BY changeCount DESC")
    List<Object[]> findFrequentlyChangedRecords(@Param("since") LocalDateTime since,
                                              @Param("threshold") long threshold);

    // Тоолох функцууд
    /**
     * Хүснэгтийн өөрчлөлтийн тоо
     */
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.tableName = :tableName")
    long countByTableName(@Param("tableName") String tableName);

    /**
     * Хэрэглэгчийн үйл ажиллагааны тоо
     */
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.changedBy = :username")
    long countByUser(@Param("username") String username);

    /**
     * Өдрийн үйл ажиллагааны тоо
     */
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE DATE(al.changedAt) = CURRENT_DATE")
    long countTodayActivity();

    /**
     * Хугацааны үйл ажиллагааны тоо
     */
    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.changedAt BETWEEN :startDate AND :endDate")
    long countActivityBetween(@Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);

    // Cleanup functions
    /**
     * Хуучин аудит лог устгах
     */
    @Modifying
    @Query("DELETE FROM AuditLog al WHERE al.changedAt < :cutoffDate")
    int deleteOldAuditLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Тодорхой хүснэгтийн хуучин лог устгах
     */
    @Modifying
    @Query("DELETE FROM AuditLog al WHERE al.tableName = :tableName AND al.changedAt < :cutoffDate")
    int deleteOldTableAuditLogs(@Param("tableName") String tableName,
                              @Param("cutoffDate") LocalDateTime cutoffDate);

    // Data integrity
    /**
     * Хүснэгтийн нэргүй лог
     */
    @Query("SELECT al FROM AuditLog al WHERE al.tableName IS NULL OR al.tableName = ''")
    List<AuditLog> findLogsWithoutTableName();

    /**
     * Бичлэгийн ID-гүй лог
     */
    @Query("SELECT al FROM AuditLog al WHERE al.recordId IS NULL OR al.recordId = ''")
    List<AuditLog> findLogsWithoutRecordId();

    /**
     * Үйлдэлгүй лог
     */
    @Query("SELECT al FROM AuditLog al WHERE al.action IS NULL OR al.action = ''")
    List<AuditLog> findLogsWithoutAction();

    // Dashboard статистик
    /**
     * Dashboard-ийн статистик
     */
    @Query("SELECT " +
           "COUNT(al) as totalLogs, " +
           "COUNT(CASE WHEN DATE(al.changedAt) = CURRENT_DATE THEN 1 END) as todayLogs, " +
           "COUNT(CASE WHEN al.action = 'INSERT' THEN 1 END) as insertCount, " +
           "COUNT(CASE WHEN al.action = 'UPDATE' THEN 1 END) as updateCount, " +
           "COUNT(CASE WHEN al.action = 'DELETE' THEN 1 END) as deleteCount, " +
           "COUNT(DISTINCT al.changedBy) as uniqueUsers, " +
           "COUNT(DISTINCT al.tableName) as uniqueTables " +
           "FROM AuditLog al")
    Object[] getDashboardStats();

    /**
     * Сүүлийн үйл ажиллагаа
     */
    @Query("SELECT al FROM AuditLog al ORDER BY al.changedAt DESC LIMIT :limit")
    List<AuditLog> findRecentActivity(@Param("limit") int limit);

    // Search functionality
    /**
     * JSON өгөгдөлөөс хайх
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.oldValues LIKE CONCAT('%', :searchTerm, '%') OR " +
           "al.newValues LIKE CONCAT('%', :searchTerm, '%')")
    Page<AuditLog> searchInJsonData(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Бүх талбараас хайх
     */
    @Query("SELECT al FROM AuditLog al WHERE " +
           "al.tableName LIKE CONCAT('%', :searchTerm, '%') OR " +
           "al.recordId LIKE CONCAT('%', :searchTerm, '%') OR " +
           "al.action LIKE CONCAT('%', :searchTerm, '%') OR " +
           "al.changedBy LIKE CONCAT('%', :searchTerm, '%') OR " +
           "al.oldValues LIKE CONCAT('%', :searchTerm, '%') OR " +
           "al.newValues LIKE CONCAT('%', :searchTerm, '%')")
    Page<AuditLog> searchAll(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Performance monitoring
    /**
     * Хамгийн идэвхтэй хэрэглэгчид
     */
    @Query("SELECT al.changedBy, COUNT(al) as activityCount FROM AuditLog al " +
           "WHERE al.changedAt >= :since AND al.changedBy IS NOT NULL " +
           "GROUP BY al.changedBy " +
           "ORDER BY activityCount DESC")
    Page<Object[]> findMostActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Хамгийн их өөрчлөгддөг хүснэгтүүд
     */
    @Query("SELECT al.tableName, COUNT(al) as changeCount FROM AuditLog al " +
           "WHERE al.changedAt >= :since " +
           "GROUP BY al.tableName " +
           "ORDER BY changeCount DESC")
    Page<Object[]> findMostChangedTables(@Param("since") LocalDateTime since, Pageable pageable);
}