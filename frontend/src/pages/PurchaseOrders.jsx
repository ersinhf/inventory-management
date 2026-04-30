import { useEffect, useState } from "react";
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
  Card,
  Descriptions,
} from "antd";
import {
  PlusOutlined,
  EditOutlined,
  SendOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  EyeOutlined,
  DeleteOutlined,
} from "@ant-design/icons";
import { purchaseOrdersApi } from "../api/purchaseOrders";
import { suppliersApi } from "../api/suppliers";
import { productsApi } from "../api/products";

const { Title, Text } = Typography;

const STATUS_META = {
  DRAFT: { label: "Taslak", color: "default" },
  SENT: { label: "Gönderildi", color: "blue" },
  RECEIVED: { label: "Teslim Alındı", color: "green" },
  CANCELLED: { label: "İptal", color: "red" },
};

const formatCurrency = (v) => `₺ ${Number(v ?? 0).toFixed(2)}`;
const formatDate = (v) => (v ? new Date(v).toLocaleString("tr-TR") : "-");

export default function PurchaseOrders() {
  const [orders, setOrders] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filterStatus, setFilterStatus] = useState();

  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const [detailOpen, setDetailOpen] = useState(false);
  const [detailOrder, setDetailOrder] = useState(null);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const data = await purchaseOrdersApi.list(filterStatus);
      setOrders(data);
    } catch (err) {
      message.error(
        "Siparişler yüklenemedi: " + (err.response?.data?.message || err.message)
      );
    } finally {
      setLoading(false);
    }
  };

  const fetchLookups = async () => {
    try {
      const [sups, prods] = await Promise.all([
        suppliersApi.list(true),
        productsApi.list(true),
      ]);
      setSuppliers(sups);
      setProducts(prods);
    } catch {
      // sessizce geç
    }
  };

  useEffect(() => {
    fetchOrders();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterStatus]);

  useEffect(() => {
    fetchLookups();
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ items: [{}] });
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue({
      supplierId: record.supplier?.id,
      note: record.note,
      items: record.items.map((it) => ({
        productId: it.productId,
        quantity: it.quantity,
        unitPrice: Number(it.unitPrice),
      })),
    });
    setModalOpen(true);
  };

  const openDetail = async (record) => {
    try {
      const fresh = await purchaseOrdersApi.get(record.id);
      setDetailOrder(fresh);
      setDetailOpen(true);
    } catch (err) {
      message.error("Detay yüklenemedi: " + (err.response?.data?.message || err.message));
    }
  };

  const handleSubmit = async (values) => {
    setSubmitting(true);
    try {
      const payload = {
        supplierId: values.supplierId,
        note: values.note,
        items: values.items,
      };
      if (editing) {
        await purchaseOrdersApi.update(editing.id, payload);
        message.success("Sipariş güncellendi");
      } else {
        await purchaseOrdersApi.create(payload);
        message.success("Sipariş taslak olarak oluşturuldu");
      }
      setModalOpen(false);
      fetchOrders();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    } finally {
      setSubmitting(false);
    }
  };

  const handleAction = async (id, action, successMsg) => {
    try {
      await action(id);
      message.success(successMsg);
      fetchOrders();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    }
  };

  const columns = [
    {
      title: "No",
      dataIndex: "id",
      key: "id",
      width: 70,
      render: (v) => <Text strong>#{v}</Text>,
    },
    {
      title: "Tarih",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 160,
      render: formatDate,
    },
    {
      title: "Tedarikçi",
      key: "supplier",
      render: (_, r) => r.supplier?.name,
    },
    {
      title: "Satır",
      key: "lineCount",
      width: 80,
      render: (_, r) => r.items?.length || 0,
    },
    {
      title: "Toplam",
      dataIndex: "totalAmount",
      key: "totalAmount",
      width: 130,
      render: (v) => <Text strong>{formatCurrency(v)}</Text>,
    },
    {
      title: "Durum",
      dataIndex: "status",
      key: "status",
      width: 130,
      render: (s) => (
        <Tag color={STATUS_META[s]?.color}>{STATUS_META[s]?.label || s}</Tag>
      ),
    },
    {
      title: "Tedarik Süresi",
      dataIndex: "leadTimeDays",
      key: "leadTimeDays",
      width: 130,
      render: (v) =>
        v == null ? <Text type="secondary">-</Text> : `${v} gün`,
    },
    {
      title: "İşlemler",
      key: "actions",
      width: 360,
      render: (_, record) => {
        const status = record.status;
        return (
          <Space wrap>
            <Button size="small" icon={<EyeOutlined />} onClick={() => openDetail(record)}>
              Detay
            </Button>
            {status === "DRAFT" && (
              <>
                <Button
                  size="small"
                  icon={<EditOutlined />}
                  onClick={() => openEdit(record)}
                >
                  Düzenle
                </Button>
                <Popconfirm
                  title="Tedarikçiye gönderilsin mi?"
                  onConfirm={() =>
                    handleAction(record.id, purchaseOrdersApi.send, "Sipariş gönderildi")
                  }
                  okText="Evet"
                  cancelText="Hayır"
                >
                  <Button size="small" type="primary" icon={<SendOutlined />}>
                    Gönder
                  </Button>
                </Popconfirm>
              </>
            )}
            {status === "SENT" && (
              <Popconfirm
                title="Mal teslim alındı, stoklar güncellensin mi?"
                onConfirm={() =>
                  handleAction(
                    record.id,
                    purchaseOrdersApi.receive,
                    "Sipariş teslim alındı, stoklar güncellendi"
                  )
                }
                okText="Evet"
                cancelText="Hayır"
              >
                <Button size="small" type="primary" icon={<CheckCircleOutlined />}>
                  Teslim Al
                </Button>
              </Popconfirm>
            )}
            {(status === "DRAFT" || status === "SENT") && (
              <Popconfirm
                title="Sipariş iptal edilsin mi?"
                onConfirm={() =>
                  handleAction(
                    record.id,
                    purchaseOrdersApi.cancel,
                    "Sipariş iptal edildi"
                  )
                }
                okText="Evet"
                cancelText="Hayır"
              >
                <Button size="small" danger icon={<CloseCircleOutlined />}>
                  İptal
                </Button>
              </Popconfirm>
            )}
          </Space>
        );
      },
    },
  ];

  const productOptions = products.map((p) => ({
    value: p.id,
    label: `${p.name} (${p.barcode})`,
    unitPrice: p.unitPrice,
  }));

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
          Satın Alma Siparişleri
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          Yeni Sipariş
        </Button>
      </div>

      <Space wrap style={{ marginBottom: 16 }}>
        <Select
          placeholder="Durum filtrele"
          allowClear
          style={{ width: 180 }}
          value={filterStatus}
          onChange={setFilterStatus}
          options={Object.entries(STATUS_META).map(([k, v]) => ({
            value: k,
            label: v.label,
          }))}
        />
      </Space>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={orders}
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1300 }}
      />

      <Modal
        title={editing ? `Sipariş Düzenle #${editing.id}` : "Yeni Satın Alma Siparişi"}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={submitting}
        okText="Kaydet"
        cancelText="İptal"
        width={820}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            label="Tedarikçi"
            name="supplierId"
            rules={[{ required: true, message: "Tedarikçi seçilmelidir" }]}
          >
            <Select
              showSearch
              optionFilterProp="label"
              placeholder="Tedarikçi seç"
              options={suppliers.map((s) => ({ value: s.id, label: s.name }))}
            />
          </Form.Item>

          <Form.Item label="Not" name="note">
            <Input.TextArea maxLength={500} rows={2} />
          </Form.Item>

          <Title level={5}>Satırlar</Title>
          <Form.List
            name="items"
            rules={[
              {
                validator: async (_, items) => {
                  if (!items || items.length === 0) {
                    return Promise.reject(new Error("En az bir satır eklemelisin"));
                  }
                },
              },
            ]}
          >
            {(fields, { add, remove }, { errors }) => (
              <>
                {fields.map(({ key, name }) => (
                  <Space
                    key={key}
                    align="baseline"
                    style={{ display: "flex", marginBottom: 8 }}
                    wrap
                  >
                    <Form.Item
                      name={[name, "productId"]}
                      rules={[{ required: true, message: "Ürün seç" }]}
                      style={{ minWidth: 320, marginBottom: 0 }}
                    >
                      <Select
                        showSearch
                        optionFilterProp="label"
                        placeholder="Ürün"
                        options={productOptions}
                        onChange={(value) => {
                          const opt = productOptions.find((o) => o.value === value);
                          if (opt && opt.unitPrice != null) {
                            const items = form.getFieldValue("items") || [];
                            items[name] = {
                              ...items[name],
                              unitPrice: Number(opt.unitPrice),
                            };
                            form.setFieldsValue({ items });
                          }
                        }}
                      />
                    </Form.Item>
                    <Form.Item
                      name={[name, "quantity"]}
                      rules={[{ required: true, message: "Miktar" }]}
                      style={{ marginBottom: 0 }}
                    >
                      <InputNumber min={1} placeholder="Miktar" style={{ width: 120 }} />
                    </Form.Item>
                    <Form.Item
                      name={[name, "unitPrice"]}
                      rules={[{ required: true, message: "Fiyat" }]}
                      style={{ marginBottom: 0 }}
                    >
                      <InputNumber
                        min={0}
                        step={0.01}
                        precision={2}
                        placeholder="Birim fiyat"
                        style={{ width: 140 }}
                        prefix="₺"
                      />
                    </Form.Item>
                    <Button
                      icon={<DeleteOutlined />}
                      danger
                      type="text"
                      onClick={() => remove(name)}
                    />
                  </Space>
                ))}
                <Form.Item>
                  <Button
                    type="dashed"
                    onClick={() => add()}
                    icon={<PlusOutlined />}
                    block
                  >
                    Satır Ekle
                  </Button>
                  <Form.ErrorList errors={errors} />
                </Form.Item>
              </>
            )}
          </Form.List>
        </Form>
      </Modal>

      <Modal
        title={detailOrder ? `Sipariş Detayı #${detailOrder.id}` : "Detay"}
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={null}
        width={760}
        destroyOnClose
      >
        {detailOrder && (
          <>
            <Card style={{ marginBottom: 16 }}>
              <Descriptions column={2} size="small">
                <Descriptions.Item label="Durum">
                  <Tag color={STATUS_META[detailOrder.status]?.color}>
                    {STATUS_META[detailOrder.status]?.label}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="Tedarikçi">
                  {detailOrder.supplier?.name}
                </Descriptions.Item>
                <Descriptions.Item label="Oluşturuldu">
                  {formatDate(detailOrder.createdAt)}
                </Descriptions.Item>
                <Descriptions.Item label="Gönderildi">
                  {formatDate(detailOrder.sentAt)}
                </Descriptions.Item>
                <Descriptions.Item label="Teslim Alındı">
                  {formatDate(detailOrder.receivedAt)}
                </Descriptions.Item>
                <Descriptions.Item label="Tedarik Süresi">
                  {detailOrder.leadTimeDays == null
                    ? "-"
                    : `${detailOrder.leadTimeDays} gün`}
                </Descriptions.Item>
                <Descriptions.Item label="Oluşturan">
                  {detailOrder.createdBy?.fullName}
                </Descriptions.Item>
                <Descriptions.Item label="Toplam">
                  <Text strong>{formatCurrency(detailOrder.totalAmount)}</Text>
                </Descriptions.Item>
                {detailOrder.note && (
                  <Descriptions.Item label="Not" span={2}>
                    {detailOrder.note}
                  </Descriptions.Item>
                )}
              </Descriptions>
            </Card>

            <Table
              rowKey="id"
              size="small"
              pagination={false}
              dataSource={detailOrder.items}
              columns={[
                { title: "Ürün", dataIndex: "productName", key: "productName" },
                { title: "Barkod", dataIndex: "productBarcode", key: "productBarcode", width: 140 },
                { title: "Miktar", dataIndex: "quantity", key: "quantity", width: 90 },
                {
                  title: "Birim Fiyat",
                  dataIndex: "unitPrice",
                  key: "unitPrice",
                  width: 120,
                  render: formatCurrency,
                },
                {
                  title: "Satır Toplamı",
                  dataIndex: "lineTotal",
                  key: "lineTotal",
                  width: 130,
                  render: (v) => <Text strong>{formatCurrency(v)}</Text>,
                },
              ]}
            />
          </>
        )}
      </Modal>
    </div>
  );
}
