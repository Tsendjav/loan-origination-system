import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth, PERMISSIONS, ROLES } from '../../contexts/AuthContext';

interface SidebarProps {
  isOpen: boolean;
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  onClose: () => void;
}

interface MenuGroup {
  name: string;
  items: MenuItem[];
  permission?: string;
  role?: string;
}

interface MenuItem {
  name: string;
  href: string;
  icon: React.ReactNode;
  permission?: string;
  role?: string;
  badge?: string | number;
  subItems?: MenuItem[];
}

const Sidebar: React.FC<SidebarProps> = ({
  isOpen,
  isCollapsed,
  onToggleCollapse,
  onClose,
}) => {
  const { state, hasPermission, hasRole } = useAuth();
  const location = useLocation();
  const [expandedGroups, setExpandedGroups] = useState<string[]>(['main']);

  // Define menu structure
  const menuGroups: MenuGroup[] = [
    {
      name: 'main',
      items: [
        {
          name: 'Хяналтын самбар',
          href: '/dashboard',
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2H5a2 2 0 00-2-2V7z" />
            </svg>
          ),
        },
        {
          name: 'Харилцагчид',
          href: '/customers',
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
            </svg>
          ),
          permission: PERMISSIONS.CUSTOMER_VIEW,
          subItems: [
            {
              name: 'Жагсаалт',
              href: '/customers',
              icon: <span className="w-2 h-2 rounded-full bg-current"></span>,
              permission: PERMISSIONS.CUSTOMER_VIEW,
            },
            {
              name: 'Шинэ харилцагч',
              href: '/customers/new',
              icon: <span className="w-2 h-2 rounded-full bg-current"></span>,
              permission: PERMISSIONS.CUSTOMER_CREATE,
            },
          ],
        },
        {
          name: 'Зээлийн хүсэлтүүд',
          href: '/loans',
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
            </svg>
          ),
          permission: PERMISSIONS.LOAN_VIEW,
          badge: 5, // Dynamic badge for pending loans
          subItems: [
            {
              name: 'Бүх хүсэлт',
              href: '/loans',
              icon: <span className="w-2 h-2 rounded-full bg-current"></span>,
              permission: PERMISSIONS.LOAN_VIEW,
            },
            {
              name: 'Шинэ хүсэлт',
              href: '/loans/new',
              icon: <span className="w-2 h-2 rounded-full bg-current"></span>,
              permission: PERMISSIONS.LOAN_CREATE,
            },
            {
              name: 'Зөвшөөрөл хүлээгч',
              href: '/loans/pending',
              icon: <span className="w-2 h-2 rounded-full bg-current"></span>,
              permission: PERMISSIONS.LOAN_APPROVE,
              badge: 3,
            },
          ],
        },
        {
          name: 'Баримт бичгүүд',
          href: '/documents',
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          ),
          permission: PERMISSIONS.DOCUMENT_VIEW,
        },
      ],
    },
    {
      name: 'analytics',
      items: [
        {
          name: 'Тайлангууд',
          href: '/reports',
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          ),
          permission: PERMISSIONS.REPORT_VIEW,
          subItems: [
            {
              name: 'Зээлийн статистик',
              href: '/reports/loans',
              icon: <span className="w-2 h-2 rounded-full bg-current"></span>,
              permission: PERMISSIONS.REPORT_VIEW,
            },
            {
              name: 'Харилцагчийн тайлан',
              href: '/reports/customers',
              icon: <span className="w-2 h-2 rounded-full bg-current"></span>,
              permission: PERMISSIONS.REPORT_VIEW,
            },
            {
              name: 'Санхүүгийн тайлан',
              href: '/reports/financial',
              icon: <span className="w-2 h-2 rounded-full bg-current"></span>,
              permission: PERMISSIONS.REPORT_VIEW,
            },
          ],
        },
        {
          name: 'Аудит лог',
          href: '/audit',
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
            </svg>
          ),
          permission: PERMISSIONS.AUDIT_VIEW,
        },
      ],
    },
    {
      name: 'admin',
      items: [
        {
          name: 'Хэрэглэгчид',
          href: '/users',
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
            </svg>
          ),
          permission: PERMISSIONS.USER_MANAGE,
        },
        {
          name: 'Эрхүүд',
          href: '/roles',
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          ),
          permission: PERMISSIONS.ROLE_MANAGE,
        },
        {
          name: 'Системийн тохиргоо',
          href: '/settings',
          icon: (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          ),
          permission: PERMISSIONS.SYSTEM_ADMIN,
        },
      ],
    },
  ];

  const toggleGroup = (groupName: string) => {
    setExpandedGroups(prev =>
      prev.includes(groupName)
        ? prev.filter(g => g !== groupName)
        : [...prev, groupName]
    );
  };

  const isActive = (href: string) => {
    return location.pathname === href || 
           (href !== '/dashboard' && location.pathname.startsWith(href));
  };

  const shouldShowItem = (item: MenuItem) => {
    if (item.permission && !hasPermission(item.permission)) return false;
    if (item.role && !hasRole(item.role)) return false;
    return true;
  };

  const shouldShowGroup = (group: MenuGroup) => {
    if (group.permission && !hasPermission(group.permission)) return false;
    if (group.role && !hasRole(group.role)) return false;
    return group.items.some(shouldShowItem);
  };

  const getGroupDisplayName = (groupName: string) => {
    const names: Record<string, string> = {
      main: 'Үндсэн цэс',
      analytics: 'Шинжилгээ',
      admin: 'Удирдлага',
    };
    return names[groupName] || groupName;
  };

  return (
    <>
      {/* Sidebar */}
      <div
        className={`
          fixed top-16 left-0 z-40 h-full bg-white border-r border-gray-200 transition-all duration-300 ease-in-out
          ${isCollapsed ? 'w-16' : 'w-64'}
          ${isOpen ? 'translate-x-0' : '-translate-x-full'}
          lg:translate-x-0
        `}
      >
        <div className="flex flex-col h-full">
          {/* Collapse toggle button (desktop only) */}
          <div className="hidden lg:flex items-center justify-end p-4 border-b border-gray-200">
            <button
              onClick={onToggleCollapse}
              className="p-1.5 rounded-md text-gray-500 hover:text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-500"
              title={isCollapsed ? 'Sidebar нээх' : 'Sidebar хаах'}
            >
              <svg
                className={`w-4 h-4 transition-transform duration-200 ${isCollapsed ? 'rotate-180' : ''}`}
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
              </svg>
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-4 py-4 space-y-6 overflow-y-auto">
            {menuGroups.map((group) => {
              if (!shouldShowGroup(group)) return null;

              const isGroupExpanded = expandedGroups.includes(group.name);

              return (
                <div key={group.name} className="space-y-1">
                  {/* Group header */}
                  {!isCollapsed && group.name !== 'main' && (
                    <div className="px-3 py-2">
                      <h3 className="text-xs font-semibold text-gray-500 uppercase tracking-wider">
                        {getGroupDisplayName(group.name)}
                      </h3>
                    </div>
                  )}

                  {/* Group items */}
                  {group.items.map((item) => {
                    if (!shouldShowItem(item)) return null;

                    const hasSubItems = item.subItems && item.subItems.length > 0;
                    const isItemActive = isActive(item.href);
                    const [isSubMenuOpen, setIsSubMenuOpen] = useState(isItemActive);

                    return (
                      <div key={item.name}>
                        {/* Main item */}
                        <div className="relative">
                          {hasSubItems ? (
                            <button
                              onClick={() => setIsSubMenuOpen(!isSubMenuOpen)}
                              className={`
                                w-full flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors duration-150
                                ${isItemActive
                                  ? 'bg-blue-100 text-blue-700'
                                  : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                                }
                              `}
                            >
                              <span className="flex-shrink-0">{item.icon}</span>
                              {!isCollapsed && (
                                <>
                                  <span className="ml-3 flex-1 text-left">{item.name}</span>
                                  {item.badge && (
                                    <span className="ml-2 bg-red-100 text-red-600 text-xs font-medium px-2 py-0.5 rounded-full">
                                      {item.badge}
                                    </span>
                                  )}
                                  <svg
                                    className={`ml-2 h-4 w-4 transition-transform duration-200 ${
                                      isSubMenuOpen ? 'rotate-90' : ''
                                    }`}
                                    fill="none"
                                    stroke="currentColor"
                                    viewBox="0 0 24 24"
                                  >
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                  </svg>
                                </>
                              )}
                            </button>
                          ) : (
                            <Link
                              to={item.href}
                              onClick={onClose}
                              className={`
                                flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors duration-150
                                ${isItemActive
                                  ? 'bg-blue-100 text-blue-700'
                                  : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                                }
                              `}
                            >
                              <span className="flex-shrink-0">{item.icon}</span>
                              {!isCollapsed && (
                                <>
                                  <span className="ml-3">{item.name}</span>
                                  {item.badge && (
                                    <span className="ml-auto bg-red-100 text-red-600 text-xs font-medium px-2 py-0.5 rounded-full">
                                      {item.badge}
                                    </span>
                                  )}
                                </>
                              )}
                            </Link>
                          )}

                          {/* Tooltip for collapsed sidebar */}
                          {isCollapsed && (
                            <div className="absolute left-full top-0 ml-2 px-2 py-1 bg-gray-900 text-white text-xs rounded opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-150 whitespace-nowrap z-50">
                              {item.name}
                              {item.badge && (
                                <span className="ml-2 bg-red-500 text-white text-xs px-1 rounded-full">
                                  {item.badge}
                                </span>
                              )}
                            </div>
                          )}
                        </div>

                        {/* Sub items */}
                        {hasSubItems && !isCollapsed && isSubMenuOpen && (
                          <div className="ml-8 mt-1 space-y-1">
                            {item.subItems!.map((subItem) => {
                              if (!shouldShowItem(subItem)) return null;

                              const isSubItemActive = isActive(subItem.href);

                              return (
                                <Link
                                  key={subItem.name}
                                  to={subItem.href}
                                  onClick={onClose}
                                  className={`
                                    flex items-center px-3 py-2 text-sm rounded-md transition-colors duration-150
                                    ${isSubItemActive
                                      ? 'bg-blue-50 text-blue-700 font-medium'
                                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                                    }
                                  `}
                                >
                                  <span className="flex-shrink-0 mr-3">{subItem.icon}</span>
                                  <span className="flex-1">{subItem.name}</span>
                                  {subItem.badge && (
                                    <span className="ml-2 bg-red-100 text-red-600 text-xs font-medium px-2 py-0.5 rounded-full">
                                      {subItem.badge}
                                    </span>
                                  )}
                                </Link>
                              );
                            })}
                          </div>
                        )}
                      </div>
                    );
                  })}
                </div>
              );
            })}
          </nav>

          {/* Footer */}
          <div className="p-4 border-t border-gray-200">
            {!isCollapsed && (
              <div className="text-xs text-gray-500 text-center">
                <p>LOS v1.0.0</p>
                <p>© 2025 Company Ltd.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default Sidebar;