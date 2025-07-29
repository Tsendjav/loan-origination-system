# 1. Dockerfile.backend үүсгэх
cat > Dockerfile.backend << 'EOF'
FROM maven:3.9-openjdk-17 AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/mvnw .
COPY backend/.mvn .mvn
RUN mvn dependency:go-offline

COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

# 2. application-dev.yml үүсгэх
mkdir -p backend/src/main/resources
cat > backend/src/main/resources/application-dev.yml << 'EOF'
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:testdb_dev
    driver-class-name: org.h2.Driver
    username: sa
    password: ''
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  sql:
    init:
      mode: always

logging:
  level:
    com.company.los: DEBUG
    org.springframework.security: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
EOF

# 3. application-prod.yml үүсгэх
cat > backend/src/main/resources/application-prod.yml << 'EOF'
spring:
  profiles:
    active: prod
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/los_prod}
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:los_user}
    password: ${DB_PASSWORD:los_password}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  sql:
    init:
      mode: never

logging:
  level:
    root: INFO
    com.company.los: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,metrics
EOF

# 4. Frontend Sidebar.tsx үүсгэх
mkdir -p frontend/src/components/layout
cat > frontend/src/components/layout/Sidebar.tsx << 'EOF'
import React from 'react';
import { Link, useLocation } from 'react-router-dom';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ isOpen, onClose }) => {
  const location = useLocation();

  const menuItems = [
    { path: '/dashboard', label: 'Хяналтын самбар', icon: '📊' },
    { path: '/customers', label: 'Харилцагчид', icon: '👥' },
    { path: '/loans', label: 'Зээлийн хүсэлт', icon: '💰' },
    { path: '/documents', label: 'Баримт бичиг', icon: '📁' },
    { path: '/reports', label: 'Тайлан', icon: '📈' },
    { path: '/settings', label: 'Тохиргоо', icon: '⚙️' },
  ];

  return (
    <div className={`fixed inset-y-0 left-0 z-50 w-64 bg-gray-800 transform ${
      isOpen ? 'translate-x-0' : '-translate-x-full'
    } transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-0`}>
      <div className="flex items-center justify-between h-16 px-4 bg-gray-900">
        <h2 className="text-xl font-semibold text-white">LOS Систем</h2>
        <button
          onClick={onClose}
          className="lg:hidden text-white hover:text-gray-300"
        >
          ✕
        </button>
      </div>
      
      <nav className="mt-8">
        {menuItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={`flex items-center px-4 py-3 text-sm text-gray-300 hover:bg-gray-700 hover:text-white ${
              location.pathname === item.path ? 'bg-gray-700 text-white' : ''
            }`}
            onClick={onClose}
          >
            <span className="mr-3 text-lg">{item.icon}</span>
            {item.label}
          </Link>
        ))}
      </nav>
      
      <div className="absolute bottom-0 w-full p-4 bg-gray-900">
        <div className="text-xs text-gray-400">
          Версий: 1.0.0
        </div>
      </div>
    </div>
  );
};

export default Sidebar;
EOF

# 5. Frontend Pages үүсгэх
mkdir -p frontend/src/pages

# CustomerPage.tsx
cat > frontend/src/pages/CustomerPage.tsx << 'EOF'
import React, { useState } from 'react';
import CustomerList from '../components/customer/CustomerList';
import CustomerForm from '../components/customer/CustomerForm';

const CustomerPage: React.FC = () => {
  const [showForm, setShowForm] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState(null);

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Харилцагчийн удирдлага</h1>
        <button
          onClick={() => setShowForm(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
        >
          + Шинэ харилцагч
        </button>
      </div>

      {showForm ? (
        <CustomerForm
          customer={selectedCustomer}
          onSave={() => {
            setShowForm(false);
            setSelectedCustomer(null);
          }}
          onCancel={() => {
            setShowForm(false);
            setSelectedCustomer(null);
          }}
        />
      ) : (
        <CustomerList
          onEdit={(customer) => {
            setSelectedCustomer(customer);
            setShowForm(true);
          }}
        />
      )}
    </div>
  );
};

export default CustomerPage;
EOF

# DashboardPage.tsx
cat > frontend/src/pages/DashboardPage.tsx << 'EOF'
import React, { useEffect, useState } from 'react';

interface DashboardStats {
  totalCustomers: number;
  totalLoans: number;
  pendingLoans: number;
  approvedLoans: number;
}

const DashboardPage: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats>({
    totalCustomers: 0,
    totalLoans: 0,
    pendingLoans: 0,
    approvedLoans: 0,
  });

  useEffect(() => {
    // Mock data - хожим API-тай холбох
    setStats({
      totalCustomers: 156,
      totalLoans: 89,
      pendingLoans: 23,
      approvedLoans: 45,
    });
  }, []);

  const StatCard: React.FC<{ title: string; value: number; icon: string; color: string }> = ({
    title, value, icon, color
  }) => (
    <div className={`bg-white p-6 rounded-lg shadow-md border-l-4 ${color}`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <p className="text-2xl font-bold text-gray-900">{value}</p>
        </div>
        <div className="text-3xl">{icon}</div>
      </div>
    </div>
  );

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Хяналтын самбар</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard
          title="Нийт харилцагч"
          value={stats.totalCustomers}
          icon="👥"
          color="border-blue-500"
        />
        <StatCard
          title="Нийт зээл"
          value={stats.totalLoans}
          icon="💰"
          color="border-green-500"
        />
        <StatCard
          title="Хүлээгдэж буй"
          value={stats.pendingLoans}
          icon="⏳"
          color="border-yellow-500"
        />
        <StatCard
          title="Зөвшөөрөгдсөн"
          value={stats.approvedLoans}
          icon="✅"
          color="border-green-500"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white p-6 rounded-lg shadow-md">
          <h3 className="text-lg font-semibold mb-4">Сүүлийн үйл ажиллагаа</h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
              <span>Шинэ харилцагч бүртгэгдлээ</span>
              <span className="text-sm text-gray-500">5 минут өмнө</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
              <span>Зээлийн хүсэлт зөвшөөрөгдлөө</span>
              <span className="text-sm text-gray-500">15 минут өмнө</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded">
              <span>Баримт бичиг хүлээн авлаа</span>
              <span className="text-sm text-gray-500">1 цаг өмнө</span>
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md">
          <h3 className="text-lg font-semibold mb-4">Системийн статус</h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span>Backend API</span>
              <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-sm">
                ✅ Ажиллаж байна
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span>Database</span>
              <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-sm">
                ✅ Холбогдсон
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span>File Storage</span>
              <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full text-sm">
                ⚠️ Шалгаж байна
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
EOF

echo "✅ Бүх дутуу файлууд амжилттай үүсгэгдлээ!"
echo "🔄 Backend дахин эхлүүлэх: cd backend && ./mvnw spring-boot:run"
echo "🎨 Frontend dependencies суулгах: cd frontend && npm install"