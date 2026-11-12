import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { ApiError, apiRequest } from '../services/http';

export type UserRole =
  | 'SUPER_ADMIN'
  | 'MANAGER'
  | 'LOAN_OFFICER'
  | 'CREDIT_COMMITTEE'
  | 'AUDITOR';

export interface SessionUser {
  id: string;
  username: string;
  name: string;
  email: string;
  role: UserRole;
  permissions: string[];
}

export interface DemoAccount {
  username: string;
  password: string;
  name: string;
  role: UserRole;
  email: string;
}

export const ROLE_LABELS: Record<UserRole, string> = {
  SUPER_ADMIN: 'Системийн админ',
  MANAGER: 'Салбарын менежер',
  LOAN_OFFICER: 'Зээлийн мэргэжилтэн',
  CREDIT_COMMITTEE: 'Зээлийн хороо',
  AUDITOR: 'Дотоод аудитор',
};

export const PERMISSIONS = {
  CUSTOMER_VIEW: 'customer:view',
  CUSTOMER_CREATE: 'customer:create',
  CUSTOMER_UPDATE: 'customer:update',
  CUSTOMER_DELETE: 'customer:delete',
  LOAN_VIEW: 'loan:view',
  LOAN_CREATE: 'loan:create',
  LOAN_APPROVE: 'loan:approve',
  LOAN_REJECT: 'loan:reject',
  LOAN_DISBURSE: 'loan:disburse',
  DOCUMENT_VIEW: 'document:view',
  DOCUMENT_UPLOAD: 'document:upload',
  DOCUMENT_VERIFY: 'document:verify',
  REPORT_VIEW: 'report:view',
  REPORT_EXPORT: 'report:export',
  AUDIT_VIEW: 'audit:view',
  SETTINGS_MANAGE: 'settings:manage',
} as const;

const ALL_PERMISSIONS = Object.values(PERMISSIONS);

const ROLE_PERMISSIONS: Record<UserRole, string[]> = {
  SUPER_ADMIN: ALL_PERMISSIONS,
  MANAGER: [
    PERMISSIONS.CUSTOMER_VIEW,
    PERMISSIONS.CUSTOMER_CREATE,
    PERMISSIONS.CUSTOMER_UPDATE,
    PERMISSIONS.LOAN_VIEW,
    PERMISSIONS.LOAN_CREATE,
    PERMISSIONS.LOAN_APPROVE,
    PERMISSIONS.LOAN_REJECT,
    PERMISSIONS.LOAN_DISBURSE,
    PERMISSIONS.DOCUMENT_VIEW,
    PERMISSIONS.DOCUMENT_UPLOAD,
    PERMISSIONS.DOCUMENT_VERIFY,
    PERMISSIONS.REPORT_VIEW,
    PERMISSIONS.REPORT_EXPORT,
    PERMISSIONS.AUDIT_VIEW,
    PERMISSIONS.SETTINGS_MANAGE,
  ],
  LOAN_OFFICER: [
    PERMISSIONS.CUSTOMER_VIEW,
    PERMISSIONS.CUSTOMER_CREATE,
    PERMISSIONS.LOAN_VIEW,
    PERMISSIONS.LOAN_CREATE,
    PERMISSIONS.DOCUMENT_VIEW,
    PERMISSIONS.DOCUMENT_UPLOAD,
    PERMISSIONS.REPORT_VIEW,
  ],
  CREDIT_COMMITTEE: [
    PERMISSIONS.CUSTOMER_VIEW,
    PERMISSIONS.LOAN_VIEW,
    PERMISSIONS.LOAN_APPROVE,
    PERMISSIONS.LOAN_REJECT,
    PERMISSIONS.DOCUMENT_VIEW,
    PERMISSIONS.DOCUMENT_VERIFY,
    PERMISSIONS.REPORT_VIEW,
    PERMISSIONS.REPORT_EXPORT,
  ],
  AUDITOR: [
    PERMISSIONS.CUSTOMER_VIEW,
    PERMISSIONS.LOAN_VIEW,
    PERMISSIONS.DOCUMENT_VIEW,
    PERMISSIONS.REPORT_VIEW,
    PERMISSIONS.REPORT_EXPORT,
    PERMISSIONS.AUDIT_VIEW,
  ],
};

export const ROLE_PERMISSION_MATRIX: Record<UserRole, string[]> = ROLE_PERMISSIONS;

export const DEMO_ACCOUNTS: DemoAccount[] = [
  {
    username: 'admin',
    password: 'admin123',
    name: 'Дорж Баатар',
    role: 'SUPER_ADMIN',
    email: 'admin@los.mn',
  },
  {
    username: 'manager',
    password: 'manager123',
    name: 'Оюунчимэг Эрдэнэ',
    role: 'MANAGER',
    email: 'manager@los.mn',
  },
  {
    username: 'loan_officer',
    password: 'loan123',
    name: 'Тэмүүлэн Ганзориг',
    role: 'LOAN_OFFICER',
    email: 'officer@los.mn',
  },
];

interface LoginResponse {
  success?: boolean;
  token?: string;
  message?: string;
  error?: string;
  user?: {
    id?: string | number;
    username?: string;
    name?: string;
    fullName?: string;
    email?: string;
    role?: string;
    permissions?: string[];
    roles?: Array<{ name?: string }>;
  };
}

interface SessionContextValue {
  user: SessionUser | null;
  isAuthenticated: boolean;
  isDemo: boolean;
  login: (username: string, password: string) => Promise<SessionUser>;
  logout: () => Promise<void>;
  hasPermission: (permission: string) => boolean;
  hasRole: (role: UserRole) => boolean;
}

const SESSION_KEY = 'los_session_v1';
const SessionContext = createContext<SessionContextValue | undefined>(undefined);

const normalizeRole = (value?: string): UserRole => {
  const role = (value ?? '').toUpperCase();
  if (role in ROLE_LABELS) return role as UserRole;
  if (role === 'ADMIN') return 'SUPER_ADMIN';
  return 'LOAN_OFFICER';
};

const buildUser = (account: {
  id: string;
  username: string;
  name: string;
  email: string;
  role: UserRole;
  permissions?: string[];
}): SessionUser => ({
  ...account,
  permissions:
    account.role === 'SUPER_ADMIN'
      ? ['*', ...ALL_PERMISSIONS]
      : account.permissions && account.permissions.length > 0
        ? account.permissions
        : ROLE_PERMISSIONS[account.role],
});

const loadSession = (): SessionUser | null => {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as SessionUser;
    if (parsed && parsed.username && parsed.role) {
      return buildUser(parsed);
    }
  } catch {
    // Ignore corrupted session.
  }
  return null;
};

export const SessionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<SessionUser | null>(loadSession);
  const [isDemo, setIsDemo] = useState<boolean>(() => localStorage.getItem('los_demo_mode') === '1');

  const persist = useCallback((session: SessionUser | null, demo: boolean) => {
    try {
      if (session) {
        localStorage.setItem(SESSION_KEY, JSON.stringify(session));
        localStorage.setItem('los_demo_mode', demo ? '1' : '0');
      } else {
        localStorage.removeItem(SESSION_KEY);
        localStorage.removeItem('los_demo_mode');
        localStorage.removeItem('los_token');
      }
    } catch {
      // Ignore storage limitations.
    }
  }, []);

  const login = useCallback(
    async (username: string, password: string): Promise<SessionUser> => {
      const trimmed = username.trim();

      try {
        const response = await apiRequest<LoginResponse>('/auth/login', {
          method: 'POST',
          timeoutMs: 3500,
          body: {
            username: trimmed,
            password,
            rememberMe: true,
            platform: 'WEB',
            timestamp: Date.now(),
          },
        });

        if (!response?.success || !response.user) {
          throw new Error(response?.message || response?.error || 'Нэвтрэх амжилтгүй боллоо');
        }

        const role = normalizeRole(response.user.role ?? response.user.roles?.[0]?.name);
        const session = buildUser({
          id: String(response.user.id ?? trimmed),
          username: response.user.username ?? trimmed,
          name: response.user.fullName || response.user.name || trimmed,
          email: response.user.email ?? `${trimmed}@los.mn`,
          role,
          permissions: response.user.permissions,
        });

        if (response.token) localStorage.setItem('los_token', response.token);
        setUser(session);
        setIsDemo(false);
        persist(session, false);
        return session;
      } catch (error) {
        // Fall back to demo accounts when the backend is unreachable (including a proxy 5xx response).
        const backendUnreachable =
          error instanceof ApiError &&
          (error.status === 0 || error.status === 404 || error.status >= 500);

        if (backendUnreachable) {
          const account = DEMO_ACCOUNTS.find(
            (item) => item.username === trimmed && item.password === password,
          );
          if (!account) {
            throw new Error('Сервертэй холбогдох боломжгүй байна. Демо хэрэглэгчийн мэдээллээр нэвтэрнэ үү.');
          }

          const session = buildUser({
            id: `demo-${account.username}`,
            username: account.username,
            name: account.name,
            email: account.email,
            role: account.role,
          });
          setUser(session);
          setIsDemo(true);
          persist(session, true);
          return session;
        }

        if (error instanceof ApiError) {
          if (error.status === 401 || error.status === 400) {
            throw new Error('Хэрэглэгчийн нэр эсвэл нууц үг буруу байна');
          }
          throw new Error(error.message);
        }

        throw error instanceof Error ? error : new Error('Нэвтрэх үед алдаа гарлаа');
      }
    },
    [persist],
  );

  const logout = useCallback(async () => {
    try {
      if (!isDemo) await apiRequest('/auth/logout', { method: 'POST', timeoutMs: 2500 });
    } catch {
      // Local logout must work even when the backend is unavailable.
    }
    setUser(null);
    setIsDemo(false);
    persist(null, false);
  }, [isDemo, persist]);

  const hasPermission = useCallback(
    (permission: string): boolean => {
      if (!user) return false;
      return user.permissions.includes('*') || user.permissions.includes(permission);
    },
    [user],
  );

  const hasRole = useCallback(
    (role: UserRole): boolean => user?.role === role,
    [user],
  );

  useEffect(() => {
    persist(user, isDemo);
  }, [user, isDemo, persist]);

  const value = useMemo<SessionContextValue>(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isDemo,
      login,
      logout,
      hasPermission,
      hasRole,
    }),
    [user, isDemo, login, logout, hasPermission, hasRole],
  );

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
};

export const useSession = (): SessionContextValue => {
  const context = useContext(SessionContext);
  if (!context) throw new Error('useSession must be used within SessionProvider');
  return context;
};
