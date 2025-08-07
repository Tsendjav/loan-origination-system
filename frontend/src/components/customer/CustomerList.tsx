// CustomerList.tsx - ИСПРАВЛЕННАЯ ВЕРСИЯ
import React, { useState, useEffect, useCallback } from 'react';
import {
  Table,
  Space,
  Button,
  Input,
  Select,
  DatePicker,
  Card,
  Tag,
  Avatar,
  Tooltip,
  Popconfirm,
  Drawer,
  Row,
  Col,
  Statistic,
  Badge,
  Typography,
  message,
} from 'antd';
import {
  SearchOutlined,
  UserAddOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  FilterOutlined,
  ExportOutlined,
  ReloadOutlined,
  PhoneOutlined,
  MailOutlined,
  BankOutlined,
  UserOutlined
} from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { FilterValue, SorterResult } from 'antd/es/table/interface';
import dayjs from 'dayjs';

// ⭐ ИСПРАВЛЕНО: Импортируем все типы из централизованного файла
import {
  Customer,
  CustomerType,
  KycStatus,
  RiskLevel, // Changed from RiskRating to RiskLevel
} from '../../types';

// Mock service import
import { customerService } from '../../services/customerService';

const { Option } = Select;
const { RangePicker } = DatePicker;
const { Text, Title } = Typography;

export interface CustomerSearchFilters {
  search?: string;
  customerType?: CustomerType;
  kycStatus?: KycStatus;
  province?: string;
  city?: string;
  riskRating?: RiskLevel; // Changed from RiskRating to RiskLevel
  assignedTo?: string;
  isActive?: boolean;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
  sort?: string;
  direction?: 'ASC' | 'DESC';
}

export interface CustomerListProps {
  onCustomerSelect?: (customer: Customer) => void;
  onCustomerEdit?: (customer: Customer) => void;
  onCustomerDelete?: (id: string) => void;
  filters?: CustomerSearchFilters;
  showActions?: boolean;
  selectable?: boolean;
  pageSize?: number;
}

// ⭐ ИСПРАВЛЕНО: Обновленные опции с правильными значениями RiskLevel
export const CUSTOMER_TYPE_OPTIONS = [
  { label: 'Хувь хүн', value: CustomerType.INDIVIDUAL },
  { label: 'Байгууллага', value: CustomerType.BUSINESS },
];

export const KYC_STATUS_OPTIONS = [
  { label: 'Хүлээгдэж буй', value: KycStatus.PENDING },
  { label: 'Явагдаж буй', value: KycStatus.IN_PROGRESS },
  { label: 'Зөвшөөрөгдсөн', value: KycStatus.APPROVED },
  { label: 'Татгалзсан', value: KycStatus.REJECTED },
  { label: 'Хугацаа дууссан', value: KycStatus.EXPIRED },
];

export const RISK_RATING_OPTIONS = [
  { label: 'Бага', value: 'LOW' as RiskLevel },
  { label: 'Дунд', value: 'MEDIUM' as RiskLevel },
  { label: 'Өндөр', value: 'HIGH' as RiskLevel },
];

export const PROVINCE_OPTIONS = [
  'Улаанбаатар', 'Архангай', 'Баян-Өлгий', 'Баянхонгор', 'Булган', 'Говь-Алтай', 'Говьсүмбэр',
  'Дархан-Уул', 'Дорноговь', 'Дорнод', 'Дундговь', 'Завхан', 'Орхон', 'Өвөрхангай', 'Өмнөговь',
  'Сүхбаатар', 'Сэлэнгэ', 'Төв', 'Увс', 'Ховд', 'Хөвсгөл', 'Хэнтий',
];

interface TableParams {
  pagination?: TablePaginationConfig;
  sortField?: string;
  sortOrder?: string;
  filters?: Record<string, FilterValue | null>;
}

const CustomerList: React.FC<CustomerListProps> = ({
  onCustomerSelect,
  onCustomerEdit,
  onCustomerDelete,
  filters: initialFilters,
  showActions = true,
  selectable = false,
  pageSize = 20
}) => {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [filterDrawerVisible, setFilterDrawerVisible] = useState(false);
  const [searchFilters, setSearchFilters] = useState<CustomerSearchFilters>(initialFilters || {});
  const [tableParams, setTableParams] = useState<TableParams>({
    pagination: {
      current: 1,
      pageSize: pageSize,
      showSizeChanger: true,
      showQuickJumper: true,
      showTotal: (total, range) => `${range[0]}-${range[1]} из ${total} харилцагчид`
    }
  });

  // Load customers data
  const fetchCustomers = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        page: (tableParams.pagination?.current || 1) - 1,
        size: tableParams.pagination?.pageSize || pageSize,
        sort: tableParams.sortField ? `${tableParams.sortField},${tableParams.sortOrder === 'ascend' ? 'asc' : 'desc'}` : undefined,
        ...searchFilters
      };

      const response = await customerService.getCustomers(params);
      setCustomers(response.content);
      setTableParams(prev => ({
        ...prev,
        pagination: {
          ...prev.pagination,
          total: response.totalElements,
          current: response.page + 1,
          pageSize: response.size
        }
      }));
    } catch (error) {
      message.error('Харилцагчдын мэдээлэл ачаалахад алдаа гарлаа');
      console.error('Error fetching customers:', error);
    } finally {
      setLoading(false);
    }
  }, [searchFilters, tableParams.pagination?.current, tableParams.pagination?.pageSize, tableParams.sortField, tableParams.sortOrder, pageSize]);

  useEffect(() => {
    fetchCustomers();
  }, [fetchCustomers]);

  // Handle table change (pagination, sorting, filtering)
  const handleTableChange = (
    pagination: TablePaginationConfig,
    filters: Record<string, FilterValue | null>,
    sorter: SorterResult<Customer> | SorterResult<Customer>[]
  ) => {
    setTableParams({
      pagination,
      filters,
      sortField: Array.isArray(sorter) ? undefined : sorter.field as string,
      sortOrder: Array.isArray(sorter) ? undefined : (sorter.order || undefined)
    });
  };

  // Handle search
  const handleSearch = (filters: CustomerSearchFilters) => {
    setSearchFilters(filters);
    setTableParams(prev => ({
      ...prev,
      pagination: { ...prev.pagination, current: 1 }
    }));
  };

  // Handle row selection
  const onSelectChange = (newSelectedRowKeys: React.Key[]) => {
    setSelectedRowKeys(newSelectedRowKeys);
  };

  const rowSelection = selectable ? {
    selectedRowKeys,
    onChange: onSelectChange,
    getCheckboxProps: (record: Customer) => ({
      disabled: !record.isActive,
      name: record.registerNumber
    })
  } : undefined;

  const renderCustomerType = (type: CustomerType) => {
    const color = type === CustomerType.INDIVIDUAL ? 'blue' : 'green';
    const text = type === CustomerType.INDIVIDUAL ? 'Хувь хүн' : 'Байгууллага';
    return <Tag color={color}>{text}</Tag>;
  };

  const renderKycStatus = (status: KycStatus) => {
    const colorMap: Record<string, string> = {
      [KycStatus.PENDING]: 'orange',
      [KycStatus.IN_PROGRESS]: 'blue',
      [KycStatus.APPROVED]: 'green',
      [KycStatus.REJECTED]: 'red',
      [KycStatus.EXPIRED]: 'red'
    };
    
    const textMap: Record<string, string> = {
      [KycStatus.PENDING]: 'Хүлээгдэж буй',
      [KycStatus.IN_PROGRESS]: 'Явагдаж буй',
      [KycStatus.APPROVED]: 'Зөвшөөрөгдсөн',
      [KycStatus.REJECTED]: 'Татгалзсан',
      [KycStatus.EXPIRED]: 'Хугацаа дууссан'
    };
    
    return <Tag color={colorMap[status]}>{textMap[status]}</Tag>;
  };

  // ⭐ ИСПРАВЛЕНО: Обновлено для работы с RiskLevel
  const renderRiskRating = (rating: RiskLevel) => {
    const colorMap: Record<string, string> = {
      'LOW': 'green',
      'MEDIUM': 'orange',
      'HIGH': 'red'
    };
    
    const textMap: Record<string, string> = {
      'LOW': 'Бага',
      'MEDIUM': 'Дунд',
      'HIGH': 'Өндөр'
    };
    
    return <Tag color={colorMap[rating]}>{textMap[rating]}</Tag>;
  };

  const renderCustomerName = (record: Customer) => {
    const name = record.customerType === CustomerType.INDIVIDUAL
      ? `${record.firstName || ''} ${record.lastName || ''}`.trim()
      : record.companyName || '';
    
    const avatar = record.customerType === CustomerType.INDIVIDUAL
      ? <Avatar icon={<UserOutlined />} />
      : <Avatar icon={<BankOutlined />} />;

    return (
      <Space>
        {avatar}
        <div>
          <div style={{ fontWeight: 500 }}>{name}</div>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {record.registerNumber}
          </Text>
        </div>
      </Space>
    );
  };

  // Table columns
  const columns: ColumnsType<Customer> = [
    {
      title: 'Харилцагч',
      key: 'customer',
      width: 250,
      render: (_, record) => renderCustomerName(record),
      sorter: true
    },
    {
      title: 'Төрөл',
      dataIndex: 'customerType',
      key: 'customerType',
      width: 100,
      render: renderCustomerType,
      filters: CUSTOMER_TYPE_OPTIONS.map(opt => ({ text: opt.label, value: opt.value }))
    },
    {
      title: 'Холбоо барих',
      key: 'contact',
      width: 200,
      render: (_, record) => (
        <div>
          <div>
            <PhoneOutlined style={{ marginRight: 4 }} />
            {record.phone}
          </div>
          {record.email && (
            <div style={{ marginTop: 2 }}>
              <MailOutlined style={{ marginRight: 4 }} />
              <Text type="secondary" style={{ fontSize: 12 }}>
                {record.email}
              </Text>
            </div>
          )}
        </div>
      )
    },
    {
      title: 'Байршил',
      key: 'location',
      width: 150,
      render: (_, record) => (
        <div>
          <div>{record.province || record.address?.state || 'Тодорхойгүй'}</div>
          {(record.city || record.address?.city) && (
            <Text type="secondary" style={{ fontSize: 12 }}>
              {record.city || record.address?.city}
            </Text>
          )}
        </div>
      ),
      filters: PROVINCE_OPTIONS.map(province => ({ text: province, value: province }))
    },
    {
      title: 'KYC Статус',
      dataIndex: 'kycStatus',
      key: 'kycStatus',
      width: 120,
      render: renderKycStatus,
      filters: KYC_STATUS_OPTIONS.map(opt => ({ text: opt.label, value: opt.value }))
    },
    {
      title: 'Эрсдэлийн үнэлгээ',
      dataIndex: 'riskLevel',
      key: 'riskLevel',
      width: 120,
      render: renderRiskRating,
      filters: RISK_RATING_OPTIONS.map(opt => ({ text: opt.label, value: opt.value }))
    },
    {
      title: 'Үүсгэсэн огноо',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 120,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD'),
      sorter: true
    },
    {
      title: 'Статус',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 80,
      render: (isActive: boolean) => (
        <Badge
          status={isActive ? 'success' : 'default'}
          text={isActive ? 'Идэвхтэй' : 'Идэвхгүй'}
        />
      ),
      filters: [
        { text: 'Идэвхтэй', value: true },
        { text: 'Идэвхгүй', value: false }
      ]
    }
  ];

  // Add actions column if showActions is true
  if (showActions) {
    columns.push({
      title: 'Үйлдэл',
      key: 'actions',
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Tooltip title="Харах">
            <Button
              type="link"
              icon={<EyeOutlined />}
              onClick={() => onCustomerSelect?.(record)}
            />
          </Tooltip>
          <Tooltip title="Засах">
            <Button
              type="link"
              icon={<EditOutlined />}
              onClick={() => onCustomerEdit?.(record)}
            />
          </Tooltip>
          <Popconfirm
            title="Та энэ харилцагчийг устгахдаа итгэлтэй байна уу?"
            onConfirm={() => onCustomerDelete?.(record.id)}
            okText="Тийм"
            cancelText="Үгүй"
          >
            <Tooltip title="Устгах">
              <Button
                type="link"
                danger
                icon={<DeleteOutlined />}
              />
            </Tooltip>
          </Popconfirm>
        </Space>
      )
    });
  }

  // Filter form component
  const FilterForm = () => (
    <Card title="Хайлтын шүүлтүүр" size="small">
      <Row gutter={[16, 16]}>
        <Col span={12}>
          <label>Хайх утга</label>
          <Input
            placeholder="Нэр, регистр, утас, и-мэйл..."
            value={searchFilters.search}
            onChange={(e) => setSearchFilters(prev => ({ ...prev, search: e.target.value }))}
            allowClear
          />
        </Col>
        <Col span={12}>
          <label>Харилцагчийн төрөл</label>
          <Select
            placeholder="Төрөл сонгох"
            value={searchFilters.customerType}
            onChange={(value) => setSearchFilters(prev => ({ ...prev, customerType: value }))}
            allowClear
            style={{ width: '100%' }}
          >
            {CUSTOMER_TYPE_OPTIONS.map(option => (
              <Option key={option.value} value={option.value}>
                {option.label}
              </Option>
            ))}
          </Select>
        </Col>
        <Col span={12}>
          <label>KYC Статус</label>
          <Select
            placeholder="KYC статус сонгох"
            value={searchFilters.kycStatus}
            onChange={(value) => setSearchFilters(prev => ({ ...prev, kycStatus: value }))}
            allowClear
            style={{ width: '100%' }}
          >
            {KYC_STATUS_OPTIONS.map(option => (
              <Option key={option.value} value={option.value}>
                {option.label}
              </Option>
            ))}
          </Select>
        </Col>
        <Col span={12}>
          <label>Аймаг/Хот</label>
          <Select
            placeholder="Байршил сонгох"
            value={searchFilters.province}
            onChange={(value) => setSearchFilters(prev => ({ ...prev, province: value }))}
            allowClear
            style={{ width: '100%' }}
          >
            {PROVINCE_OPTIONS.map(province => (
              <Option key={province} value={province}>
                {province}
              </Option>
            ))}
          </Select>
        </Col>
        <Col span={12}>
          <label>Эрсдэлийн үнэлгээ</label>
          <Select
            placeholder="Эрсдэл сонгох"
            value={searchFilters.riskRating}
            onChange={(value) => setSearchFilters(prev => ({ ...prev, riskRating: value }))}
            allowClear
            style={{ width: '100%' }}
          >
            {RISK_RATING_OPTIONS.map(option => (
              <Option key={option.value} value={option.value}>
                {option.label}
              </Option>
            ))}
          </Select>
        </Col>
        <Col span={12}>
          <label>Идэвх статус</label>
          <Select
            placeholder="Статус сонгох"
            value={searchFilters.isActive}
            onChange={(value) => setSearchFilters(prev => ({ ...prev, isActive: value }))}
            allowClear
            style={{ width: '100%' }}
          >
            <Option value={true}>Идэвхтэй</Option>
            <Option value={false}>Идэвхгүй</Option>
          </Select>
        </Col>
        <Col span={24}>
          <label>Үүсгэсэн огноо</label>
          <RangePicker
            style={{ width: '100%' }}
            onChange={(dates) => {
              if (dates && dates[0] && dates[1]) {
                setSearchFilters(prev => ({
                  ...prev,
                  dateFrom: dates[0]!.format('YYYY-MM-DD'),
                  dateTo: dates[1]!.format('YYYY-MM-DD')
                }));
              } else {
                setSearchFilters(prev => ({
                  ...prev,
                  dateFrom: undefined,
                  dateTo: undefined
                }));
              }
            }}
          />
        </Col>
        <Col span={24}>
          <Space>
            <Button
              type="primary"
              icon={<SearchOutlined />}
              onClick={() => {
                handleSearch(searchFilters);
                setFilterDrawerVisible(false);
              }}
            >
              Хайх
            </Button>
            <Button
              onClick={() => {
                setSearchFilters({});
                handleSearch({});
                setFilterDrawerVisible(false);
              }}
            >
              Цэвэрлэх
            </Button>
          </Space>
        </Col>
      </Row>
    </Card>
  );

  return (
    <div className="customer-list">
      {/* Header with actions */}
      <Card>
        <Row justify="space-between" align="middle" style={{ marginBottom: 16 }}>
          <Col>
            <Title level={4} style={{ margin: 0 }}>
              Харилцагчдын жагсаалт
            </Title>
          </Col>
          <Col>
            <Space>
              <Button
                icon={<FilterOutlined />}
                onClick={() => setFilterDrawerVisible(true)}
              >
                Шүүлтүүр
              </Button>
              <Button
                icon={<ReloadOutlined />}
                onClick={fetchCustomers}
                loading={loading}
              >
                Сэргээх
              </Button>
              <Button
                icon={<ExportOutlined />}
                onClick={() => {
                  message.info('Экспорт функц удахгүй нэмэгдэнэ');
                }}
              >
                Экспорт
              </Button>
              <Button
                type="primary"
                icon={<UserAddOutlined />}
                onClick={() => {
                  message.info('Шинэ харилцагч нэмэх функц удахгүй нэмэгдэнэ');
                }}
              >
                Шинэ харилцагч
              </Button>
            </Space>
          </Col>
        </Row>

        {/* Quick stats */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Statistic
              title="Нийт харилцагч"
              value={tableParams.pagination?.total || 0}
              prefix={<UserOutlined />}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Идэвхтэй"
              value={customers.filter(c => c.isActive).length}
              valueStyle={{ color: '#3f8600' }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="KYC зөвшөөрөгдсөн"
              value={customers.filter(c => c.kycStatus === KycStatus.APPROVED).length}
              valueStyle={{ color: '#1890ff' }}
            />
          </Col>
          <Col span={6}>
            <Statistic
              title="Сонгогдсон"
              value={selectedRowKeys.length}
              valueStyle={{ color: '#722ed1' }}
            />
          </Col>
        </Row>

        {/* Table */}
        <Table
          rowSelection={rowSelection}
          columns={columns}
          dataSource={customers}
          rowKey="id"
          pagination={tableParams.pagination}
          loading={loading}
          onChange={handleTableChange}
          scroll={{ x: 1200 }}
          size="middle"
          onRow={(record) => ({
            onDoubleClick: () => onCustomerSelect?.(record),
            style: { cursor: 'pointer' }
          })}
        />
      </Card>

      {/* Filter Drawer */}
      <Drawer
        title="Хайлтын шүүлтүүр"
        placement="right"
        width={400}
        onClose={() => setFilterDrawerVisible(false)}
        open={filterDrawerVisible}
      >
        <FilterForm />
      </Drawer>
    </div>
  );
};

export default CustomerList;