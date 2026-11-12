import React from 'react';
import ReactDOM from 'react-dom/client';
import { App as AntApp, ConfigProvider } from 'antd';
import mnMN from 'antd/locale/mn_MN';
import App from './App';
import 'antd/dist/reset.css';
import './styles/shell.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ConfigProvider
      locale={mnMN}
      theme={{
        token: {
          colorPrimary: '#1d4ed8',
          colorInfo: '#1d4ed8',
          borderRadius: 10,
          fontFamily:
            "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', sans-serif",
          colorTextBase: '#0f172a',
          colorBgLayout: '#f4f6fb',
        },
        components: {
          Layout: { headerBg: '#ffffff', siderBg: '#ffffff', bodyBg: '#f4f6fb' },
          Menu: { itemBorderRadius: 8, itemHeight: 42 },
          Card: { borderRadiusLG: 14 },
          Button: { controlHeight: 36 },
        },
      }}
    >
      <AntApp>
        <App />
      </AntApp>
    </ConfigProvider>
  </React.StrictMode>,
);
