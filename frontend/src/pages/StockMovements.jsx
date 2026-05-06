import { useEffect, useMemo, useRef, useState } from "react";
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
  Tag,
  Popconfirm,
  message,
  Typography,
  DatePicker,
} from "antd";
import { PlusOutlined, ReloadOutlined, PrinterOutlined } from "@ant-design/icons";
import { stockMovementsApi } from "../api/stockMovements";
import { productsApi } from "../api/products";
import { useAuth } from "../auth/AuthContext";

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

const TYPE_META = {
  IN: { label: "Giriş", color: "green", sign: "+" },
  OUT: { label: "Çıkış", color: "red", sign: "−" },
  ADJUSTMENT: { label: "Sayım", color: "blue", sign: "=" },
};

function handlePrint(ref) {
  const content = ref.current?.innerHTML;
  if (!content) return;
  const win = window.open("", "_blank");
  win.document.write(`
    <html>
      <head>
        <title>Stok Hareketleri</title>
        <style>
          body { font-family: sans-serif; padding: 24px; }
          table { width: 100%; border-collapse: collapse; }
          th, td { border: 1px solid #ddd; padding: 8px; text-align: left; font-size: 13px; }
          th { background: #f0f0f0; }
          .ant-tag { border: 1px solid #ccc; padding: 1px 6px; border-radius: 4px; font-size: 12px; }
          .ant-table-pagination, button, .ant-space-item:has(button) { display: none !important; }
        </style>
      </head>
      <body>${content}</body>
    </html>
  `);
  win.document.close();
  win.focus();
  win.print();
  win.close();
}

export default function StockMovements() {
  const { isWarehouseManager } = useAuth();
  const [movements, setMovements] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);

  const [filterProductId, setFilterProductId] = useState();
  const [filterType, setFilterType] = useState();
  const [filterRange, setFilterRange] = useState();

  const [modalOpen, setModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();
  const watchedType = Form.useWatch("type", form);

  const printRef = useRef(null);

  const allowedTypes = useMemo(
    () =>
      isWarehouseManager
        ? ["IN", "OUT", "ADJUSTMENT"]
        : ["OUT"],
    [isWarehouseManager]
  );

  const fetchMovements = async (overrides) => {
    const productId = overrides?.productId !== undefined ? overrides.productId : filterProductId;
    const type = overrides?.type !== undefined ? overrides.type : filterType;
    const range = overrides?.range !== undefined ? overrides.range : filterRange;

    setLoading(true);
    try {
      const params = {};
      if (productId) params.productId = productId;
      if (type) params.type = type;
      if (range?.[0]) params.from = range[0].format("YYYY-MM-DDTHH:mm:ss");
      if (range?.[1]) params.to = range[1].format("YYYY-MM-DDTHH:mm:ss");

      const data = await stockMovementsApi.filter(params);
      setMovements(data);
    } catch (err) {
      message.error(
        "Hareketler yüklenemedi: " + (err.response?.data?.message || err.message)
      );
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const data = await productsApi.list(true);
      setProducts(data);
    } catch {
      // Sessizce geç
    }
  };

  useEffect(() => {
    fetchMovements();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleApplyFilters = () => fetchMovements();

  const handleResetFilters = () => {
    setFilterProductId(undefined);
    setFilterType(undefined);
    setFilterRange(undefined);
    fetchMovements({ productId: null, type: null, range: null });
  };

  const openCreate = () => {
    form.resetFields();
    form.setFieldsValue({ type: allowedTypes[0] });
    setModalOpen(true);
  };

  const handleSubmit = async (values) => {
    setSubmitting(true);
    try {
      await stockMovementsApi.create(values);
      message.success("Stok hareketi kaydedildi");
      setModalOpen(false);
      fetchMovements();
      fetchProducts();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = async (record) => {
    try {
      await stockMovementsApi.cancel(record.id);
      message.success("Hareket iptal edildi");
      fetchMovements();
      fetchProducts();
    } catch (err) {
      message.error(err.response?.data?.message || "İptal başarısız");
    }
  };

  const quantityLabel = (() => {
    if (watchedType === "IN") return "Eklenecek Miktar";
    if (watchedType === "OUT") return "Düşülecek Miktar";
    if (watchedType === "ADJUSTMENT") return "Sayım Sonrası Yeni Stok";
    return "Miktar";
  })();

  const columns = [
    {
      title: "Tarih",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 170,
      render: (v) => (v ? new Date(v).toLocaleString("tr-TR") : "-"),
    },
    {
      title: "Ürün",
      key: "product",
      render: (_, r) => (
        <Space direction="vertical" size={0}>
          <span>{r.product?.name}</span>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {r.product?.barcode}
          </Text>
        </Space>
      ),
    },
    {
      title: "Tip",
      dataIndex: "type",
      key: "type",
      width: 110,
      render: (t) => (
        <Tag color={TYPE_META[t]?.color}>{TYPE_META[t]?.label || t}</Tag>
      ),
    },
    {
      title: "Miktar",
      key: "quantity",
      width: 110,
      render: (_, r) => {
        const meta = TYPE_META[r.type];
        return (
          <Text
            strong
            style={{
              color:
                meta?.color === "red"
                  ? "#cf1322"
                  : meta?.color === "green"
                  ? "#389e0d"
                  : undefined,
            }}
          >
            {meta?.sign} {r.quantity}
          </Text>
        );
      },
    },
    {
      title: "Sonraki Stok",
      dataIndex: "stockAfter",
      key: "stockAfter",
      width: 120,
    },
    {
      title: "Durum",
      dataIndex: "active",
      key: "active",
      width: 100,
      render: (active) =>
        active === false ? (
          <Tag color="default">İptal</Tag>
        ) : (
          <Tag color="green">Aktif</Tag>
        ),
    },
    {
      title: "Yapan",
      key: "performedBy",
      width: 160,
      render: (_, r) => r.performedBy?.fullName || "-",
    },
    {
      title: "Not",
      dataIndex: "note",
      key: "note",
      ellipsis: true,
      render: (v) => v || <Text type="secondary">-</Text>,
    },
    ...(isWarehouseManager
      ? [
          {
            title: "İşlemler",
            key: "actions",
            width: 110,
            render: (_, record) =>
              record.active !== false ? (
                <Popconfirm
                  title="Bu hareket iptal edilsin mi?"
                  description="Stok miktarı geri alınmaz, sadece kayıt pasife alınır."
                  onConfirm={() => handleCancel(record)}
                  okText="İptal Et"
                  cancelText="Vazgeç"
                  okButtonProps={{ danger: true }}
                >
                  <Button size="small" danger>
                    İptal Et
                  </Button>
                </Popconfirm>
              ) : (
                <Text type="secondary" style={{ fontSize: 12 }}>
                  İptal edildi
                </Text>
              ),
          },
        ]
      : []),
  ];

  return (
    <div>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          marginBottom: 16,
          flexWrap: "wrap",
          gap: 12,
        }}
      >
        <Title level={3} style={{ margin: 0 }}>
          Stok Hareketleri
        </Title>
        <Space>
          <Button
            icon={<PrinterOutlined />}
            onClick={() => handlePrint(printRef)}
          >
            Yazdır
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            Yeni Hareket
          </Button>
        </Space>
      </div>

      <Space wrap style={{ marginBottom: 16 }}>
        <Select
          placeholder="Ürün filtrele"
          allowClear
          style={{ minWidth: 240 }}
          showSearch
          optionFilterProp="label"
          value={filterProductId}
          onChange={setFilterProductId}
          options={products.map((p) => ({
            value: p.id,
            label: `${p.name} (${p.barcode})`,
          }))}
        />
        <Select
          placeholder="Tip"
          allowClear
          style={{ width: 140 }}
          value={filterType}
          onChange={setFilterType}
          options={[
            { value: "IN", label: "Giriş" },
            { value: "OUT", label: "Çıkış" },
            { value: "ADJUSTMENT", label: "Sayım" },
          ]}
        />
        <RangePicker
          showTime
          value={filterRange}
          onChange={setFilterRange}
          format="DD.MM.YYYY HH:mm"
        />
        <Button type="primary" onClick={handleApplyFilters}>
          Filtrele
        </Button>
        <Button icon={<ReloadOutlined />} onClick={handleResetFilters}>
          Temizle
        </Button>
      </Space>

      <div ref={printRef}>
        <Table
          rowKey="id"
          columns={columns}
          dataSource={movements}
          loading={loading}
          pagination={{ pageSize: 15 }}
          scroll={{ x: 1100 }}
        />
      </div>

      <Modal
        title="Yeni Stok Hareketi"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={submitting}
        okText="Kaydet"
        cancelText="İptal"
        destroyOnClose
        width={520}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ type: allowedTypes[0] }}
        >
          <Form.Item
            label="Ürün"
            name="productId"
            rules={[{ required: true, message: "Ürün seçilmelidir" }]}
          >
            <Select
              showSearch
              placeholder="Ürün seç"
              optionFilterProp="label"
              options={products.map((p) => ({
                value: p.id,
                label: `${p.name} (${p.barcode}) — stok: ${p.currentStock}`,
              }))}
            />
          </Form.Item>

          <Form.Item
            label="Hareket Tipi"
            name="type"
            rules={[{ required: true, message: "Tip seçilmelidir" }]}
          >
            <Select
              options={allowedTypes.map((t) => ({
                value: t,
                label: TYPE_META[t].label,
              }))}
            />
          </Form.Item>

          <Form.Item
            label={quantityLabel}
            name="quantity"
            rules={[
              { required: true, message: "Miktar zorunlu" },
              {
                type: "number",
                min: 0,
                message: "Miktar 0 veya pozitif olmalı",
              },
            ]}
          >
            <InputNumber min={0} style={{ width: "100%" }} />
          </Form.Item>

          {watchedType === "ADJUSTMENT" && (
            <Text type="secondary" style={{ display: "block", marginBottom: 12 }}>
              Sayım sonrası bulduğun gerçek stok değerini gir. Bu değer, mevcut
              stoğun yerine geçer.
            </Text>
          )}

          <Form.Item label="Açıklama / Not" name="note">
            <Input.TextArea maxLength={500} rows={2} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
