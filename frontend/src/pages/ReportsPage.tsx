import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Button,
  Card,
  Col,
  Empty,
  Progress,
  Result,
  Row,
  Statistic,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { DownloadOutlined, PrinterOutlined } from '@ant-design/icons';
import { useInstitution } from '../contexts/InstitutionContext';
import { PERMISSIONS, useSession } from '../contexts/SessionContext';
import { useLosStore } from '../services/demoStore';
import { LOAN_STATUS_META, downloadCsv, formatMnt, type LoanStatus } from '../utils/format';

const { Title, Text } = Typography;

const STATUS_ORDER: LoanStatus[] = ['SUBMITTED', 'IN_REVIEW', 'APPROVED', 'DISBURSED', 'REJECTED'];

interface ProductRow {
  key: string;
  product: string;
  count: number;
  amount: number;
  approved: number;
}

const ReportsPage: React.FC = () => {
  const navigate = useNavigate();
  const { hasPermission } = useSession();
  const { profile } = useInstitution();
  const { loans } = useLosStore();

  const totals = useMemo(() => {
    const portfolio = loans.reduce((sum, loan) => sum + loan.amount, 0);
    const disbursed = loans
      .filter((loan) => loan.status === 'DISBURSED')
      .reduce((sum, loan) => sum + loan.amount, 0);
    const approved = loans.filter(
      (loan) => loan.status === 'APPROVED' || loan.status === 'DISBURSED',
    ).length;
    return {
      portfolio,
      disbursed,
      average: loans.length === 0 ? 0 : Math.round(portfolio / loans.length),
      approvalRate: loans.length === 0 ? 0 : Math.round((approved / loans.length) * 100),
    };
  }, [loans]);

  const productRows = useMemo<ProductRow[]>(() => {
    const map = new Map<string, ProductRow>();
    loans.forEach((loan) => {
      const current = map.get(loan.product) ?? {
        key: loan.product,
        product: loan.product,
        count: 0,
        amount: 0,
        approved: 0,
      };
      current.count += 1;
      current.amount += loan.amount;
      if (loan.status === 'APPROVED' || loan.status === 'DISBURSED') current.approved += 1;
      map.set(loan.product, current);
    });
    return Array.from(map.values()).sort((a, b) => b.amount - a.amount);
  }, [loans]);

  if (!hasPermission(PERMISSIONS.REPORT_VIEW)) {
    return (
      <Result
        status="403"
        title="Хандах эрхгүй"
        subTitle="Тайлан үзэх эрх танд байхгүй байна."
        extra={<Button onClick={() => navigate('/dashboard')}>Хяналтын самбар руу буцах</Button>}
      />
    );
  }

  const exportReport = (): void => {
    downloadCsv('portfolio-report.csv', [
      ['Бүтээгдэхүүн', 'Хүсэлтийн тоо', 'Нийт дүн', 'Зөвшөөрөгдсөн'],
      ...productRows.map((row) => [row.product, row.count, row.amount, row.approved]),
      [],
      ['Нийт портфель', loans.length, totals.portfolio, totals.approvalRate],
    ]);
    message.success('Тайлан татагдлаа');
  };

  const columns: ColumnsType<ProductRow> = [
    { title: 'Бүтээгдэхүүн', dataIndex: 'product', key: 'product' },
    { title: 'Хүсэлт', dataIndex: 'count', key: 'count', align: 'right' },
    {
      title: 'Нийт дүн',
      dataIndex: 'amount',
      key: 'amount',
      align: 'right',
      render: (value: number) => formatMnt(value),
    },
    { title: 'Зөвшөөрөгдсөн', dataIndex: 'approved', key: 'approved', align: 'right' },
  ];

  return (
    <div>
      <div className="page-head">
        <div>
          <Title level={3} className="page-head__title">Портфелийн тайлан</Title>
          <Text className="page-head__sub">
            {profile.name} · {profile.branch} · {new Date().toLocaleDateString('mn-MN')}
          </Text>
        </div>
        <div className="page-head__actions">
          <Button icon={<PrinterOutlined />} onClick={() => window.print()}>
            Хэвлэх
          </Button>
          {hasPermission(PERMISSIONS.REPORT_EXPORT) && (
            <Button type="primary" icon={<DownloadOutlined />} onClick={exportReport}>
              CSV татах
            </Button>
          )}
        </div>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} xl={6}>
          <Card>
            <Statistic title="Нийт хүсэлтийн дүн" value={totals.portfolio} suffix="₮" />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card>
            <Statistic title="Олгосон дүн" value={totals.disbursed} suffix="₮" valueStyle={{ color: '#08979c' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card>
            <Statistic title="Дундаж зээл" value={totals.average} suffix="₮" />
          </Card>
        </Col>
        <Col xs={24} sm={12} xl={6}>
          <Card>
            <Statistic title="Зөвшөөрлийн хувь" value={totals.approvalRate} suffix="%" valueStyle={{ color: '#389e0d' }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} xl={14}>
          <Card title="Бүтээгдэхүүний задаргаа" className="section-card">
            {productRows.length === 0 ? (
              <Empty description="Өгөгдөл байхгүй" />
            ) : (
              <Table<ProductRow>
                rowKey="key"
                columns={columns}
                dataSource={productRows}
                pagination={false}
                size="middle"
                scroll={{ x: 560 }}
              />
            )}
          </Card>
        </Col>
        <Col xs={24} xl={10}>
          <Card title="Төлвийн задаргаа" className="section-card">
            <div className="stack-16">
              {STATUS_ORDER.map((status) => {
                const count = loans.filter((loan) => loan.status === status).length;
                const percent = loans.length === 0 ? 0 : Math.round((count / loans.length) * 100);
                return (
                  <div key={status}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                      <Tag color={LOAN_STATUS_META[status].color}>{LOAN_STATUS_META[status].label}</Tag>
                      <Text type="secondary">{count} хүсэлт</Text>
                    </div>
                    <Progress percent={percent} size="small" strokeColor={LOAN_STATUS_META[status].stroke} />
                  </div>
                );
              })}
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default ReportsPage;
