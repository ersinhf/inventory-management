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
  message,
  Typography,
  Card,
  Descriptions,
} from "antd";
import {
  PlusOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  EyeOutlined,
  DeleteOutlined,
} from "@ant-design/icons";
import { materialRequestsApi } from "../api/materialRequests";
import { productsApi } from "../api/products";
import { useAuth } from "../auth/AuthContext";

const { Title, Text } = Typography;

const STATUS_META = {
  PENDING: { label: "Bekliyor", color: "orange" },
  APPROVED: { label: "Onaylandı", color: "green" },
  REJECTED: { label: "Reddedildi", color: "red" },
};

const formatDate = (v) => (v ? new Date(v).toLocaleString("tr-TR") : "-");

export default function MaterialRequests() {
  const { isWarehouseManager } = useAuth();
  const [requests, setRequests] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filterStatus, setFilterStatus] = useState();

  const [createOpen, setCreateOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const [detailOpen, setDetailOpen] = useState(false);
  const [detailReq, setDetailReq] = useState(null);

  const [decisionOpen, setDecisionOpen] = useState(false);
  const [decisionMode, setDecisionMode] = useState(null);
  const [decisionTarget, setDecisionTarget] = useState(null);
  const [decisionForm] = Form.useForm();

  const fetchRequests = async () => {
    setLoading(true);
    try {
      const params = filterStatus ? { status: filterStatus } : {};
      const data = await materialRequestsApi.list(params);
      setRequests(data);
    } catch (err) {
      message.error(
        "Talepler yüklenemedi: " + (err.response?.data?.message || err.message)
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
      // sessizce geç
    }
  };

  useEffect(() => {
    fetchRequests();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterStatus]);

  useEffect(() => {
    fetchProducts();
  }, []);

  const openCreate = () => {
    form.resetFields();
    form.setFieldsValue({ items: [{}] });
    setCreateOpen(true);
  };

  const openDetail = async (record) => {
    try {
      const fresh = await materialRequestsApi.get(record.id);
      setDetailReq(fresh);
      setDetailOpen(true);
    } catch (err) {
      message.error("Detay yüklenemedi: " + (err.response?.data?.message || err.message));
    }
  };

  const openDecision = (record, mode) => {
    setDecisionTarget(record);
    setDecisionMode(mode);
    decisionForm.resetFields();
    setDecisionOpen(true);
  };

  const handleCreate = async (values) => {
    setSubmitting(true);
    try {
      await materialRequestsApi.create(values);
      message.success("Talep oluşturuldu, onay bekleniyor");
      setCreateOpen(false);
      fetchRequests();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDecide = async (values) => {
    setSubmitting(true);
    try {
      const note = values.decisionNote;
      if (decisionMode === "approve") {
        await materialRequestsApi.approve(decisionTarget.id, note);
        message.success("Talep onaylandı, stoklar güncellendi");
      } else {
        await materialRequestsApi.reject(decisionTarget.id, note);
        message.success("Talep reddedildi");
      }
      setDecisionOpen(false);
      fetchRequests();
      fetchProducts();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    } finally {
      setSubmitting(false);
    }
  };

  const productOptions = products.map((p) => ({
    value: p.id,
    label: `${p.name} (${p.barcode}) — stok: ${p.currentStock}`,
  }));

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
      title: "Talep Eden",
      key: "requestedBy",
      render: (_, r) => (
        <Space direction="vertical" size={0}>
          <span>{r.requestedBy?.fullName}</span>
          {r.requestedBy?.department && (
            <Text type="secondary" style={{ fontSize: 12 }}>
              {r.requestedBy.department}
            </Text>
          )}
        </Space>
      ),
    },
    {
      title: "Satır",
      key: "lineCount",
      width: 80,
      render: (_, r) => r.items?.length || 0,
    },
    {
      title: "Gerekçe",
      dataIndex: "reason",
      key: "reason",
      ellipsis: true,
      render: (v) => v || <Text type="secondary">-</Text>,
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
      title: "Karar Tarihi",
      dataIndex: "decidedAt",
      key: "decidedAt",
      width: 160,
      render: formatDate,
    },
    {
      title: "İşlemler",
      key: "actions",
      width: 280,
      render: (_, record) => (
        <Space wrap>
          <Button size="small" icon={<EyeOutlined />} onClick={() => openDetail(record)}>
            Detay
          </Button>
          {isWarehouseManager && record.status === "PENDING" && (
            <>
              <Button
                size="small"
                type="primary"
                icon={<CheckCircleOutlined />}
                onClick={() => openDecision(record, "approve")}
              >
                Onayla
              </Button>
              <Button
                size="small"
                danger
                icon={<CloseCircleOutlined />}
                onClick={() => openDecision(record, "reject")}
              >
                Reddet
              </Button>
            </>
          )}
        </Space>
      ),
    },
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
          Malzeme Talepleri
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
          Yeni Talep
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
        {!isWarehouseManager && (
          <Text type="secondary">Yalnızca kendi talepleriniz görünür.</Text>
        )}
      </Space>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={requests}
        loading={loading}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1200 }}
      />

      {/* Yeni talep modalı */}
      <Modal
        title="Yeni Malzeme Talebi"
        open={createOpen}
        onCancel={() => setCreateOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={submitting}
        okText="Talep Gönder"
        cancelText="İptal"
        width={720}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            label="Gerekçe"
            name="reason"
            rules={[{ max: 500, message: "En fazla 500 karakter" }]}
          >
            <Input.TextArea
              rows={2}
              maxLength={500}
              placeholder="Hangi departman / hangi iş için?"
            />
          </Form.Item>

          <Title level={5}>Talep Edilen Ürünler</Title>
          <Form.List
            name="items"
            rules={[
              {
                validator: async (_, items) => {
                  if (!items || items.length === 0) {
                    return Promise.reject(new Error("En az bir ürün eklemelisin"));
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
                      style={{ minWidth: 360, marginBottom: 0 }}
                    >
                      <Select
                        showSearch
                        optionFilterProp="label"
                        placeholder="Ürün"
                        options={productOptions}
                      />
                    </Form.Item>
                    <Form.Item
                      name={[name, "quantity"]}
                      rules={[{ required: true, message: "Miktar" }]}
                      style={{ marginBottom: 0 }}
                    >
                      <InputNumber min={1} placeholder="Miktar" style={{ width: 140 }} />
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

      {/* Detay modalı */}
      <Modal
        title={detailReq ? `Talep Detayı #${detailReq.id}` : "Detay"}
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={null}
        width={720}
        destroyOnClose
      >
        {detailReq && (
          <>
            <Card style={{ marginBottom: 16 }}>
              <Descriptions column={2} size="small">
                <Descriptions.Item label="Durum">
                  <Tag color={STATUS_META[detailReq.status]?.color}>
                    {STATUS_META[detailReq.status]?.label}
                  </Tag>
                </Descriptions.Item>
                <Descriptions.Item label="Talep Tarihi">
                  {formatDate(detailReq.createdAt)}
                </Descriptions.Item>
                <Descriptions.Item label="Talep Eden">
                  {detailReq.requestedBy?.fullName}
                </Descriptions.Item>
                <Descriptions.Item label="Departman">
                  {detailReq.requestedBy?.department || "-"}
                </Descriptions.Item>
                {detailReq.decidedBy && (
                  <>
                    <Descriptions.Item label="Karar Veren">
                      {detailReq.decidedBy.fullName}
                    </Descriptions.Item>
                    <Descriptions.Item label="Karar Tarihi">
                      {formatDate(detailReq.decidedAt)}
                    </Descriptions.Item>
                  </>
                )}
                {detailReq.reason && (
                  <Descriptions.Item label="Gerekçe" span={2}>
                    {detailReq.reason}
                  </Descriptions.Item>
                )}
                {detailReq.decisionNote && (
                  <Descriptions.Item label="Karar Notu" span={2}>
                    {detailReq.decisionNote}
                  </Descriptions.Item>
                )}
              </Descriptions>
            </Card>

            <Table
              rowKey="id"
              size="small"
              pagination={false}
              dataSource={detailReq.items}
              columns={[
                { title: "Ürün", dataIndex: "productName", key: "productName" },
                {
                  title: "Barkod",
                  dataIndex: "productBarcode",
                  key: "productBarcode",
                  width: 140,
                },
                { title: "Talep Adedi", dataIndex: "quantity", key: "quantity", width: 110 },
                {
                  title: "Mevcut Stok",
                  dataIndex: "currentStock",
                  key: "currentStock",
                  width: 110,
                },
              ]}
            />
          </>
        )}
      </Modal>

      {/* Onay/Red modalı (manager) */}
      <Modal
        title={
          decisionMode === "approve"
            ? `Talebi Onayla #${decisionTarget?.id}`
            : `Talebi Reddet #${decisionTarget?.id}`
        }
        open={decisionOpen}
        onCancel={() => setDecisionOpen(false)}
        onOk={() => decisionForm.submit()}
        confirmLoading={submitting}
        okText={decisionMode === "approve" ? "Onayla" : "Reddet"}
        okButtonProps={{ danger: decisionMode === "reject" }}
        cancelText="Vazgeç"
        destroyOnClose
      >
        <Text type="secondary" style={{ display: "block", marginBottom: 12 }}>
          {decisionMode === "approve"
            ? "Onaylanırsa her satır için otomatik OUT stok hareketi oluşturulur. Yetersiz stok varsa onay reddedilir."
            : "Red için bir gerekçe girmelisin."}
        </Text>
        <Form form={decisionForm} layout="vertical" onFinish={handleDecide}>
          <Form.Item
            label={decisionMode === "approve" ? "Onay Notu (opsiyonel)" : "Red Gerekçesi"}
            name="decisionNote"
            rules={
              decisionMode === "reject"
                ? [{ required: true, message: "Red gerekçesi zorunlu" }]
                : []
            }
          >
            <Input.TextArea rows={3} maxLength={500} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
