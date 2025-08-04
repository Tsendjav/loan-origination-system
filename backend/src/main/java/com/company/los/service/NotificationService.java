package com.company.los.service;

import com.company.los.entity.Customer;
import com.company.los.entity.LoanApplication;

/**
 * Notification service for sending various notifications
 * 
 * @author LOS Development Team
 * @version 1.0
 * @since 2025-01-01
 */
public interface NotificationService {
    
    /**
     * Send notification when loan application is created
     * 
     * @param application the loan application
     */
    void sendApplicationCreatedNotification(LoanApplication application);
    
    /**
     * Send notification when application status is updated
     * 
     * @param application the loan application
     */
    void sendStatusUpdateNotification(LoanApplication application);
    
    /**
     * Send notification when loan is approved
     * 
     * @param application the approved loan application
     */
    void sendLoanApprovedNotification(LoanApplication application);
    
    /**
     * Send notification when loan is rejected
     * 
     * @param application the rejected loan application
     */
    void sendLoanRejectedNotification(LoanApplication application);
    
    /**
     * Send notification when documents are required
     * 
     * @param application the loan application
     * @param requiredDocuments list of required documents
     */
    void sendDocumentsRequiredNotification(LoanApplication application, java.util.List<String> requiredDocuments);
    
    /**
     * Send notification when loan is disbursed
     * 
     * @param application the disbursed loan application
     */
    void sendLoanDisbursedNotification(LoanApplication application);
    
    /**
     * Send welcome notification to new customer
     * 
     * @param customer the new customer
     */
    void sendWelcomeNotification(Customer customer);
    
    /**
     * Send notification when customer KYC is completed
     * 
     * @param customer the customer
     */
    void sendKYCCompletedNotification(Customer customer);
    
    /**
     * Send payment reminder notification
     * 
     * @param application the loan application
     * @param daysOverdue number of days overdue
     */
    void sendPaymentReminderNotification(LoanApplication application, int daysOverdue);
}