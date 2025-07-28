package com.company.los;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Зээлийн систем эхлүүлэгч
 * Loan Origination System Main Application
 * 
 * @EnableJpaRepositories болон @EnableJpaAuditing аннотацууд
 * JpaConfig.java файлд тохируулагдсан тул энд шаардлагагүй
 */
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = "com.company.los")
public class LoanOriginationApplication {

    public static void main(String[] args) {
        System.out.println("🏦 Зээлийн системийг эхлүүлж байна..."); // Starting Loan Origination System...
        SpringApplication.run(LoanOriginationApplication.class, args);
        System.out.println("✅ Зээлийн систем амжилттай эхэллээ!"); // Loan Origination System started successfully!
        System.out.println("📊 Swagger UI: http://localhost:8080/los/swagger-ui.html");
        System.out.println("🔐 Authentication API: http://localhost:8080/los/api/v1/auth");
        System.out.println("🗄️ H2 Console: http://localhost:8080/los/h2-console");
        System.out.println("💊 Health Check: http://localhost:8080/los/actuator/health");
    }
}