import { useEffect, useState } from "react";
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Space,
  Tag,
  Popconfirm,
  message,
  Typography,
  Switch,
} from "antd";
import { PlusOutlined, EditOutlined } from "@ant-design/icons";
import { suppliersApi } from "../api/suppliers";

const { Title } = Typography;

export default function Suppliers() {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeOnly, setActiveOnly] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const fetchSuppliers = async () => {
    setLoading(true);
    try {
      const data = await suppliersApi.list(activeOnly);
      setSuppliers(data);
    } catch (err) {
      message.error("Tedarikçiler yüklenemedi: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSuppliers();
  }, [activeOnly]);

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleSubmit = async (values) => {
    setSubmitting(true);
    try {
      if (editing) {
        await suppliersApi.update(editing.id, values);
        message.success("Tedarikçi güncellendi");
      } else {
        await suppliersApi.create(values);
        message.success("Tedarikçi eklendi");
      }
      setModalOpen(false);
      fetchSuppliers();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleActive = async (record) => {
    try {
      if (record.active) {
        await suppliersApi.deactivate(record.id);
        message.success("Tedarikçi pasife alındı");
      } else {
        await suppliersApi.activate(record.id);
        message.success("Tedarikçi aktif edildi");
      }
      fetchSuppliers();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    }
  };

  const columns = [
    { title: "Firma Adı", dataIndex: "name", key: "name" },
    { title: "Yetkili Kişi", dataIndex: "contactPerson", key: "contactPerson" },
    { title: "E-posta", dataIndex: "email", key: "email" },
    { title: "Telefon", dataIndex: "phone", key: "phone" },
    { title: "Vergi No", dataIndex: "taxNumber", key: "taxNumber" },
    {
      title: "Durum",
      dataIndex: "active",
      key: "active",
      width: 100,
      render: (active) =>
        active ? <Tag color="green">Aktif</Tag> : <Tag color="default">Pasif</Tag>,
    },
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
  ];

  return (
    <div>
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16, flexWrap: "wrap", gap: 8 }}>
        <Title level={3} style={{ margin: 0 }}>Tedarikçiler</Title>
        <Space>
          <span>Sadece aktifler:</span>
          <Switch checked={activeOnly} onChange={setActiveOnly} />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            Yeni Tedarikçi
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={suppliers}
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title={editing ? "Tedarikçi Düzenle" : "Yeni Tedarikçi"}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={() => form.submit()}
        confirmLoading={submitting}
        okText="Kaydet"
        cancelText="İptal"
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            label="Firma Adı"
            name="name"
            rules={[{ required: true, message: "Firma adı zorunlu" }]}
          >
            <Input maxLength={150} />
          </Form.Item>

          <Form.Item label="Yetkili Kişi" name="contactPerson">
            <Input maxLength={100} />
          </Form.Item>

          <Form.Item
            label="E-posta"
            name="email"
            rules={[{ type: "email", message: "Geçerli e-posta giriniz" }]}
          >
            <Input maxLength={100} />
          </Form.Item>

          <Form.Item label="Telefon" name="phone">
            <Input maxLength={20} placeholder="+90 555 123 45 67" />
          </Form.Item>

          <Form.Item label="Adres" name="address">
            <Input.TextArea maxLength={300} rows={2} />
          </Form.Item>

          <Form.Item label="Vergi Numarası" name="taxNumber">
            <Input maxLength={20} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
