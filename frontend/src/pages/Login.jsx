import { useState } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import { Form, Input, Button, Card, Typography, message } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import { useAuth } from "../auth/AuthContext";

const { Title, Text } = Typography;

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);

  if (user) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async ({ email, password }) => {
    setSubmitting(true);
    try {
      await login(email, password);
      message.success("Giriş başarılı");
      navigate("/", { replace: true });
    } catch (err) {
      const msg =
        err.response?.status === 401
          ? "E-posta veya şifre hatalı"
          : err.response?.data?.message || "Giriş yapılamadı";
      message.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: "#f0f2f5",
        padding: 16,
      }}
    >
      <Card style={{ width: 400, boxShadow: "0 4px 12px rgba(0,0,0,0.08)" }}>
        <div style={{ textAlign: "center", marginBottom: 24 }}>
          <Title level={3} style={{ marginBottom: 4 }}>
            Envanter Yönetim Sistemi
          </Title>
          <Text type="secondary">Devam etmek için giriş yapın</Text>
        </div>

        <Form layout="vertical" onFinish={handleSubmit} autoComplete="off">
          <Form.Item
            label="E-posta"
            name="email"
            rules={[
              { required: true, message: "E-posta zorunlu" },
              { type: "email", message: "Geçerli bir e-posta giriniz" },
            ]}
          >
            <Input prefix={<UserOutlined />} placeholder="ornek@firma.com" />
          </Form.Item>

          <Form.Item
            label="Şifre"
            name="password"
            rules={[{ required: true, message: "Şifre zorunlu" }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="••••••••" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={submitting}>
              Giriş Yap
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
