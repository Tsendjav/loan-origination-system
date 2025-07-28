import React, { useState, useEffect } from 'react';
import { LoanApplication, LoanProduct, LoanType, LoanPurpose, DocumentType, CollateralType, loanService, loanUtils } from '../../services/loanService';
import { Customer, customerService } from '../../services/customerService';
import { showToast } from '../layout/MainLayout';

interface LoanApplicationFormProps {
  application?: LoanApplication;
  customerId?: number;
  onSubmit: (application: Partial<LoanApplication>) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  mode: 'create' | 'edit' | 'view';
}

const LoanApplicationForm: React.FC<LoanApplicationFormProps> = ({
  application,
  customerId,
  onSubmit,
  onCancel,
  isLoading = false,
  mode = 'create',
}) => {
  const [formData, setFormData] = useState<Partial<LoanApplication>>({
    customerId: customerId,
    requestedAmount: 0,
    loanTerm: 12,
    purpose: LoanPurpose.OTHER,
    documents: [],
    collateral: [],
    notes: '',
  });

  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loanProducts, setLoanProducts] = useState<LoanProduct[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<LoanProduct | null>(null);
  const [calculationResult, setCalculationResult] = useState<any>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [currentStep, setCurrentStep] = useState(1);
  const [documentFiles, setDocumentFiles] = useState<Record<string, File>>({});

  const isReadOnly = mode === 'view';
  const totalSteps = 4;

  // Initialize form data
  useEffect(() => {
    if (application) {
      setFormData(application);
      if (application.loanProduct) {
        setSelectedProduct(application.loanProduct);
      }
    }
  }, [application]);

  // Load customers and loan products
  useEffect(() => {
    const loadData = async () => {
      try {
        const [customersData, productsData] = await Promise.all([
          customerService.getCustomers({ size: 1000 }),
          loanService.getActiveLoanProducts(),
        ]);
        
        setCustomers(customersData.content);
        setLoanProducts(productsData);
      } catch (error) {
        console.error('Error loading data:', error);
        showToast({
          message: 'Өгөгдөл ачаалахад алдаа гарлаа',
          type: 'error',
        });
      }
    };

    loadData();
  }, []);

  // Calculate loan payment when amount or product changes
  useEffect(() => {
    if (formData.requestedAmount && formData.loanTerm && selectedProduct) {
      calculatePayment();
    }
  }, [formData.requestedAmount, formData.loanTerm, selectedProduct]);

  const calculatePayment = async () => {
    if (!formData.requestedAmount || !formData.loanTerm || !selectedProduct) return;

    try {
      const result = await loanService.calculateLoanPayment({
        loanAmount: formData.requestedAmount,
        interestRate: selectedProduct.baseInterestRate,
        loanTerm: formData.loanTerm,
        paymentFrequency: 'MONTHLY',
      });
      setCalculationResult(result);
    } catch (error) {
      console.error('Calculation error:', error);
    }
  };

  // Validate current step
  const validateStep = (step: number): boolean => {
    const newErrors: Record<string, string> = {};

    switch (step) {
      case 1: // Basic Information
        if (!formData.customerId) newErrors.customerId = 'Харилцагч сонгоно уу';
        if (!formData.loanProductId) newErrors.loanProductId = 'Зээлийн бүтээгдэхүүн сонгоно уу';
        if (!formData.requestedAmount || formData.requestedAmount <= 0) {
          newErrors.requestedAmount = 'Зээлийн дүн оруулна уу';
        } else if (selectedProduct) {
          if (formData.requestedAmount < selectedProduct.minAmount) {
            newErrors.requestedAmount = `Хамгийн бага дүн: ${selectedProduct.minAmount.toLocaleString()} ₮`;
          }
          if (formData.requestedAmount > selectedProduct.maxAmount) {
            newErrors.requestedAmount = `Хамгийн их дүн: ${selectedProduct.maxAmount.toLocaleString()} ₮`;
          }
        }
        if (!formData.loanTerm || formData.loanTerm <= 0) {
          newErrors.loanTerm = 'Зээлийн хугацаа оруулна уу';
        } else if (selectedProduct) {
          if (formData.loanTerm < selectedProduct.minTerm) {
            newErrors.loanTerm = `Хамгийн бага хугацаа: ${selectedProduct.minTerm} сар`;
          }
          if (formData.loanTerm > selectedProduct.maxTerm) {
            newErrors.loanTerm = `Хамгийн их хугацаа: ${selectedProduct.maxTerm} сар`;
          }
        }
        if (!formData.purpose) newErrors.purpose = 'Зээлийн зорилго сонгоно уу';
        break;

      case 2: // Documents
        if (selectedProduct?.requiredDocuments?.length > 0) {
          const missingDocs = selectedProduct.requiredDocuments.filter(
            docType => !documentFiles[docType] && !formData.documents?.some(d => d.documentType.toString() === docType)
          );
          if (missingDocs.length > 0) {
            newErrors.documents = `Дараах баримтууд шаардлагатай: ${missingDocs.join(', ')}`;
          }
        }
        break;

      case 3: // Collateral (if required)
        if (selectedProduct && formData.requestedAmount && formData.requestedAmount > 5000000) {
          if (!formData.collateral || formData.collateral.length === 0) {
            newErrors.collateral = 'Барьцаа шаардлагатай';
          }
        }
        break;

      case 4: // Review - no validation needed
        break;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle input changes
  const handleChange = (field: string, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));

    // Clear error for this field
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }));
    }

    // Handle product selection
    if (field === 'loanProductId') {
      const product = loanProducts.find(p => p.id === value);
      setSelectedProduct(product || null);
    }
  };

  // Handle file upload
  const handleFileUpload = (documentType: string, file: File) => {
    setDocumentFiles(prev => ({ ...prev, [documentType]: file }));
  };

  // Add collateral
  const addCollateral = () => {
    const newCollateral = {
      type: CollateralType.OTHER,
      description: '',
      estimatedValue: 0,
      condition: 'GOOD',
      documents: [],
    };
    setFormData(prev => ({
      ...prev,
      collateral: [...(prev.collateral || []), newCollateral],
    }));
  };

  // Remove collateral
  const removeCollateral = (index: number) => {
    setFormData(prev => ({
      ...prev,
      collateral: prev.collateral?.filter((_, i) => i !== index) || [],
    }));
  };

  // Update collateral
  const updateCollateral = (index: number, field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      collateral: prev.collateral?.map((item, i) => 
        i === index ? { ...item, [field]: value } : item
      ) || [],
    }));
  };

  // Handle step navigation
  const handleNext = () => {
    if (validateStep(currentStep)) {
      setCurrentStep(prev => Math.min(prev + 1, totalSteps));
    }
  };

  const handlePrevious = () => {
    setCurrentStep(prev => Math.max(prev - 1, 1));
  };

  // Handle form submission
  const handleSubmit = async () => {
    if (!validateStep(currentStep)) return;

    try {
      // Upload documents if any
      const uploadedDocuments = [];
      for (const [docType, file] of Object.entries(documentFiles)) {
        if (file) {
          try {
            const uploadedDoc = await loanService.uploadLoanDocument(
              formData.id || 0, // Will be set by backend for new applications
              file,
              docType as DocumentType
            );
            uploadedDocuments.push(uploadedDoc);
          } catch (error) {
            console.error('Document upload error:', error);
          }
        }
      }

      const applicationData = {
        ...formData,
        documents: [...(formData.documents || []), ...uploadedDocuments],
      };

      await onSubmit(applicationData);
      showToast({
        message: mode === 'create' ? 'Зээлийн хүсэлт амжилттай илгээгдлээ' : 'Зээлийн хүсэлт шинэчлэгдлээ',
        type: 'success',
      });
    } catch (error: any) {
      showToast({
        message: error.message || 'Алдаа гарлаа',
        type: 'error',
      });
    }
  };

  // Step indicator
  const StepIndicator = () => (
    <div className="mb-8">
      <div className="flex items-center justify-between">
        {Array.from({ length: totalSteps }, (_, i) => i + 1).map((step) => (
          <div key={step} className="flex items-center">
            <div
              className={`
                flex items-center justify-center w-8 h-8 rounded-full text-sm font-medium
                ${step <= currentStep
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-300 text-gray-600'
                }
              `}
            >
              {step}
            </div>
            {step < totalSteps && (
              <div
                className={`
                  w-full h-1 mx-2
                  ${step < currentStep ? 'bg-blue-600' : 'bg-gray-300'}
                `}
              />
            )}
          </div>
        ))}
      </div>
      <div className="flex justify-between mt-2 text-sm text-gray-600">
        <span>Үндсэн мэдээлэл</span>
        <span>Баримт бичиг</span>
        <span>Барьцаа</span>
        <span>Баталгаажуулалт</span>
      </div>
    </div>
  );

  // Render basic information step
  const renderBasicInfo = () => (
    <div className="space-y-6">
      <h3 className="text-lg font-medium text-gray-900">Үндсэн мэдээлэл</h3>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Customer Selection */}
        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Харилцагч *
          </label>
          <select
            value={formData.customerId || ''}
            onChange={(e) => handleChange('customerId', Number(e.target.value))}
            disabled={isReadOnly || !!customerId}
            className={`
              w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500
              ${errors.customerId ? 'border-red-300' : ''}
              ${isReadOnly ? 'bg-gray-50' : ''}
            `}
          >
            <option value="">Харилцагч сонгоно уу</option>
            {customers.map(customer => (
              <option key={customer.id} value={customer.id}>
                {customer.firstName} {customer.lastName} - {customer.email}
              </option>
            ))}
          </select>
          {errors.customerId && <p className="mt-1 text-sm text-red-600">{errors.customerId}</p>}
        </div>

        {/* Loan Product */}
        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Зээлийн бүтээгдэхүүн *
          </label>
          <select
            value={formData.loanProductId || ''}
            onChange={(e) => handleChange('loanProductId', Number(e.target.value))}
            disabled={isReadOnly}
            className={`
              w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500
              ${errors.loanProductId ? 'border-red-300' : ''}
              ${isReadOnly ? 'bg-gray-50' : ''}
            `}
          >
            <option value="">Зээлийн бүтээгдэхүүн сонгоно уу</option>
            {loanProducts.map(product => (
              <option key={product.id} value={product.id}>
                {product.name} - {loanUtils.formatInterestRate(product.baseInterestRate)}
              </option>
            ))}
          </select>
          {errors.loanProductId && <p className="mt-1 text-sm text-red-600">{errors.loanProductId}</p>}
          
          {selectedProduct && (
            <div className="mt-2 p-3 bg-blue-50 rounded-md text-sm text-blue-800">
              <p><strong>Тайлбар:</strong> {selectedProduct.description}</p>
              <p><strong>Дүн:</strong> {selectedProduct.minAmount.toLocaleString()} - {selectedProduct.maxAmount.toLocaleString()} ₮</p>
              <p><strong>Хугацаа:</strong> {selectedProduct.minTerm} - {selectedProduct.maxTerm} сар</p>
              <p><strong>Хүү:</strong> {loanUtils.formatInterestRate(selectedProduct.baseInterestRate)}</p>
            </div>
          )}
        </div>

        {/* Requested Amount */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Хүссэн дүн (₮) *
          </label>
          <input
            type="number"
            value={formData.requestedAmount || ''}
            onChange={(e) => handleChange('requestedAmount', Number(e.target.value))}
            disabled={isReadOnly}
            className={`
              w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500
              ${errors.requestedAmount ? 'border-red-300' : ''}
              ${isReadOnly ? 'bg-gray-50' : ''}
            `}
            placeholder="10000000"
            min={selectedProduct?.minAmount || 0}
            max={selectedProduct?.maxAmount || Infinity}
          />
          {errors.requestedAmount && <p className="mt-1 text-sm text-red-600">{errors.requestedAmount}</p>}
        </div>

        {/* Loan Term */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Зээлийн хугацаа (сар) *
          </label>
          <input
            type="number"
            value={formData.loanTerm || ''}
            onChange={(e) => handleChange('loanTerm', Number(e.target.value))}
            disabled={isReadOnly}
            className={`
              w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500
              ${errors.loanTerm ? 'border-red-300' : ''}
              ${isReadOnly ? 'bg-gray-50' : ''}
            `}
            placeholder="12"
            min={selectedProduct?.minTerm || 1}
            max={selectedProduct?.maxTerm || 360}
          />
          {errors.loanTerm && <p className="mt-1 text-sm text-red-600">{errors.loanTerm}</p>}
        </div>

        {/* Purpose */}
        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Зээлийн зорилго *
          </label>
          <select
            value={formData.purpose || ''}
            onChange={(e) => handleChange('purpose', e.target.value)}
            disabled={isReadOnly}
            className={`
              w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500
              ${errors.purpose ? 'border-red-300' : ''}
              ${isReadOnly ? 'bg-gray-50' : ''}
            `}
          >
            <option value="">Зорилго сонгоно уу</option>
            <option value={LoanPurpose.HOME_PURCHASE}>Орон сууц худалдан авах</option>
            <option value={LoanPurpose.HOME_IMPROVEMENT}>Орон сууц засварлах</option>
            <option value={LoanPurpose.DEBT_CONSOLIDATION}>Өрийг нэгтгэх</option>
            <option value={LoanPurpose.EDUCATION}>Боловсрол</option>
            <option value={LoanPurpose.MEDICAL}>Эрүүл мэнд</option>
            <option value={LoanPurpose.BUSINESS_EXPANSION}>Бизнес өргөжүүлэх</option>
            <option value={LoanPurpose.VEHICLE_PURCHASE}>Тээврийн хэрэгсэл худалдан авах</option>
            <option value={LoanPurpose.OTHER}>Бусад</option>
          </select>
          {errors.purpose && <p className="mt-1 text-sm text-red-600">{errors.purpose}</p>}
        </div>

        {/* Notes */}
        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Нэмэлт мэдээлэл
          </label>
          <textarea
            rows={3}
            value={formData.notes || ''}
            onChange={(e) => handleChange('notes', e.target.value)}
            disabled={isReadOnly}
            className={`
              w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500
              ${isReadOnly ? 'bg-gray-50' : ''}
            `}
            placeholder="Нэмэлт тайлбар..."
          />
        </div>
      </div>

      {/* Payment Calculation */}
      {calculationResult && (
        <div className="mt-6 p-4 bg-green-50 border border-green-200 rounded-md">
          <h4 className="font-medium text-green-800 mb-2">Төлбөрийн тооцоо</h4>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
            <div>
              <span className="text-green-700">Сарын төлбөр:</span>
              <p className="font-semibold text-green-900">
                {loanUtils.formatAmount(calculationResult.monthlyPayment)}
              </p>
            </div>
            <div>
              <span className="text-green-700">Нийт хүү:</span>
              <p className="font-semibold text-green-900">
                {loanUtils.formatAmount(calculationResult.totalInterest)}
              </p>
            </div>
            <div>
              <span className="text-green-700">Нийт төлбөр:</span>
              <p className="font-semibold text-green-900">
                {loanUtils.formatAmount(calculationResult.totalAmount)}
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  // Render documents step
  const renderDocuments = () => (
    <div className="space-y-6">
      <h3 className="text-lg font-medium text-gray-900">Баримт бичиг</h3>
      
      {selectedProduct?.requiredDocuments && selectedProduct.requiredDocuments.length > 0 ? (
        <div className="space-y-4">
          <p className="text-sm text-gray-600">
            Дараах баримт бичгүүд шаардлагатай:
          </p>
          
          {selectedProduct.requiredDocuments.map((docType) => (
            <div key={docType} className="border border-gray-200 rounded-lg p-4">
              <div className="flex items-center justify-between mb-2">
                <h4 className="font-medium text-gray-900">{getDocumentTypeName(docType)}</h4>
                <span className="text-xs text-red-600">Заавал</span>
              </div>
              
              {!isReadOnly && (
                <div className="mt-2">
                  <input
                    type="file"
                    accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file) {
                        handleFileUpload(docType, file);
                      }
                    }}
                    className="w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                  />
                </div>
              )}
              
              {documentFiles[docType] && (
                <div className="mt-2 text-sm text-green-600">
                  ✓ {documentFiles[docType].name} ({(documentFiles[docType].size / 1024 / 1024).toFixed(2)} MB)
                </div>
              )}
              
              {formData.documents?.some(d => d.documentType.toString() === docType) && (
                <div className="mt-2 text-sm text-blue-600">
                  ✓ Баримт байршуулсан
                </div>
              )}
            </div>
          ))}
          
          {errors.documents && (
            <p className="text-sm text-red-600">{errors.documents}</p>
          )}
        </div>
      ) : (
        <p className="text-gray-500">Энэ зээлийн бүтээгдэхүүнд тусгай баримт шаардагдахгүй.</p>
      )}
    </div>
  );

  // Render collateral step
  const renderCollateral = () => (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-medium text-gray-900">Барьцаа</h3>
        {!isReadOnly && (
          <button
            type="button"
            onClick={addCollateral}
            className="px-3 py-1 text-sm bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Барьцаа нэмэх
          </button>
        )}
      </div>
      
      {formData.collateral && formData.collateral.length > 0 ? (
        <div className="space-y-4">
          {formData.collateral.map((collateral, index) => (
            <div key={index} className="border border-gray-200 rounded-lg p-4">
              <div className="flex items-center justify-between mb-4">
                <h4 className="font-medium text-gray-900">Барьцаа #{index + 1}</h4>
                {!isReadOnly && (
                  <button
                    type="button"
                    onClick={() => removeCollateral(index)}
                    className="text-red-600 hover:text-red-800"
                  >
                    Устгах
                  </button>
                )}
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Төрөл
                  </label>
                  <select
                    value={collateral.type}
                    onChange={(e) => updateCollateral(index, 'type', e.target.value)}
                    disabled={isReadOnly}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value={CollateralType.REAL_ESTATE}>Үл хөдлөх хөрөнгө</option>
                    <option value={CollateralType.VEHICLE}>Тээврийн хэрэгсэл</option>
                    <option value={CollateralType.JEWELRY}>Үнэт эдлэл</option>
                    <option value={CollateralType.SECURITIES}>Үнэт цаас</option>
                    <option value={CollateralType.EQUIPMENT}>Тоног төхөөрөмж</option>
                    <option value={CollateralType.OTHER}>Бусад</option>
                  </select>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Үнэлгээ (₮)
                  </label>
                  <input
                    type="number"
                    value={collateral.estimatedValue}
                    onChange={(e) => updateCollateral(index, 'estimatedValue', Number(e.target.value))}
                    disabled={isReadOnly}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="0"
                  />
                </div>
                
                <div className="md:col-span-2">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Тайлбар
                  </label>
                  <textarea
                    rows={2}
                    value={collateral.description}
                    onChange={(e) => updateCollateral(index, 'description', e.target.value)}
                    disabled={isReadOnly}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Барьцааны дэлгэрэнгүй тайлбар..."
                  />
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-8 text-gray-500">
          <p>Барьцаа байхгүй</p>
          {formData.requestedAmount && formData.requestedAmount > 5000000 && (
            <p className="text-sm text-red-600 mt-2">
              5,000,000₮-аас дээш зээлд барьцаа шаардлагатай
            </p>
          )}
        </div>
      )}
      
      {errors.collateral && (
        <p className="text-sm text-red-600">{errors.collateral}</p>
      )}
    </div>
  );

  // Render review step
  const renderReview = () => (
    <div className="space-y-6">
      <h3 className="text-lg font-medium text-gray-900">Хүсэлтийг баталгаажуулах</h3>
      
      <div className="bg-gray-50 p-6 rounded-lg space-y-6">
        {/* Basic Information */}
        <div>
          <h4 className="font-medium text-gray-900 mb-2">Үндсэн мэдээлэл</h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-gray-600">Харилцагч:</span>
              <p className="font-medium">
                {customers.find(c => c.id === formData.customerId)?.firstName} {customers.find(c => c.id === formData.customerId)?.lastName}
              </p>
            </div>
            <div>
              <span className="text-gray-600">Зээлийн бүтээгдэхүүн:</span>
              <p className="font-medium">{selectedProduct?.name}</p>
            </div>
            <div>
              <span className="text-gray-600">Хүссэн дүн:</span>
              <p className="font-medium">{loanUtils.formatAmount(formData.requestedAmount || 0)}</p>
            </div>
            <div>
              <span className="text-gray-600">Хугацаа:</span>
              <p className="font-medium">{formData.loanTerm} сар</p>
            </div>
            <div>
              <span className="text-gray-600">Зорилго:</span>
              <p className="font-medium">{getLoanPurposeName(formData.purpose)}</p>
            </div>
          </div>
        </div>

        {/* Payment Information */}
        {calculationResult && (
          <div>
            <h4 className="font-medium text-gray-900 mb-2">Төлбөрийн мэдээлэл</h4>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
              <div>
                <span className="text-gray-600">Сарын төлбөр:</span>
                <p className="font-medium text-green-600">{loanUtils.formatAmount(calculationResult.monthlyPayment)}</p>
              </div>
              <div>
                <span className="text-gray-600">Нийт хүү:</span>
                <p className="font-medium">{loanUtils.formatAmount(calculationResult.totalInterest)}</p>
              </div>
              <div>
                <span className="text-gray-600">Нийт төлбөр:</span>
                <p className="font-medium">{loanUtils.formatAmount(calculationResult.totalAmount)}</p>
              </div>
            </div>
          </div>
        )}

        {/* Documents */}
        <div>
          <h4 className="font-medium text-gray-900 mb-2">Баримт бичиг</h4>
          <div className="text-sm">
            {Object.keys(documentFiles).length > 0 ? (
              <ul className="space-y-1">
                {Object.entries(documentFiles).map(([docType, file]) => (
                  <li key={docType} className="flex items-center text-green-600">
                    <span className="mr-2">✓</span>
                    {getDocumentTypeName(docType)} - {file.name}
                  </li>
                ))}
              </ul>
            ) : (
              <p className="text-gray-500">Баримт байршуулаагүй</p>
            )}
          </div>
        </div>

        {/* Collateral */}
        {formData.collateral && formData.collateral.length > 0 && (
          <div>
            <h4 className="font-medium text-gray-900 mb-2">Барьцаа</h4>
            <div className="space-y-2 text-sm">
              {formData.collateral.map((collateral, index) => (
                <div key={index} className="flex justify-between">
                  <span>{getCollateralTypeName(collateral.type)}</span>
                  <span className="font-medium">{loanUtils.formatAmount(collateral.estimatedValue)}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );

  // Helper functions
  const getDocumentTypeName = (type: string) => {
    const names: Record<string, string> = {
      [DocumentType.IDENTITY_PROOF]: 'Иргэний үнэмлэх',
      [DocumentType.INCOME_PROOF]: 'Орлогын справка',
      [DocumentType.ADDRESS_PROOF]: 'Хаягийн баталгаа',
      [DocumentType.BANK_STATEMENTS]: 'Банкны хуулга',
      [DocumentType.TAX_RETURNS]: 'Татварын тайлан',
      [DocumentType.EMPLOYMENT_LETTER]: 'Ажлын газрын справка',
      [DocumentType.PROPERTY_DOCUMENTS]: 'Өмчийн гэрчилгээ',
      [DocumentType.COLLATERAL_DOCUMENTS]: 'Барьцааны баримт',
      [DocumentType.OTHER]: 'Бусад',
    };
    return names[type] || type;
  };

  const getLoanPurposeName = (purpose?: LoanPurpose) => {
    const names: Record<string, string> = {
      [LoanPurpose.HOME_PURCHASE]: 'Орон сууц худалдан авах',
      [LoanPurpose.HOME_IMPROVEMENT]: 'Орон сууц засварлах',
      [LoanPurpose.DEBT_CONSOLIDATION]: 'Өрийг нэгтгэх',
      [LoanPurpose.EDUCATION]: 'Боловсрол',
      [LoanPurpose.MEDICAL]: 'Эрүүл мэнд',
      [LoanPurpose.BUSINESS_EXPANSION]: 'Бизнес өргөжүүлэх',
      [LoanPurpose.VEHICLE_PURCHASE]: 'Тээврийн хэрэгсэл худалдан авах',
      [LoanPurpose.OTHER]: 'Бусад',
    };
    return names[purpose || LoanPurpose.OTHER] || 'Тодорхойгүй';
  };

  const getCollateralTypeName = (type: CollateralType) => {
    const names: Record<string, string> = {
      [CollateralType.REAL_ESTATE]: 'Үл хөдлөх хөрөнгө',
      [CollateralType.VEHICLE]: 'Тээврийн хэрэгсэл',
      [CollateralType.JEWELRY]: 'Үнэт эдлэл',
      [CollateralType.SECURITIES]: 'Үнэт цаас',
      [CollateralType.EQUIPMENT]: 'Тоног төхөөрөмж',
      [CollateralType.OTHER]: 'Бусад',
    };
    return names[type] || type;
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="px-6 py-4 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">
            {mode === 'create' ? 'Шинэ зээлийн хүсэлт' : 
             mode === 'edit' ? 'Зээлийн хүсэлт засах' : 
             'Зээлийн хүсэлтийн мэдээлэл'}
          </h2>
        </div>

        <div className="p-6">
          {!isReadOnly && <StepIndicator />}

          <div className="min-h-[500px]">
            {currentStep === 1 && renderBasicInfo()}
            {currentStep === 2 && renderDocuments()}
            {currentStep === 3 && renderCollateral()}
            {currentStep === 4 && renderReview()}
          </div>

          {/* Form Actions */}
          <div className="flex justify-between mt-8 pt-6 border-t border-gray-200">
            <div className="flex space-x-3">
              {currentStep > 1 && !isReadOnly && (
                <button
                  type="button"
                  onClick={handlePrevious}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  Өмнөх
                </button>
              )}
            </div>

            <div className="flex space-x-3">
              <button
                type="button"
                onClick={onCancel}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {isReadOnly ? 'Хаах' : 'Болих'}
              </button>

              {!isReadOnly && (
                <>
                  {currentStep < totalSteps ? (
                    <button
                      type="button"
                      onClick={handleNext}
                      className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                      Дараах
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={handleSubmit}
                      disabled={isLoading}
                      className={`
                        px-6 py-2 text-sm font-medium text-white rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500
                        ${isLoading 
                          ? 'bg-gray-400 cursor-not-allowed' 
                          : 'bg-green-600 hover:bg-green-700'
                        }
                      `}
                    >
                      {isLoading ? 'Илгээж байна...' : 
                       mode === 'create' ? 'Хүсэлт илгээх' : 'Шинэчлэх'}
                    </button>
                  )}
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoanApplicationForm;