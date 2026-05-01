import { useEffect, useRef, useState } from "react";
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
  Switch,
  Tooltip,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  SearchOutlined,
  WarningOutlined,
  QrcodeOutlined,
  DownloadOutlined,
} from "@ant-design/icons";
import { QRCodeCanvas } from "qrcode.react";
import { productsApi } from "../api/products";
import { categoriesApi } from "../api/categories";
import { suppliersApi } from "../api/suppliers";
import { useAuth } from "../auth/AuthContext";

const { Title, Text } = Typography;

export default function Products() {
  const { isWarehouseManager } = useAuth();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeOnly, setActiveOnly] = useState(false);
  const [lowStockOnly, setLowStockOnly] = useState(false);
  const [barcodeSearch, setBarcodeSearch] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();
  const [qrProduct, setQrProduct] = useState(null);
  const qrCanvasRef = useRef(null);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const data = lowStockOnly
        ? await productsApi.lowStock()
        : await productsApi.list(activeOnly);
      setProducts(data);
    } catch (err) {
      message.error("Ürünler yüklenemedi: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const fetchLookups = async () => {
    try {
      const [cats, sups] = await Promise.all([
        categoriesApi.list(),
        isWarehouseManager ? suppliersApi.list(true) : Promise.resolve([]),
      ]);
      setCategories(cats);
      setSuppliers(sups);
    } catch {
      // Bölüm Çalışanı tedarikçileri çekemez, sessizce geç.
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [activeOnly, lowStockOnly]);

  useEffect(() => {
    fetchLookups();
  }, []);

  const handleBarcodeSearch = async () => {
    const code = barcodeSearch.trim();
    if (!code) {
      fetchProducts();
      return;
    }
    setLoading(true);
    try {
      const product = await productsApi.getByBarcode(code);
      setProducts([product]);
    } catch (err) {
      if (err.response?.status === 404) {
        message.warning("Bu barkodla ürün bulunamadı");
        setProducts([]);
      } else {
        message.error("Arama başarısız");
      }
    } finally {
      setLoading(false);
    }
  };

  const clearSearch = () => {
    setBarcodeSearch("");
    fetchProducts();
  };

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue({
      name: record.name,
      description: record.description,
      barcode: record.barcode,
      unitPrice: record.unitPrice,
      minimumStockLevel: record.minimumStockLevel,
      categoryId: record.category?.id,
      supplierIds: record.suppliers?.map((s) => s.id) || [],
    });
    setModalOpen(true);
  };

  const handleSubmit = async (values) => {
    setSubmitting(true);
    try {
      const payload = {
        ...values,
        barcode: values.barcode?.trim() ? values.barcode.trim() : null,
        supplierIds: values.supplierIds || [],
      };
      if (editing) {
        await productsApi.update(editing.id, payload);
        message.success("Ürün güncellendi");
      } else {
        await productsApi.create(payload);
        message.success("Ürün eklendi");
      }
      setModalOpen(false);
      fetchProducts();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDownloadQr = () => {
    const canvas = qrCanvasRef.current?.querySelector("canvas");
    if (!canvas || !qrProduct) return;
    const url = canvas.toDataURL("image/png");
    const link = document.createElement("a");
    link.href = url;
    link.download = `${qrProduct.barcode}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleToggleActive = async (record) => {
    try {
      if (record.active) {
        await productsApi.deactivate(record.id);
        message.success("Ürün pasife alındı");
      } else {
        await productsApi.activate(record.id);
        message.success("Ürün aktif edildi");
      }
      fetchProducts();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    }
  };

  const columns = [
    { title: "Ürün Adı", dataIndex: "name", key: "name" },
    {
      title: "Kod",
      dataIndex: "barcode",
      key: "barcode",
      width: 150,
      render: (v) => <Text code>{v}</Text>,
    },
    {
      title: "QR",
      key: "qr",
      width: 70,
      align: "center",
      render: (_, r) => (
        <Tooltip title="QR kodunu göster">
          <Button
            type="text"
            icon={<QrcodeOutlined style={{ fontSize: 20 }} />}
            onClick={() => setQrProduct(r)}
          />
        </Tooltip>
      ),
    },
    {
      title: "Kategori",
      key: "category",
      render: (_, r) => r.category?.name || <Text type="secondary">-</Text>,
    },
    {
      title: "Tedarikçiler",
      key: "suppliers",
      render: (_, r) =>
        r.suppliers?.length ? (
          <Space size={[0, 4]} wrap>
            {r.suppliers.map((s) => (
              <Tag key={s.id}>{s.name}</Tag>
            ))}
          </Space>
        ) : (
          <Text type="secondary">-</Text>
        ),
    },
    {
      title: "Birim Fiyat",
      dataIndex: "unitPrice",
      key: "unitPrice",
      width: 120,
      render: (v) => `₺ ${Number(v).toFixed(2)}`,
    },
    {
      title: "Stok",
      key: "stock",
      width: 130,
      render: (_, r) => (
        <Space>
          <span>{r.currentStock} / {r.minimumStockLevel}</span>
          {r.lowStock && (
            <Tooltip title="Stok minimum seviyenin altında">
              <WarningOutlined style={{ color: "#faad14" }} />
            </Tooltip>
          )}
        </Space>
      ),
    },
    {
      title: "Durum",
      dataIndex: "active",
      key: "active",
      width: 90,
      render: (active) =>
        active ? <Tag color="green">Aktif</Tag> : <Tag>Pasif</Tag>,
    },
    ...(isWarehouseManager
      ? [
          {
            title: "İşlemler",
            key: "actions",
            width: 220,
            render: (_, record) => (
              <Space>
                <Button icon={<EditOutlined />} size="small" onClick={() => openEdit(record)}>
                  Düzenle
                </Button>
                <Popconfirm
                  title={record.active ? "Pasife alınsın mı?" : "Aktif edilsin mi?"}
                  onConfirm={() => handleToggleActive(record)}
                  okText="Evet"
                  cancelText="Hayır"
                >
                  <Button size="small" danger={record.active}>
                    {record.active ? "Pasife Al" : "Aktif Et"}
                  </Button>
                </Popconfirm>
              </Space>
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
        <Title level={3} style={{ margin: 0 }}>Ürünler</Title>
        {isWarehouseManager && (
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            Yeni Ürün
          </Button>
        )}
      </div>

      <Space wrap style={{ marginBottom: 16 }}>
        <Input
          placeholder="Barkod ile ara"
          value={barcodeSearch}
          onChange={(e) => setBarcodeSearch(e.target.value)}
          onPressEnter={handleBarcodeSearch}
          prefix={<SearchOutlined />}
          style={{ width: 240 }}
          allowClear
          onClear={clearSearch}
        />
        <Button onClick={handleBarcodeSearch}>Ara</Button>

        <Space>
          <span>Sadece aktifler:</span>
          <Switch
            checked={activeOnly}
            onChange={(v) => {
              setActiveOnly(v);
              if (v) setLowStockOnly(false);
            }}
            disabled={lowStockOnly}
          />
        </Space>

        {isWarehouseManager && (
          <Space>
            <span>Sadece düşük stok:</span>
            <Switch
              checked={lowStockOnly}
              onChange={(v) => {
                setLowStockOnly(v);
                if (v) setActiveOnly(false);
              }}
            />
          </Space>
        )}
      </Space>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={products}
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1200 }}
      />

      <Modal
        title={editing ? "Ürün Düzenle" : "Yeni Ürün"}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={submitting}
        okText="Kaydet"
        cancelText="İptal"
        width={600}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            label="Ürün Adı"
            name="name"
            rules={[{ required: true, message: "Ürün adı zorunlu" }]}
          >
            <Input maxLength={150} />
          </Form.Item>

          <Form.Item label="Açıklama" name="description">
            <Input.TextArea maxLength={500} rows={2} />
          </Form.Item>

          <Form.Item
            label="Kod / Barkod (opsiyonel)"
            name="barcode"
            extra="Boş bırakılırsa sistem otomatik üretir (PRD-000XXX). Tedarikçinin kendi barkodunu girmek için kullanabilirsin."
          >
            <Input maxLength={50} placeholder="Otomatik üretilecek" />
          </Form.Item>

          <Space style={{ display: "flex" }} size="large">
            <Form.Item
              label="Birim Fiyat (₺)"
              name="unitPrice"
              rules={[{ required: true, message: "Birim fiyat zorunlu" }]}
              style={{ flex: 1 }}
            >
              <InputNumber min={0} step={0.01} style={{ width: "100%" }} precision={2} />
            </Form.Item>

            <Form.Item
              label="Min. Stok Seviyesi"
              name="minimumStockLevel"
              rules={[{ required: true, message: "Min. stok zorunlu" }]}
              style={{ flex: 1 }}
            >
              <InputNumber min={0} style={{ width: "100%" }} />
            </Form.Item>
          </Space>

          <Form.Item label="Kategori" name="categoryId">
            <Select
              allowClear
              placeholder="Kategori seç (opsiyonel)"
              options={categories.map((c) => ({ value: c.id, label: c.name }))}
            />
          </Form.Item>

          <Form.Item label="Tedarikçiler" name="supplierIds">
            <Select
              mode="multiple"
              allowClear
              placeholder="Tedarikçi seç"
              options={suppliers.map((s) => ({ value: s.id, label: s.name }))}
            />
          </Form.Item>

          {editing && (
            <Text type="secondary">
              Mevcut stok: <strong>{editing.currentStock}</strong> — stok değişimi sadece stok hareketleri modülüyle yapılır.
            </Text>
          )}
        </Form>
      </Modal>

      <Modal
        title={qrProduct ? `${qrProduct.name} — QR Kod` : "QR Kod"}
        open={!!qrProduct}
        onCancel={() => setQrProduct(null)}
        footer={[
          <Button key="close" onClick={() => setQrProduct(null)}>
            Kapat
          </Button>,
          <Button
            key="download"
            type="primary"
            icon={<DownloadOutlined />}
            onClick={handleDownloadQr}
          >
            PNG İndir
          </Button>,
        ]}
        width={360}
        destroyOnClose
      >
        {qrProduct && (
          <div style={{ textAlign: "center" }}>
            <div ref={qrCanvasRef} style={{ display: "inline-block", padding: 8, background: "#fff" }}>
              <QRCodeCanvas
                value={qrProduct.barcode}
                size={256}
                level="M"
                includeMargin
              />
            </div>
            <div style={{ marginTop: 12 }}>
              <Text code style={{ fontSize: 16 }}>{qrProduct.barcode}</Text>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
