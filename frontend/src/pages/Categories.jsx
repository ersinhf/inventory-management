import { useEffect, useState } from "react";
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Space,
  Popconfirm,
  message,
  Typography,
} from "antd";
import { PlusOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import { categoriesApi } from "../api/categories";
import { useAuth } from "../auth/AuthContext";

const { Title } = Typography;

export default function Categories() {
  const { isWarehouseManager } = useAuth();
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const data = await categoriesApi.list();
      setCategories(data);
    } catch (err) {
      message.error("Kategoriler yüklenemedi: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

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
        await categoriesApi.update(editing.id, values);
        message.success("Kategori güncellendi");
      } else {
        await categoriesApi.create(values);
        message.success("Kategori eklendi");
      }
      setModalOpen(false);
      fetchCategories();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await categoriesApi.delete(id);
      message.success("Kategori silindi");
      fetchCategories();
    } catch (err) {
      message.error(err.response?.data?.message || "Silinemedi");
    }
  };

  const columns = [
    { title: "Kategori Adı", dataIndex: "name", key: "name" },
    { title: "Açıklama", dataIndex: "description", key: "description" },
    ...(isWarehouseManager
      ? [
          {
            title: "İşlemler",
            key: "actions",
            width: 200,
            render: (_, record) => (
              <Space>
                <Button icon={<EditOutlined />} size="small" onClick={() => openEdit(record)}>
                  Düzenle
                </Button>
                <Popconfirm
                  title="Bu kategoriyi silmek istiyor musun?"
                  description="Bu kategoriye bağlı ürünler kategorisiz kalır."
                  onConfirm={() => handleDelete(record.id)}
                  okText="Evet"
                  cancelText="Hayır"
                >
                  <Button icon={<DeleteOutlined />} size="small" danger>
                    Sil
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
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16, flexWrap: "wrap", gap: 8 }}>
        <Title level={3} style={{ margin: 0 }}>Kategoriler</Title>
        {isWarehouseManager && (
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            Yeni Kategori
          </Button>
        )}
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={categories}
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title={editing ? "Kategori Düzenle" : "Yeni Kategori"}
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
            label="Kategori Adı"
            name="name"
            rules={[{ required: true, message: "Kategori adı zorunlu" }]}
          >
            <Input maxLength={100} />
          </Form.Item>

          <Form.Item label="Açıklama" name="description">
            <Input.TextArea maxLength={300} rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
