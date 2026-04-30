import { useState } from "react";
import { Outlet, useNavigate, useLocation } from "react-router-dom";
import { Layout, Menu, Button, Avatar, Dropdown, Typography } from "antd";
import {
  DashboardOutlined,
  ShopOutlined,
  AppstoreOutlined,
  InboxOutlined,
  SwapOutlined,
  ShoppingCartOutlined,
  FileDoneOutlined,
  LogoutOutlined,
  UserOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from "@ant-design/icons";
import { useAuth } from "../auth/AuthContext";

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

export default function AppLayout() {
  const { user, logout, isWarehouseManager } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);

  const menuItems = [
    { key: "/", icon: <DashboardOutlined />, label: "Panel" },
    ...(isWarehouseManager
      ? [{ key: "/suppliers", icon: <ShopOutlined />, label: "Tedarikçiler" }]
      : []),
    { key: "/categories", icon: <AppstoreOutlined />, label: "Kategoriler" },
    { key: "/products", icon: <InboxOutlined />, label: "Ürünler" },
    { key: "/stock-movements", icon: <SwapOutlined />, label: "Stok Hareketleri" },
    ...(isWarehouseManager
      ? [{ key: "/purchase-orders", icon: <ShoppingCartOutlined />, label: "Satın Alma" }]
      : []),
    { key: "/material-requests", icon: <FileDoneOutlined />, label: "Malzeme Talepleri" },
  ];

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  const userMenu = {
    items: [
      {
        key: "profile",
        icon: <UserOutlined />,
        label: `${user?.firstName} ${user?.lastName}`,
        disabled: true,
      },
      { type: "divider" },
      {
        key: "logout",
        icon: <LogoutOutlined />,
        label: "Çıkış Yap",
        onClick: handleLogout,
      },
    ],
  };

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider trigger={null} collapsible collapsed={collapsed} theme="dark">
        <div
          style={{
            color: "#fff",
            padding: 16,
            fontWeight: 600,
            fontSize: collapsed ? 14 : 16,
            textAlign: "center",
            whiteSpace: "nowrap",
            overflow: "hidden",
          }}
        >
          {collapsed ? "EYS" : "Envanter Yönetimi"}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>

      <Layout>
        <Header
          style={{
            background: "#fff",
            padding: "0 16px",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            borderBottom: "1px solid #f0f0f0",
          }}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: 16 }}
          />

          <Dropdown menu={userMenu} placement="bottomRight">
            <div style={{ cursor: "pointer", display: "flex", alignItems: "center", gap: 8 }}>
              <Avatar icon={<UserOutlined />} />
              <div style={{ display: "flex", flexDirection: "column", lineHeight: 1.2 }}>
                <Text strong>{user?.firstName} {user?.lastName}</Text>
                <Text type="secondary" style={{ fontSize: 12 }}>
                  {user?.role === "WAREHOUSE_MANAGER" ? "Depo Sorumlusu" : "Bölüm Çalışanı"}
                </Text>
              </div>
            </div>
          </Dropdown>
        </Header>

        <Content style={{ margin: 16, padding: 24, background: "#fff", borderRadius: 8 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
