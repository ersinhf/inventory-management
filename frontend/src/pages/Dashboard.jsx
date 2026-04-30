import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Card,
  Col,
  Descriptions,
  List,
  Row,
  Skeleton,
  Statistic,
  Tag,
  Typography,
  message,
} from "antd";
import {
  AlertOutlined,
  DollarOutlined,
  FileDoneOutlined,
  InboxOutlined,
  ShoppingCartOutlined,
} from "@ant-design/icons";
import { useAuth } from "../auth/AuthContext";
import { dashboardApi } from "../api/dashboard";

const { Title, Paragraph, Text } = Typography;

const TYPE_META = {
  IN: { label: "Giriş", color: "green", sign: "+" },
  OUT: { label: "Çıkış", color: "red", sign: "−" },
  ADJUSTMENT: { label: "Sayım", color: "blue", sign: "=" },
};

const currencyFormatter = new Intl.NumberFormat("tr-TR", {
  style: "currency",
  currency: "TRY",
  maximumFractionDigits: 2,
});

export default function Dashboard() {
  const { user } = useAuth();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    (async () => {
      try {
        const data = await dashboardApi.summary();
        if (active) setSummary(data);
      } catch (err) {
        message.error(
          "Panel verileri yüklenemedi: " +
            (err.response?.data?.message || err.message)
        );
      } finally {
        if (active) setLoading(false);
      }
    })();
    return () => {
      active = false;
    };
  }, []);

  const roleLabel =
    user?.role === "WAREHOUSE_MANAGER" ? "Depo Sorumlusu" : "Bölüm Çalışanı";
  const roleColor = user?.role === "WAREHOUSE_MANAGER" ? "blue" : "green";

  const lowStockCount = summary?.lowStockCount ?? 0;
  const activePOCount =
    (summary?.draftPurchaseOrders ?? 0) + (summary?.sentPurchaseOrders ?? 0);

  return (
    <div>
      <Title level={2} style={{ marginBottom: 4 }}>
        Hoş geldin, {user?.firstName}!
      </Title>
      <Paragraph type="secondary">
        Envanter Yönetim Sistemi'nin ana paneli. Aşağıda anlık durum özetini
        görebilirsin.
      </Paragraph>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={8} xl={5}>
          <Card>
            <Statistic
              title="Aktif Ürün"
              value={summary?.totalActiveProducts ?? 0}
              loading={loading}
              prefix={<InboxOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8} xl={5}>
          <Card>
            <Statistic
              title="Toplam Stok Değeri"
              value={Number(summary?.totalStockValue ?? 0)}
              loading={loading}
              precision={2}
              formatter={(v) => currencyFormatter.format(v)}
              prefix={<DollarOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8} xl={5}>
          <Card>
            <Statistic
              title="Kritik Stok"
              value={lowStockCount}
              loading={loading}
              prefix={<AlertOutlined />}
              valueStyle={{ color: lowStockCount > 0 ? "#cf1322" : undefined }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8} xl={5}>
          <Card>
            <Statistic
              title="Bekleyen Talep"
              value={summary?.pendingMaterialRequests ?? 0}
              loading={loading}
              prefix={<FileDoneOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8} xl={4}>
          <Card>
            <Statistic
              title="Aktif Sipariş"
              value={activePOCount}
              loading={loading}
              prefix={<ShoppingCartOutlined />}
              suffix={
                <Text type="secondary" style={{ fontSize: 12 }}>
                  ({summary?.draftPurchaseOrders ?? 0} taslak /{" "}
                  {summary?.sentPurchaseOrders ?? 0} gönderildi)
                </Text>
              }
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card
            title="Kritik Stoktaki Ürünler"
            extra={<Link to="/products">Tümü</Link>}
          >
            {loading ? (
              <Skeleton active paragraph={{ rows: 3 }} />
            ) : (
              <List
                locale={{ emptyText: "Kritik stok altında ürün yok" }}
                dataSource={summary?.lowStockProducts ?? []}
                renderItem={(item) => (
                  <List.Item
                    actions={[
                      <Tag color="red" key="stock">
                        {item.currentStock} / min {item.minimumStockLevel}
                      </Tag>,
                    ]}
                  >
                    <List.Item.Meta
                      title={item.name}
                      description={`Mevcut stok minimum seviyenin altında veya eşit`}
                    />
                  </List.Item>
                )}
              />
            )}
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card
            title="Son Stok Hareketleri"
            extra={<Link to="/stock-movements">Tümü</Link>}
          >
            {loading ? (
              <Skeleton active paragraph={{ rows: 3 }} />
            ) : (
              <List
                locale={{ emptyText: "Henüz stok hareketi yok" }}
                dataSource={summary?.recentMovements ?? []}
                renderItem={(item) => {
                  const meta = TYPE_META[item.type] || {};
                  return (
                    <List.Item
                      actions={[
                        <Text
                          key="qty"
                          strong
                          style={{
                            color:
                              meta.color === "red"
                                ? "#cf1322"
                                : meta.color === "green"
                                ? "#389e0d"
                                : undefined,
                          }}
                        >
                          {meta.sign} {item.quantity}
                        </Text>,
                      ]}
                    >
                      <List.Item.Meta
                        title={
                          <span>
                            <Tag color={meta.color}>{meta.label}</Tag>
                            {item.productName}
                          </span>
                        }
                        description={
                          <Text type="secondary" style={{ fontSize: 12 }}>
                            {item.performedBy} •{" "}
                            {item.createdAt
                              ? new Date(item.createdAt).toLocaleString("tr-TR")
                              : ""}
                          </Text>
                        }
                      />
                    </List.Item>
                  );
                }}
              />
            )}
          </Card>
        </Col>
      </Row>

      <Card title="Hesap Bilgileri" style={{ marginTop: 16, maxWidth: 600 }}>
        <Descriptions column={1}>
          <Descriptions.Item label="Ad Soyad">
            {user?.firstName} {user?.lastName}
          </Descriptions.Item>
          <Descriptions.Item label="E-posta">{user?.email}</Descriptions.Item>
          <Descriptions.Item label="Departman">
            {user?.department || "-"}
          </Descriptions.Item>
          <Descriptions.Item label="Rol">
            <Tag color={roleColor}>{roleLabel}</Tag>
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}
