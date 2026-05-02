import { useEffect, useMemo, useState } from "react";
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Select,
  Space,
  Tag,
  Popconfirm,
  message,
  Typography,
  Switch,
  Tooltip,
} from "antd";
import { PlusOutlined, EditOutlined, SafetyOutlined } from "@ant-design/icons";
import { usersApi } from "../api/users";
import { useAuth } from "../auth/AuthContext";

const { Title } = Typography;

const ROLE_OPTIONS = [
  { value: "WAREHOUSE_MANAGER", label: "Depo Sorumlusu" },
  { value: "DEPARTMENT_EMPLOYEE", label: "Bölüm Çalışanı" },
];

const roleLabel = (role) =>
  ROLE_OPTIONS.find((r) => r.value === role)?.label ?? role;

export default function Users() {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeOnly, setActiveOnly] = useState(false);
  const [search, setSearch] = useState("");

  const [createOpen, setCreateOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [roleOpen, setRoleOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [createForm] = Form.useForm();
  const [editForm] = Form.useForm();
  const [roleForm] = Form.useForm();

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const data = await usersApi.list();
      setUsers(data);
    } catch (err) {
      message.error("Kullanıcılar yüklenemedi: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return users.filter((u) => {
      if (activeOnly && !u.active) return false;
      if (!q) return true;
      const haystack = `${u.firstName} ${u.lastName} ${u.email} ${u.department ?? ""}`.toLowerCase();
      return haystack.includes(q);
    });
  }, [users, search, activeOnly]);

  const openCreate = () => {
    createForm.resetFields();
    createForm.setFieldsValue({ roleName: "DEPARTMENT_EMPLOYEE" });
    setCreateOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    editForm.setFieldsValue({
      firstName: record.firstName,
      lastName: record.lastName,
      email: record.email,
      department: record.department,
    });
    setEditOpen(true);
  };

  const openRole = (record) => {
    setEditing(record);
    roleForm.setFieldsValue({ roleName: record.role });
    setRoleOpen(true);
  };

  const handleCreate = async (values) => {
    setSubmitting(true);
    try {
      await usersApi.create(values);
      message.success("Kullanıcı oluşturuldu");
      setCreateOpen(false);
      fetchUsers();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = async (values) => {
    setSubmitting(true);
    try {
      await usersApi.update(editing.id, values);
      message.success("Kullanıcı güncellendi");
      setEditOpen(false);
      fetchUsers();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    } finally {
      setSubmitting(false);
    }
  };

  const handleRole = async (values) => {
    setSubmitting(true);
    try {
      await usersApi.updateRole(editing.id, values.roleName);
      message.success("Rol güncellendi");
      setRoleOpen(false);
      fetchUsers();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleActive = async (record) => {
    try {
      if (record.active) {
        await usersApi.deactivate(record.id);
        message.success("Kullanıcı pasife alındı");
      } else {
        await usersApi.activate(record.id);
        message.success("Kullanıcı aktif edildi");
      }
      fetchUsers();
    } catch (err) {
      message.error(err.response?.data?.message || "İşlem başarısız");
    }
  };

  const columns = [
    {
      title: "Ad Soyad",
      key: "fullName",
      render: (_, r) => `${r.firstName} ${r.lastName}`,
      sorter: (a, b) =>
        `${a.firstName} ${a.lastName}`.localeCompare(`${b.firstName} ${b.lastName}`, "tr"),
    },
    { title: "E-posta", dataIndex: "email", key: "email" },
    { title: "Departman", dataIndex: "department", key: "department" },
    {
      title: "Rol",
      dataIndex: "role",
      key: "role",
      width: 160,
      filters: ROLE_OPTIONS.map((r) => ({ text: r.label, value: r.value })),
      onFilter: (value, record) => record.role === value,
      render: (role) => (
        <Tag color={role === "WAREHOUSE_MANAGER" ? "geekblue" : "default"}>
          {roleLabel(role)}
        </Tag>
      ),
    },
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
      width: 360,
      render: (_, record) => {
        const isSelf = record.id === currentUser?.id;
        return (
          <Space wrap>
            <Button icon={<EditOutlined />} size="small" onClick={() => openEdit(record)}>
              Düzenle
            </Button>
            <Tooltip title={isSelf ? "Kendi rolünüzü değiştiremezsiniz" : ""}>
              <Button
                icon={<SafetyOutlined />}
                size="small"
                disabled={isSelf}
                onClick={() => openRole(record)}
              >
                Rol
              </Button>
            </Tooltip>
            <Tooltip title={isSelf ? "Kendi hesabınızı pasife alamazsınız" : ""}>
              <Popconfirm
                title={record.active ? "Pasife alınsın mı?" : "Aktif edilsin mi?"}
                onConfirm={() => handleToggleActive(record)}
                okText="Evet"
                cancelText="Hayır"
                disabled={isSelf}
              >
                <Button size="small" danger={record.active} disabled={isSelf}>
                  {record.active ? "Pasife Al" : "Aktif Et"}
                </Button>
              </Popconfirm>
            </Tooltip>
          </Space>
        );
      },
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
          gap: 8,
        }}
      >
        <Title level={3} style={{ margin: 0 }}>Kullanıcılar</Title>
        <Space wrap>
          <Input.Search
            allowClear
            placeholder="İsim, e-posta, departman ara"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ width: 280 }}
          />
          <span>Sadece aktifler:</span>
          <Switch checked={activeOnly} onChange={setActiveOnly} />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            Yeni Kullanıcı
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={filtered}
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title="Yeni Kullanıcı"
        open={createOpen}
        onCancel={() => setCreateOpen(false)}
        onOk={() => createForm.submit()}
        confirmLoading={submitting}
        okText="Oluştur"
        cancelText="İptal"
        destroyOnClose
      >
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            label="Ad"
            name="firstName"
            rules={[{ required: true, message: "Ad zorunlu" }]}
          >
            <Input maxLength={50} />
          </Form.Item>
          <Form.Item
            label="Soyad"
            name="lastName"
            rules={[{ required: true, message: "Soyad zorunlu" }]}
          >
            <Input maxLength={50} />
          </Form.Item>
          <Form.Item
            label="E-posta"
            name="email"
            rules={[
              { required: true, message: "E-posta zorunlu" },
              { type: "email", message: "Geçerli e-posta giriniz" },
            ]}
          >
            <Input maxLength={100} />
          </Form.Item>
          <Form.Item
            label="Şifre"
            name="password"
            rules={[
              { required: true, message: "Şifre zorunlu" },
              { min: 6, message: "Şifre en az 6 karakter olmalıdır" },
            ]}
          >
            <Input.Password maxLength={100} />
          </Form.Item>
          <Form.Item label="Departman" name="department">
            <Input maxLength={100} />
          </Form.Item>
          <Form.Item
            label="Rol"
            name="roleName"
            rules={[{ required: true, message: "Rol zorunlu" }]}
          >
            <Select options={ROLE_OPTIONS} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="Kullanıcıyı Düzenle"
        open={editOpen}
        onCancel={() => setEditOpen(false)}
        onOk={() => editForm.submit()}
        confirmLoading={submitting}
        okText="Kaydet"
        cancelText="İptal"
        destroyOnClose
      >
        <Form form={editForm} layout="vertical" onFinish={handleEdit}>
          <Form.Item
            label="Ad"
            name="firstName"
            rules={[{ required: true, message: "Ad zorunlu" }]}
          >
            <Input maxLength={50} />
          </Form.Item>
          <Form.Item
            label="Soyad"
            name="lastName"
            rules={[{ required: true, message: "Soyad zorunlu" }]}
          >
            <Input maxLength={50} />
          </Form.Item>
          <Form.Item
            label="E-posta"
            name="email"
            rules={[
              { required: true, message: "E-posta zorunlu" },
              { type: "email", message: "Geçerli e-posta giriniz" },
            ]}
          >
            <Input maxLength={100} />
          </Form.Item>
          <Form.Item label="Departman" name="department">
            <Input maxLength={100} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={editing ? `${editing.firstName} ${editing.lastName} - Rol Değiştir` : "Rol Değiştir"}
        open={roleOpen}
        onCancel={() => setRoleOpen(false)}
        onOk={() => roleForm.submit()}
        confirmLoading={submitting}
        okText="Kaydet"
        cancelText="İptal"
        destroyOnClose
      >
        <Form form={roleForm} layout="vertical" onFinish={handleRole}>
          <Form.Item
            label="Yeni Rol"
            name="roleName"
            rules={[{ required: true, message: "Rol zorunlu" }]}
          >
            <Select options={ROLE_OPTIONS} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
