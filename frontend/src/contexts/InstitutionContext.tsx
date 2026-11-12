import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';

export type InstitutionType = 'BANK' | 'NBFI';

export interface InstitutionProfile {
  type: InstitutionType;
  name: string;
  shortName: string;
  licenseNo: string;
  branch: string;
  email: string;
  phone: string;
}

export const INSTITUTION_LABEL: Record<InstitutionType, string> = {
  BANK: 'Банк',
  NBFI: 'Банк бус санхүүгийн байгууллага',
};

export const INSTITUTION_SHORT_LABEL: Record<InstitutionType, string> = {
  BANK: 'Банк',
  NBFI: 'БСБ',
};

export const DEFAULT_PROFILES: Record<InstitutionType, InstitutionProfile> = {
  BANK: {
    type: 'BANK',
    name: 'Монгол Хөгжлийн Банк',
    shortName: 'МХБ',
    licenseNo: 'БТ-2026/014',
    branch: 'Төв салбар',
    email: 'info@mdb.mn',
    phone: '7011-2233',
  },
  NBFI: {
    type: 'NBFI',
    name: 'Тэнгэр Санхүүгийн Байгууллага',
    shortName: 'ТСБ',
    licenseNo: 'СФБ-2026/082',
    branch: 'Улаанбаатар салбар',
    email: 'info@tengernbfi.mn',
    phone: '7700-4455',
  },
};

export const PRODUCT_CATALOG: Record<InstitutionType, Array<{ name: string; description: string }>> = {
  BANK: [
    { name: 'Хэрэглээний зээл', description: 'Хувь хүний өдөр тутмын хэрэгцээнд' },
    { name: 'Ипотекийн зээл', description: 'Орон сууц худалдан авах, барих' },
    { name: 'Автомашины зээл', description: 'Шинэ болон хуучин автомашин' },
    { name: 'Бизнесийн зээл', description: 'Хуулийн этгээдийн эргэлтийн хөрөнгө' },
  ],
  NBFI: [
    { name: 'Цалингийн зээл', description: 'Байгууллагын цалингийн картад суурилсан' },
    { name: 'Худалдан авалтын зээл', description: 'Бараа, үйлчилгээг хэсэгчлэн төлөх' },
    { name: 'Барьцаатай зээл', description: 'Хөдлөх болон үл хөдлөх хөрөнгийн барьцаатай' },
    { name: 'Бизнесийн зээл', description: 'Жижиг, дунд бизнесийн санхүүжилт' },
  ],
};

interface InstitutionContextValue {
  profile: InstitutionProfile;
  updateProfile: (patch: Partial<InstitutionProfile>) => void;
  switchInstitutionType: (type: InstitutionType) => void;
  products: string[];
}

const STORAGE_KEY = 'los_institution_v1';
const InstitutionContext = createContext<InstitutionContextValue | undefined>(undefined);

const loadProfile = (): InstitutionProfile => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw) as InstitutionProfile;
      if (parsed && (parsed.type === 'BANK' || parsed.type === 'NBFI') && parsed.name) {
        return { ...DEFAULT_PROFILES[parsed.type], ...parsed };
      }
    }
  } catch {
    // Fall back to the default bank profile.
  }
  return DEFAULT_PROFILES.BANK;
};

export const InstitutionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [profile, setProfile] = useState<InstitutionProfile>(loadProfile);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(profile));
    } catch {
      // Ignore storage limitations.
    }
  }, [profile]);

  const updateProfile = useCallback((patch: Partial<InstitutionProfile>) => {
    setProfile((current) => ({ ...current, ...patch }));
  }, []);

  const switchInstitutionType = useCallback((type: InstitutionType) => {
    setProfile((current) =>
      current.type === type ? current : { ...DEFAULT_PROFILES[type] },
    );
  }, []);

  const value = useMemo<InstitutionContextValue>(
    () => ({
      profile,
      updateProfile,
      switchInstitutionType,
      products: PRODUCT_CATALOG[profile.type].map((product) => product.name),
    }),
    [profile, updateProfile, switchInstitutionType],
  );

  return <InstitutionContext.Provider value={value}>{children}</InstitutionContext.Provider>;
};

export const useInstitution = (): InstitutionContextValue => {
  const context = useContext(InstitutionContext);
  if (!context) throw new Error('useInstitution must be used within InstitutionProvider');
  return context;
};
