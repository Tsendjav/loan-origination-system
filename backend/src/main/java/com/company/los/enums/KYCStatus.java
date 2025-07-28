package com.company.los.enums;

/**
 * KYCStatus нь хэрэглэгчийн KYC (Know Your Customer) статусыг тодорхойлно.
 */
public enum KYCStatus {
    PENDING,        // Хүлээгдэж байгаа
    IN_PROGRESS,    // Үргэлжилж байна
    COMPLETED,      // Дууссан
    REJECTED,       // Татгалзсан
    FAILED          // Амжилтгүй
}