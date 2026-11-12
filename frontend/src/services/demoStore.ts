import { useSyncExternalStore } from 'react';
import type { LoanStatus } from '../utils/format';

export type CustomerType = 'INDIVIDUAL' | 'CORPORATE';
export type KycStatus = 'VERIFIED' | 'PENDING' | 'REJECTED';
export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';
export type DocumentStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';
export type ActivityType = 'customer' | 'loan' | 'document' | 'system';

export interface CustomerRecord {
  id: string;
  code: string;
  name: string;
  type: CustomerType;
  registerNo: string;
  phone: string;
  email: string;
  segment: string;
  kyc: KycStatus;
  risk: RiskLevel;
  status: 'ACTIVE' | 'INACTIVE';
  createdAt: string;
}

export interface LoanRecord {
  id: string;
  number: string;
  customerId: string;
  customerName: string;
  product: string;
  amount: number;
  termMonths: number;
  rate: number;
  status: LoanStatus;
  risk: RiskLevel;
  purpose: string;
  officer: string;
  createdAt: string;
  decisionNote?: string;
}

export interface DocumentRecord {
  id: string;
  loanId: string;
  loanNumber: string;
  customerName: string;
  name: string;
  category: string;
  status: DocumentStatus;
  uploadedAt: string;
}

export interface ActivityRecord {
  id: string;
  at: string;
  actor: string;
  action: string;
  target: string;
  type: ActivityType;
}

export interface LosState {
  customers: CustomerRecord[];
  loans: LoanRecord[];
  documents: DocumentRecord[];
  activities: ActivityRecord[];
}

const STORAGE_KEY = 'los_demo_store_v1';

const daysAgo = (days: number, hours = 0): string =>
  new Date(Date.now() - days * 86_400_000 - hours * 3_600_000).toISOString();

const createSeed = (): LosState => {
  const customers: CustomerRecord[] = [
    {
      id: 'c-1001',
      code: 'C-1001',
      name: 'Батбаяр Болд',
      type: 'INDIVIDUAL',
      registerNo: 'УБ98010112',
      phone: '9911-2233',
      email: 'batbayar@example.mn',
      segment: 'Жижиглэн',
      kyc: 'VERIFIED',
      risk: 'LOW',
      status: 'ACTIVE',
      createdAt: daysAgo(120),
    },
    {
      id: 'c-1002',
      code: 'C-1002',
      name: 'Сарангэрэл Батбаяр',
      type: 'INDIVIDUAL',
      registerNo: 'УБ99020234',
      phone: '8899-7766',
      email: 'sarangerel@example.mn',
      segment: 'Жижиглэн',
      kyc: 'VERIFIED',
      risk: 'MEDIUM',
      status: 'ACTIVE',
      createdAt: daysAgo(84),
    },
    {
      id: 'c-1003',
      code: 'C-1003',
      name: 'Ганбат Дорж',
      type: 'INDIVIDUAL',
      registerNo: 'УБ88030345',
      phone: '9911-0022',
      email: 'ganbat@example.mn',
      segment: 'Ипотек',
      kyc: 'VERIFIED',
      risk: 'LOW',
      status: 'ACTIVE',
      createdAt: daysAgo(61),
    },
    {
      id: 'c-1004',
      code: 'C-1004',
      name: 'Ануужин Төмөр',
      type: 'INDIVIDUAL',
      registerNo: 'УБ00040456',
      phone: '9555-4433',
      email: 'anuujin@example.mn',
      segment: 'Цалингийн',
      kyc: 'PENDING',
      risk: 'MEDIUM',
      status: 'ACTIVE',
      createdAt: daysAgo(21),
    },
    {
      id: 'c-1005',
      code: 'C-1005',
      name: 'Монос Фүүдс ХХК',
      type: 'CORPORATE',
      registerNo: '1234567',
      phone: '7711-2233',
      email: 'info@monosfood.mn',
      segment: 'Бизнес',
      kyc: 'VERIFIED',
      risk: 'LOW',
      status: 'ACTIVE',
      createdAt: daysAgo(205),
    },
    {
      id: 'c-1006',
      code: 'C-1006',
      name: 'Гэрэл Трейд ХХК',
      type: 'CORPORATE',
      registerNo: '7654321',
      phone: '7700-9988',
      email: 'hello@gereltrade.mn',
      segment: 'Жижиг бизнес',
      kyc: 'PENDING',
      risk: 'HIGH',
      status: 'ACTIVE',
      createdAt: daysAgo(9),
    },
  ];

  const loans: LoanRecord[] = [
    {
      id: 'l-2001',
      number: 'LN-2026-1001',
      customerId: 'c-1001',
      customerName: 'Батбаяр Болд',
      product: 'Хэрэглээний зээл',
      amount: 12_000_000,
      termMonths: 24,
      rate: 14.4,
      status: 'SUBMITTED',
      risk: 'MEDIUM',
      purpose: 'Гэрийн тавилга шинэчлэх',
      officer: 'Зээлийн мэргэжилтэн',
      createdAt: daysAgo(1, 2),
    },
    {
      id: 'l-2002',
      number: 'LN-2026-1002',
      customerId: 'c-1005',
      customerName: 'Монос Фүүдс ХХК',
      product: 'Бизнесийн зээл',
      amount: 45_000_000,
      termMonths: 36,
      rate: 13.2,
      status: 'IN_REVIEW',
      risk: 'LOW',
      purpose: 'Үйлдвэрийн тоног төхөөрөмж худалдан авах',
      officer: 'Салбарын менежер',
      createdAt: daysAgo(3),
    },
    {
      id: 'l-2003',
      number: 'LN-2026-1003',
      customerId: 'c-1002',
      customerName: 'Сарангэрэл Батбаяр',
      product: 'Хэрэглээний зээл',
      amount: 4_500_000,
      termMonths: 12,
      rate: 16.8,
      status: 'APPROVED',
      risk: 'MEDIUM',
      purpose: 'Эрүүл мэндийн зардал',
      officer: 'Зээлийн мэргэжилтэн',
      createdAt: daysAgo(6),
      decisionNote: 'Орлого болон зээлийн түүх хангалттай.',
    },
    {
      id: 'l-2004',
      number: 'LN-2026-1004',
      customerId: 'c-1003',
      customerName: 'Ганбат Дорж',
      product: 'Ипотекийн зээл',
      amount: 80_000_000,
      termMonths: 60,
      rate: 12.6,
      status: 'DISBURSED',
      risk: 'LOW',
      purpose: 'Орон сууцны урьдчилгаа',
      officer: 'Салбарын менежер',
      createdAt: daysAgo(32),
      decisionNote: 'Зээлийн гэрээ байгуулж, олголт хийсэн.',
    },
    {
      id: 'l-2005',
      number: 'LN-2026-1005',
      customerId: 'c-1006',
      customerName: 'Гэрэл Трейд ХХК',
      product: 'Бизнесийн зээл',
      amount: 25_000_000,
      termMonths: 24,
      rate: 15.0,
      status: 'REJECTED',
      risk: 'HIGH',
      purpose: 'Эргэлтийн хөрөнгө',
      officer: 'Зээлийн мэргэжилтэн',
      createdAt: daysAgo(12),
      decisionNote: 'Барьцаа хөрөнгийн үнэлгээ хүрэлцээгүй.',
    },
    {
      id: 'l-2006',
      number: 'LN-2026-1006',
      customerId: 'c-1004',
      customerName: 'Ануужин Төмөр',
      product: 'Цалингийн зээл',
      amount: 6_000_000,
      termMonths: 18,
      rate: 18.0,
      status: 'SUBMITTED',
      risk: 'MEDIUM',
      purpose: 'Боловсролын төлбөр',
      officer: 'Зээлийн мэргэжилтэн',
      createdAt: daysAgo(0, 5),
    },
  ];

  const documents: DocumentRecord[] = [
    {
      id: 'd-3001',
      loanId: 'l-2001',
      loanNumber: 'LN-2026-1001',
      customerName: 'Батбаяр Болд',
      name: 'Иргэний үнэмлэхийн хуулбар',
      category: 'Хувь хүний бичиг баримт',
      status: 'VERIFIED',
      uploadedAt: daysAgo(1),
    },
    {
      id: 'd-3002',
      loanId: 'l-2001',
      loanNumber: 'LN-2026-1001',
      customerName: 'Батбаяр Болд',
      name: 'Цалингийн тодорхойлолт',
      category: 'Орлогын баримт',
      status: 'PENDING',
      uploadedAt: daysAgo(1),
    },
    {
      id: 'd-3003',
      loanId: 'l-2002',
      loanNumber: 'LN-2026-1002',
      customerName: 'Монос Фүүдс ХХК',
      name: 'Бизнесийн гэрчилгээ',
      category: 'Байгууллагын баримт',
      status: 'VERIFIED',
      uploadedAt: daysAgo(3),
    },
    {
      id: 'd-3004',
      loanId: 'l-2002',
      loanNumber: 'LN-2026-1002',
      customerName: 'Монос Фүүдс ХХК',
      name: 'Санхүүгийн тайлан 2025',
      category: 'Санхүүгийн тайлан',
      status: 'PENDING',
      uploadedAt: daysAgo(2),
    },
    {
      id: 'd-3005',
      loanId: 'l-2005',
      loanNumber: 'LN-2026-1005',
      customerName: 'Гэрэл Трейд ХХК',
      name: 'Барьцаа хөрөнгийн үнэлгээ',
      category: 'Барьцаа хөрөнгө',
      status: 'REJECTED',
      uploadedAt: daysAgo(10),
    },
  ];

  const activities: ActivityRecord[] = [
    {
      id: 'a-1',
      at: daysAgo(0, 1),
      actor: 'Систем',
      action: 'Шинэ зээлийн хүсэлт бүртгэгдлээ',
      target: 'LN-2026-1006 · Ануужин Төмөр',
      type: 'loan',
    },
    {
      id: 'a-2',
      at: daysAgo(0, 3),
      actor: 'Зээлийн мэргэжилтэн',
      action: 'Баримт бичиг хяналтад илгээгдлээ',
      target: 'LN-2026-1001 · Цалингийн тодорхойлолт',
      type: 'document',
    },
    {
      id: 'a-3',
      at: daysAgo(1),
      actor: 'Салбарын менежер',
      action: 'Зээл зөвшөөрөгдлөө',
      target: 'LN-2026-1003 · Сарангэрэл Батбаяр',
      type: 'loan',
    },
    {
      id: 'a-4',
      at: daysAgo(2),
      actor: 'Систем',
      action: 'Шинэ харилцагч бүртгэгдлээ',
      target: 'C-1006 · Гэрэл Трейд ХХК',
      type: 'customer',
    },
    {
      id: 'a-5',
      at: daysAgo(4),
      actor: 'Аудитор',
      action: 'Баримт бичиг баталгаажлаа',
      target: 'LN-2026-1002 · Бизнесийн гэрчилгээ',
      type: 'document',
    },
  ];

  return { customers, loans, documents, activities };
};

const loadState = (): LosState => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw) as LosState;
      if (
        parsed &&
        Array.isArray(parsed.customers) &&
        Array.isArray(parsed.loans) &&
        Array.isArray(parsed.documents) &&
        Array.isArray(parsed.activities)
      ) {
        return parsed;
      }
    }
  } catch {
    // Corrupted local state falls back to the seeded demo data.
  }
  return createSeed();
};

let state: LosState = loadState();
const listeners = new Set<() => void>();

const commit = (next: LosState): void => {
  state = next;
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
  } catch {
    // Storage can be unavailable in private mode; in-memory state still works.
  }
  listeners.forEach((listener) => listener());
};

const subscribe = (listener: () => void): (() => void) => {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
};

const getSnapshot = (): LosState => state;

export const useLosStore = (): LosState => useSyncExternalStore(subscribe, getSnapshot, getSnapshot);

const uid = (prefix: string): string =>
  `${prefix}-${Date.now().toString(36)}${Math.random().toString(36).slice(2, 6)}`;

const logActivity = (
  actor: string,
  action: string,
  target: string,
  type: ActivityType,
): ActivityRecord => ({
  id: uid('a'),
  at: new Date().toISOString(),
  actor,
  action,
  target,
  type,
});

export interface NewCustomerInput {
  name: string;
  type: CustomerType;
  registerNo: string;
  phone: string;
  email: string;
  segment: string;
  kyc: KycStatus;
  risk: RiskLevel;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface NewLoanInput {
  customerId: string;
  product: string;
  amount: number;
  termMonths: number;
  rate: number;
  purpose: string;
  risk: RiskLevel;
}

export interface NewDocumentInput {
  loanId: string;
  name: string;
  category: string;
}

const nextCustomerCode = (): string => {
  const max = state.customers.reduce((acc, customer) => {
    const numeric = Number(customer.code.replace(/\D/g, ''));
    return Number.isFinite(numeric) ? Math.max(acc, numeric) : acc;
  }, 1000);
  return `C-${max + 1}`;
};

const nextLoanNumber = (): string => {
  const year = new Date().getFullYear();
  const max = state.loans.reduce((acc, loan) => {
    const numeric = Number(loan.number.split('-').pop());
    return Number.isFinite(numeric) ? Math.max(acc, numeric) : acc;
  }, 1000);
  return `LN-${year}-${max + 1}`;
};

export const losActions = {
  createCustomer(input: NewCustomerInput, actor: string): CustomerRecord {
    const customer: CustomerRecord = {
      ...input,
      id: uid('c'),
      code: nextCustomerCode(),
      createdAt: new Date().toISOString(),
    };
    commit({
      ...state,
      customers: [customer, ...state.customers],
      activities: [
        logActivity(actor, 'Шинэ харилцагч бүртгэгдлээ', `${customer.code} · ${customer.name}`, 'customer'),
        ...state.activities,
      ],
    });
    return customer;
  },

  updateCustomer(id: string, patch: Partial<NewCustomerInput>, actor: string): void {
    commit({
      ...state,
      customers: state.customers.map((customer) =>
        customer.id === id ? { ...customer, ...patch } : customer,
      ),
      activities: [
        logActivity(actor, 'Харилцагчийн мэдээлэл шинэчлэгдлээ', id, 'customer'),
        ...state.activities,
      ],
    });
  },

  deleteCustomer(id: string, actor: string): void {
    const customer = state.customers.find((item) => item.id === id);
    commit({
      ...state,
      customers: state.customers.filter((item) => item.id !== id),
      activities: [
        logActivity(actor, 'Харилцагч устгагдлаа', customer ? `${customer.code} · ${customer.name}` : id, 'customer'),
        ...state.activities,
      ],
    });
  },

  createLoan(input: NewLoanInput, actor: string): LoanRecord | null {
    const customer = state.customers.find((item) => item.id === input.customerId);
    if (!customer) return null;

    const loan: LoanRecord = {
      ...input,
      id: uid('l'),
      number: nextLoanNumber(),
      customerName: customer.name,
      status: 'SUBMITTED',
      officer: actor,
      createdAt: new Date().toISOString(),
    };

    commit({
      ...state,
      loans: [loan, ...state.loans],
      activities: [
        logActivity(actor, 'Шинэ зээлийн хүсэлт бүртгэгдлээ', `${loan.number} · ${loan.customerName}`, 'loan'),
        ...state.activities,
      ],
    });
    return loan;
  },

  setLoanStatus(id: string, status: LoanStatus, actor: string, note?: string): void {
    const loan = state.loans.find((item) => item.id === id);
    commit({
      ...state,
      loans: state.loans.map((item) =>
        item.id === id ? { ...item, status, decisionNote: note ?? item.decisionNote } : item,
      ),
      activities: [
        logActivity(
          actor,
          `Зээлийн төлөв: ${status}`,
          loan ? `${loan.number} · ${loan.customerName}` : id,
          'loan',
        ),
        ...state.activities,
      ],
    });
  },

  addDocument(input: NewDocumentInput, actor: string): void {
    const loan = state.loans.find((item) => item.id === input.loanId);
    if (!loan) return;

    const document: DocumentRecord = {
      id: uid('d'),
      loanId: loan.id,
      loanNumber: loan.number,
      customerName: loan.customerName,
      name: input.name,
      category: input.category,
      status: 'PENDING',
      uploadedAt: new Date().toISOString(),
    };

    commit({
      ...state,
      documents: [document, ...state.documents],
      activities: [
        logActivity(actor, 'Баримт бичиг хавсаргагдлаа', `${loan.number} · ${document.name}`, 'document'),
        ...state.activities,
      ],
    });
  },

  setDocumentStatus(id: string, status: DocumentStatus, actor: string): void {
    const document = state.documents.find((item) => item.id === id);
    commit({
      ...state,
      documents: state.documents.map((item) => (item.id === id ? { ...item, status } : item)),
      activities: [
        logActivity(
          actor,
          status === 'VERIFIED' ? 'Баримт бичиг баталгаажлаа' : 'Баримт бичиг буцаагдлаа',
          document ? `${document.loanNumber} · ${document.name}` : id,
          'document',
        ),
        ...state.activities,
      ],
    });
  },

  reset(): void {
    commit(createSeed());
  },
};
