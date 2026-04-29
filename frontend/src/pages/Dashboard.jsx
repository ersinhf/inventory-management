import { Card, Typography, Descriptions, Tag } from "antd";
import { useAuth } from "../auth/AuthContext";

const { Title, Paragraph } = Typography;

export default function Dashboard() {
  const { user } = useAuth();

  const roleLabel =
    user?.role === "WAREHOUSE_MANAGER" ? "Depo Sorumlusu" : "Bölüm Çalışanı";
  const roleColor = user?.role === "WAREHOUSE_MANAGER" ? "blue" : "green";

  return (
    <div>
      <Title level={2}>Hoş geldin, {user?.firstName}!</Title>
      <Paragraph type="secondary">
        Envanter Yönetim Sistemi'nin ana paneline hoş geldin. Sol menüden modüllere erişebilirsin.
      </Paragraph>

      <Card title="Hesap Bilgileri" style={{ marginTop: 24, maxWidth: 600 }}>
        <Descriptions column={1}>
          <Descriptions.Item label="Ad Soyad">
            {user?.firstName} {user?.lastName}
          </Descriptions.Item>
          <Descriptions.Item label="E-posta">{user?.email}</Descriptions.Item>
          <Descriptions.Item label="Departman">{user?.department || "-"}</Descriptions.Item>
          <Descriptions.Item label="Rol">
            <Tag color={roleColor}>{roleLabel}</Tag>
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}
