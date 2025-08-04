import React from 'react';
import { Spin } from 'antd';
import { LoadingOutlined } from '@ant-design/icons';

interface LoadingSpinnerProps {
  size?: 'small' | 'default' | 'large';
  tip?: string;
  spinning?: boolean;
  children?: React.ReactNode;
  className?: string;
  style?: React.CSSProperties;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 'default',
  tip = 'Ачааллаж байна...',
  spinning = true,
  children,
  className,
  style
}) => {
  const antIcon = <LoadingOutlined style={{ fontSize: getSizeValue(size) }} spin />;

  if (children) {
    return (
      <Spin 
        spinning={spinning} 
        tip={tip} 
        indicator={antIcon}
        className={className}
        style={style}
      >
        {children}
      </Spin>
    );
  }

  return (
    <div 
      className={`loading-spinner-container ${className || ''}`}
      style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '200px',
        ...style
      }}
    >
      <Spin 
        spinning={spinning} 
        tip={tip} 
        indicator={antIcon}
        size={size}
      />
    </div>
  );
};

const getSizeValue = (size: 'small' | 'default' | 'large'): number => {
  switch (size) {
    case 'small':
      return 14;
    case 'large':
      return 32;
    default:
      return 24;
  }
};

// Specialized loading components
export const PageLoadingSpinner: React.FC = () => (
  <LoadingSpinner 
    size="large" 
    tip="Хуудас ачааллаж байна..."
    style={{ 
      minHeight: '60vh',
      backgroundColor: 'rgba(255, 255, 255, 0.8)',
      position: 'absolute',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      zIndex: 1000
    }}
  />
);

export const TableLoadingSpinner: React.FC = () => (
  <LoadingSpinner 
    size="default" 
    tip="Өгөгдөл ачааллаж байна..."
    style={{ minHeight: '100px' }}
  />
);

export const ButtonLoadingSpinner: React.FC = () => (
  <LoadingOutlined style={{ marginRight: '8px' }} />
);

export const FormLoadingSpinner: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <LoadingSpinner 
    tip="Мэдээлэл ачааллаж байна..."
    style={{ minHeight: '300px' }}
  >
    {children}
  </LoadingSpinner>
);

export default LoadingSpinner;