import React, { useEffect } from 'react';
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import MainLayout from './components/layout/MainLayout';
import { InstitutionProvider } from './contexts/InstitutionContext';
import { SessionProvider, useSession } from './contexts/SessionContext';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import CustomersPage from './pages/CustomersPage';
import LoansPage from './pages/LoansPage';
import DocumentsPage from './pages/DocumentsPage';
import ReportsPage from './pages/ReportsPage';
import AuditPage from './pages/AuditPage';
import SettingsPage from './pages/SettingsPage';

const PAGE_TITLES: Record<string, string> = {
  '/login': 'Нэвтрэх',
  '/dashboard': 'Хяналтын самбар',
  '/customers': 'Харилцагчид',
  '/loans': 'Зээлийн хүсэлтүүд',
  '/documents': 'Баримт бичиг',
  '/reports': 'Тайлан',
  '/audit': 'Аудит лог',
  '/settings': 'Тохиргоо',
};

const ProtectedLayout: React.FC = () => {
  const { isAuthenticated } = useSession();
  return isAuthenticated ? <MainLayout /> : <Navigate to="/login" replace />;
};

const GuestOnly: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useSession();
  return isAuthenticated ? <Navigate to="/dashboard" replace /> : <>{children}</>;
};

const AppRoutes: React.FC = () => {
  const location = useLocation();
  const { isAuthenticated } = useSession();

  useEffect(() => {
    const title = PAGE_TITLES[location.pathname] ?? 'Зээлийн хүсэлтийн систем';
    document.title = `${title} · LOS`;
  }, [location.pathname]);

  return (
    <Routes>
      <Route
        path="/login"
        element={
          <GuestOnly>
            <LoginPage />
          </GuestOnly>
        }
      />

      <Route element={<ProtectedLayout />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/customers" element={<CustomersPage />} />
        <Route path="/loans" element={<LoansPage />} />
        <Route path="/documents" element={<DocumentsPage />} />
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="/audit" element={<AuditPage />} />
        <Route path="/settings" element={<SettingsPage />} />
      </Route>

      <Route
        path="*"
        element={<Navigate to={isAuthenticated ? '/dashboard' : '/login'} replace />}
      />
    </Routes>
  );
};

const App: React.FC = () => (
  <BrowserRouter>
    <InstitutionProvider>
      <SessionProvider>
        <AppRoutes />
      </SessionProvider>
    </InstitutionProvider>
  </BrowserRouter>
);

export default App;
