package com.company.los.service.impl;

import com.company.los.entity.Customer;
import com.company.los.entity.LoanApplication;
import com.company.los.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of NotificationService
 * ⭐ ЗАСВАРЛАСАН - log variable алдаа шийдэгдсэн ⭐
 * 
 * @author LOS Development Team
 * @version 1.1 - Log variable алдаа засварлагдсан
 * @since 2025-01-01
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    @Value("${app.notification.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${app.notification.sms.enabled:false}")
    private boolean smsEnabled;
    
    @Override
    public void sendApplicationCreatedNotification(LoanApplication application) {
        log.info("Зээлийн хүсэлт үүсгэгдсэн талаар мэдэгдэл илгээж байна: {}", application.getApplicationNumber());
        
        if (emailEnabled) {
            sendEmailNotification(
                application.getCustomer().getEmail(),
                "Зээлийн хүсэлт үүсгэгдлээ",
                String.format("Таны зээлийн хүсэлт №%s амжилттай үүсгэгдлээ. Хүсэлтийн дүн: %.0f төгрөг", 
                    application.getApplicationNumber(), 
                    application.getRequestedAmount().doubleValue())
            );
        }
        
        if (smsEnabled) {
            sendSMSNotification(
                application.getCustomer().getPhone(),
                String.format("Зээлийн хүсэлт №%s үүсгэгдлээ. Дүн: %.0f төгрөг", 
                    application.getApplicationNumber(), 
                    application.getRequestedAmount().doubleValue())
            );
        }
    }
    
    @Override
    public void sendStatusUpdateNotification(LoanApplication application) {
        log.info("Зээлийн хүсэлтийн статус өөрчлөгдсөн талаар мэдэгдэл илгээж байна: {} -> {}", 
            application.getApplicationNumber(), application.getStatus());
        
        String statusMessage = getStatusMessage(application.getStatus());
        
        if (emailEnabled) {
            sendEmailNotification(
                application.getCustomer().getEmail(),
                "Зээлийн хүсэлтийн статус өөрчлөгдлөө",
                String.format("Таны зээлийн хүсэлт №%s-ийн статус: %s", 
                    application.getApplicationNumber(), statusMessage)
            );
        }
        
        if (smsEnabled) {
            sendSMSNotification(
                application.getCustomer().getPhone(),
                String.format("Зээлийн хүсэлт №%s: %s", 
                    application.getApplicationNumber(), statusMessage)
            );
        }
    }
    
    @Override
    public void sendLoanApprovedNotification(LoanApplication application) {
        log.info("Зээл зөвшөөрөгдсөн талаар мэдэгдэл илгээж байна: {}", application.getApplicationNumber());
        
        if (emailEnabled) {
            sendEmailNotification(
                application.getCustomer().getEmail(),
                "🎉 Зээлийн хүсэлт зөвшөөрөгдлөө!",
                String.format("Баяр хүргэе! Таны зээлийн хүсэлт №%s зөвшөөрөгдлөө. Зөвшөөрөгдсөн дүн: %.0f төгрөг", 
                    application.getApplicationNumber(), 
                    application.getApprovedAmount() != null ? application.getApprovedAmount().doubleValue() : 0)
            );
        }
        
        if (smsEnabled) {
            sendSMSNotification(
                application.getCustomer().getPhone(),
                String.format("Зээлийн хүсэлт №%s зөвшөөрөгдлөө! Дүн: %.0f төгрөг", 
                    application.getApplicationNumber(), 
                    application.getApprovedAmount() != null ? application.getApprovedAmount().doubleValue() : 0)
            );
        }
    }
    
    @Override
    public void sendLoanRejectedNotification(LoanApplication application) {
        log.info("Зээл татгалзсан талаар мэдэгдэл илгээж байна: {}", application.getApplicationNumber());
        
        if (emailEnabled) {
            sendEmailNotification(
                application.getCustomer().getEmail(),
                "Зээлийн хүсэлт татгалзагдлаа",
                String.format("Уучлаарай, таны зээлийн хүсэлт №%s татгалзагдлаа. Дэлгэрэнгүй мэдээллийг манай төвөөс авна уу.", 
                    application.getApplicationNumber())
            );
        }
        
        if (smsEnabled) {
            sendSMSNotification(
                application.getCustomer().getPhone(),
                String.format("Зээлийн хүсэлт №%s татгалзагдлаа. Дэлгэрэнгүй мэдээлэл: +976-1234-5678", 
                    application.getApplicationNumber())
            );
        }
    }
    
    @Override
    public void sendDocumentsRequiredNotification(LoanApplication application, List<String> requiredDocuments) {
        log.info("Нэмэлт баримт шаардлагатай талаар мэдэгдэл илгээж байна: {}", application.getApplicationNumber());
        
        String documentsStr = String.join(", ", requiredDocuments);
        
        if (emailEnabled) {
            sendEmailNotification(
                application.getCustomer().getEmail(),
                "Нэмэлт баримт бичиг шаардлагатай",
                String.format("Таны зээлийн хүсэлт №%s-д дараах баримт бичиг шаардлагатай: %s", 
                    application.getApplicationNumber(), documentsStr)
            );
        }
        
        if (smsEnabled) {
            sendSMSNotification(
                application.getCustomer().getPhone(),
                String.format("Зээлийн хүсэлт №%s-д нэмэлт баримт шаардлагатай", 
                    application.getApplicationNumber())
            );
        }
    }
    
    @Override
    public void sendLoanDisbursedNotification(LoanApplication application) {
        log.info("Зээл олгогдсон талаар мэдэгдэл илгээж байна: {}", application.getApplicationNumber());
        
        if (emailEnabled) {
            sendEmailNotification(
                application.getCustomer().getEmail(),
                "💰 Зээл амжилттай олгогдлоо!",
                String.format("Таны зээл №%s амжилттай олгогдлоо. Дүн: %.0f төгрөг", 
                    application.getApplicationNumber(), 
                    application.getApprovedAmount() != null ? application.getApprovedAmount().doubleValue() : 0)
            );
        }
        
        if (smsEnabled) {
            sendSMSNotification(
                application.getCustomer().getPhone(),
                String.format("Зээл №%s олгогдлоо! Дүн: %.0f төгрөг", 
                    application.getApplicationNumber(), 
                    application.getApprovedAmount() != null ? application.getApprovedAmount().doubleValue() : 0)
            );
        }
    }
    
    @Override
    public void sendWelcomeNotification(Customer customer) {
        log.info("Шинэ харилцагчид тавтай морил мэдэгдэл илгээж байна: {}", customer.getEmail());
        
        if (emailEnabled) {
            sendEmailNotification(
                customer.getEmail(),
                "🏦 Loan Origination System-д тавтай морилно уу!",
                String.format("Сайн байна уу %s %s! Манай зээлийн системд тавтай морилно уу. Та зээлийн хүсэлт гаргаж эхлэх боломжтой.", 
                    customer.getFirstName(), customer.getLastName())
            );
        }
        
        if (smsEnabled) {
            sendSMSNotification(
                customer.getPhone(),
                String.format("Сайн байна уу %s! LOS системд тавтай морилно уу!", 
                    customer.getFirstName())
            );
        }
    }
    
    @Override
    public void sendKYCCompletedNotification(Customer customer) {
        log.info("KYC дууссан талаар мэдэгдэл илгээж байна: {}", customer.getEmail());
        
        if (emailEnabled) {
            sendEmailNotification(
                customer.getEmail(),
                "✅ KYC процесс амжилттай дууслаа",
                String.format("Сайн байна уу %s %s! Таны танин мэдэх (KYC) процесс амжилттай дуусч, та зээлийн хүсэлт гаргах боломжтой боллоо.", 
                    customer.getFirstName(), customer.getLastName())
            );
        }
        
        if (smsEnabled) {
            sendSMSNotification(
                customer.getPhone(),
                "KYC процесс дууслаа. Та зээлийн хүсэлт гаргах боломжтой боллоо."
            );
        }
    }
    
    @Override
    public void sendPaymentReminderNotification(LoanApplication application, int daysOverdue) {
        log.info("Төлбөрийн сануулга илгээж байна: {} - {} өдөр хоцорсон", 
            application.getApplicationNumber(), daysOverdue);
        
        String message = daysOverdue > 0 
            ? String.format("Таны зээлийн төлбөр %d өдөр хоцорсон байна", daysOverdue)
            : "Таны зээлийн төлбөрийн хугацаа ойртож байна";
        
        if (emailEnabled) {
            sendEmailNotification(
                application.getCustomer().getEmail(),
                "⚠️ Зээлийн төлбөрийн сануулга",
                String.format("Зээл №%s: %s. Төлбөрөө хугацаандаа төлнө үү.", 
                    application.getApplicationNumber(), message)
            );
        }
        
        if (smsEnabled) {
            sendSMSNotification(
                application.getCustomer().getPhone(),
                String.format("Зээл №%s: %s", application.getApplicationNumber(), message)
            );
        }
    }
    
    // Helper methods
    private void sendEmailNotification(String to, String subject, String body) {
        // TODO: Implement actual email sending logic
        log.info("📧 EMAIL илгээлээ: {} -> {}", to, subject);
        // Here you would integrate with actual email service (SMTP, SendGrid, etc.)
    }
    
    private void sendSMSNotification(String phoneNumber, String message) {
        // TODO: Implement actual SMS sending logic
        log.info("📱 SMS илгээлээ: {} -> {}", phoneNumber, message);
        // Here you would integrate with actual SMS service (Twilio, etc.)
    }
    
    private String getStatusMessage(LoanApplication.ApplicationStatus status) {
        if (status == null) return "Тодорхойгүй статус";
        
        return switch (status) {
            case DRAFT -> "Ноорог";
            case SUBMITTED -> "Илгээсэн";
            case PENDING -> "Хүлээгдэж байна";
            case UNDER_REVIEW -> "Шалгагдаж байна";
            case PENDING_DOCUMENTS -> "Баримт хүлээж байна";
            case APPROVED -> "Зөвшөөрөгдсөн";
            case REJECTED -> "Татгалзсан";
            case DISBURSED -> "Олгогдсон";
            case CANCELLED -> "Цуцлагдсан";
            default -> "Тодорхойгүй статус";
        };
    }
}