export const formatMnt = (amount: number): string =>
  `${new Intl.NumberFormat('mn-MN', { maximumFractionDigits: 0 }).format(amount)} ₮`;

export const formatCompactMnt = (amount: number): string => {
  if (amount >= 1_000_000_000) return `${(amount / 1_000_000_000).toFixed(1)} тэрбум ₮`;
  if (amount >= 1_000_000) return `${(amount / 1_000_000).toFixed(1)} сая ₮`;
  if (amount >= 1_000) return `${(amount / 1_000).toFixed(0)} мянга ₮`;
  return formatMnt(amount);
};

export const formatDate = (iso: string): string =>
  new Date(iso).toLocaleDateString('mn-MN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });

export const formatDateTime = (iso: string): string =>
  new Date(iso).toLocaleString('mn-MN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

export const initialsOf = (name: string): string =>
  name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('');

export const downloadCsv = (filename: string, rows: Array<Array<string | number>>): void => {
  const csv = rows
    .map((row) =>
      row
        .map((cell) => `"${String(cell).replace(/"/g, '""')}"`)
        .join(','),
    )
    .join('\r\n');

  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
};

export type LoanStatus = 'SUBMITTED' | 'IN_REVIEW' | 'APPROVED' | 'REJECTED' | 'DISBURSED';

export const LOAN_STATUS_META: Record<LoanStatus, { label: string; color: string; stroke: string }> = {
  SUBMITTED: { label: 'Илгээсэн', color: 'gold', stroke: '#d48806' },
  IN_REVIEW: { label: 'Хянаж байгаа', color: 'blue', stroke: '#1677ff' },
  APPROVED: { label: 'Зөвшөөрсөн', color: 'green', stroke: '#389e0d' },
  REJECTED: { label: 'Татгалзсан', color: 'red', stroke: '#cf1322' },
  DISBURSED: { label: 'Олгосон', color: 'cyan', stroke: '#08979c' },
};

export const KYC_META: Record<string, { label: string; color: string }> = {
  VERIFIED: { label: 'Баталгаажсан', color: 'green' },
  PENDING: { label: 'Хүлээгдэж байна', color: 'gold' },
  REJECTED: { label: 'Татгалзсан', color: 'red' },
};

export const RISK_META: Record<string, { label: string; color: string }> = {
  LOW: { label: 'Бага', color: 'green' },
  MEDIUM: { label: 'Дунд', color: 'gold' },
  HIGH: { label: 'Өндөр', color: 'red' },
};

export const DOC_STATUS_META: Record<string, { label: string; color: string }> = {
  PENDING: { label: 'Хяналт хүлээж байна', color: 'gold' },
  VERIFIED: { label: 'Баталгаажсан', color: 'green' },
  REJECTED: { label: 'Татгалзсан', color: 'red' },
};
