package com.company.los.enums;

/**
 * LoanStatus нь зээлийн хүсэлтийн статусыг тодорхойлно.
 */
public enum LoanStatus {
    DRAFT,          // Ноорог
    SUBMITTED,      // Илгээсэн
    UNDER_REVIEW,   // Хянаж байгаа
    APPROVED,       // Зөвшөөрсөн
    REJECTED,       // Татгалзсан
    CANCELLED,      // Цуцалсан
    DISBURSED,      // Олгосон
    COMPLETED       // Дууссан
}