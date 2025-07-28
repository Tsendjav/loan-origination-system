import React, { useState, useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';
import { useAuth } from '../../contexts/AuthContext';

interface MainLayoutProps {
  children?: React.ReactNode;
}

const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const { state } = useAuth();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

  // Close sidebar on mobile when route changes
  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  // Handle responsive behavior
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth >= 1024) {
        setSidebarOpen(false); // On desktop, sidebar is always visible
      }
    };

    window.addEventListener('resize', handleResize);
    handleResize(); // Initial check

    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  const toggleSidebarCollapse = () => {
    setSidebarCollapsed(!sidebarCollapsed);
  };

  // Loading state
  if (state.isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center space-y-4">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p className="text-gray-600">Системийг ачаалж байна...</p>
        </div>
      </div>
    );
  }

  // Unauthenticated state
  if (!state.isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-50">
        {children || <Outlet />}
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <Header 
        onToggleSidebar={toggleSidebar} 
        sidebarOpen={sidebarOpen}
        sidebarCollapsed={sidebarCollapsed}
      />

      <div className="flex h-screen pt-16"> {/* pt-16 to account for fixed header */}
        {/* Sidebar */}
        <Sidebar
          isOpen={sidebarOpen}
          isCollapsed={sidebarCollapsed}
          onToggleCollapse={toggleSidebarCollapse}
          onClose={() => setSidebarOpen(false)}
        />

        {/* Mobile overlay */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
            onClick={() => setSidebarOpen(false)}
          />
        )}

        {/* Main content */}
        <main
          className={`
            flex-1 overflow-auto transition-all duration-300 ease-in-out
            ${sidebarCollapsed ? 'lg:ml-16' : 'lg:ml-64'}
          `}
        >
          <div className="p-6">
            {/* Breadcrumbs */}
            <div className="mb-6">
              <Breadcrumbs />
            </div>

            {/* Page content */}
            <div className="bg-white rounded-lg shadow-sm min-h-[calc(100vh-12rem)]">
              {children || <Outlet />}
            </div>
          </div>
        </main>
      </div>

      {/* Notification Toast Container */}
      <div id="toast-container" className="fixed top-20 right-4 z-50 space-y-4">
        {/* Toasts will be rendered here */}
      </div>

      {/* Global Loading Overlay */}
      <GlobalLoadingOverlay />
    </div>
  );
};

// Breadcrumbs component
const Breadcrumbs: React.FC = () => {
  const location = useLocation();
  
  const getBreadcrumbs = (pathname: string) => {
    const paths = pathname.split('/').filter(Boolean);
    const breadcrumbs = [
      { name: 'Үндсэн хуудас', href: '/dashboard', current: false }
    ];

    const pathMap: Record<string, string> = {
      'dashboard': 'Хяналтын самбар',
      'customers': 'Харилцагчид',
      'loans': 'Зээлийн хүсэлтүүд',
      'documents': 'Баримт бичгүүд',
      'reports': 'Тайлангууд',
      'settings': 'Тохиргоо',
      'profile': 'Хувийн мэдээлэл',
      'users': 'Хэрэглэгчид',
      'roles': 'Эрхүүд',
      'audit': 'Аудит',
    };

    let currentPath = '';
    paths.forEach((path, index) => {
      currentPath += `/${path}`;
      const isLast = index === paths.length - 1;
      
      breadcrumbs.push({
        name: pathMap[path] || path,
        href: currentPath,
        current: isLast,
      });
    });

    return breadcrumbs;
  };

  const breadcrumbs = getBreadcrumbs(location.pathname);

  return (
    <nav className="flex" aria-label="Breadcrumb">
      <ol className="flex items-center space-x-4">
        {breadcrumbs.map((breadcrumb, index) => (
          <li key={breadcrumb.href}>
            <div className="flex items-center">
              {index > 0 && (
                <svg
                  className="h-5 w-5 text-gray-400 mr-4"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                  aria-hidden="true"
                >
                  <path d="M5.555 17.776l8-16 .894.448-8 16-.894-.448z" />
                </svg>
              )}
              {breadcrumb.current ? (
                <span className="text-sm font-medium text-gray-500">
                  {breadcrumb.name}
                </span>
              ) : (
                <a
                  href={breadcrumb.href}
                  className="text-sm font-medium text-gray-700 hover:text-blue-600 transition-colors"
                >
                  {breadcrumb.name}
                </a>
              )}
            </div>
          </li>
        ))}
      </ol>
    </nav>
  );
};

// Global loading overlay component
const GlobalLoadingOverlay: React.FC = () => {
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // Listen for global loading events
    const handleStartLoading = () => setIsLoading(true);
    const handleStopLoading = () => setIsLoading(false);

    window.addEventListener('app:loading:start', handleStartLoading);
    window.addEventListener('app:loading:stop', handleStopLoading);

    return () => {
      window.removeEventListener('app:loading:start', handleStartLoading);
      window.removeEventListener('app:loading:stop', handleStopLoading);
    };
  }, []);

  if (!isLoading) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center">
      <div className="bg-white rounded-lg p-6 shadow-xl flex flex-col items-center space-y-4">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <p className="text-gray-700 font-medium">Боловсруулж байна...</p>
      </div>
    </div>
  );
};

// Utility functions for global loading
export const showGlobalLoading = () => {
  window.dispatchEvent(new CustomEvent('app:loading:start'));
};

export const hideGlobalLoading = () => {
  window.dispatchEvent(new CustomEvent('app:loading:stop'));
};

// Toast notification system
interface ToastProps {
  message: string;
  type: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
}

export const showToast = ({ message, type, duration = 5000 }: ToastProps) => {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `
    px-4 py-3 rounded-lg shadow-lg max-w-sm w-full transform transition-all duration-300 ease-in-out
    ${type === 'success' ? 'bg-green-500 text-white' : ''}
    ${type === 'error' ? 'bg-red-500 text-white' : ''}
    ${type === 'warning' ? 'bg-yellow-500 text-white' : ''}
    ${type === 'info' ? 'bg-blue-500 text-white' : ''}
  `;

  toast.innerHTML = `
    <div class="flex items-center justify-between">
      <span class="text-sm font-medium">${message}</span>
      <button onclick="this.parentElement.parentElement.remove()" class="ml-3 text-white hover:text-gray-200">
        <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
        </svg>
      </button>
    </div>
  `;

  container.appendChild(toast);

  // Animate in
  requestAnimationFrame(() => {
    toast.style.transform = 'translateX(0)';
    toast.style.opacity = '1';
  });

  // Auto remove
  setTimeout(() => {
    toast.style.transform = 'translateX(100%)';
    toast.style.opacity = '0';
    setTimeout(() => {
      toast.remove();
    }, 300);
  }, duration);
};

// Error boundary for layout
interface ErrorBoundaryState {
  hasError: boolean;
  error?: Error;
}

class LayoutErrorBoundary extends React.Component<
  React.PropsWithChildren<{}>,
  ErrorBoundaryState
> {
  constructor(props: React.PropsWithChildren<{}>) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Layout Error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
          <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-6 text-center">
            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
              <svg
                className="h-6 w-6 text-red-600"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"
                />
              </svg>
            </div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              Алдаа гарлаа
            </h3>
            <p className="text-sm text-gray-500 mb-4">
              Системд алдаа гарсан байна. Хуудсыг дахин ачаална уу.
            </p>
            <button
              onClick={() => window.location.reload()}
              className="w-full bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
            >
              Дахин ачаалах
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

// Wrapped MainLayout with error boundary
const MainLayoutWithErrorBoundary: React.FC<MainLayoutProps> = (props) => (
  <LayoutErrorBoundary>
    <MainLayout {...props} />
  </LayoutErrorBoundary>
);

export default MainLayoutWithErrorBoundary;