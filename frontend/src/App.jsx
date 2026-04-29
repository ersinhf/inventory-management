import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { ConfigProvider } from "antd";
import trTR from "antd/locale/tr_TR";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import ProtectedRoute from "./auth/ProtectedRoute";
import AppLayout from "./components/AppLayout";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Suppliers from "./pages/Suppliers";
import Categories from "./pages/Categories";
import Products from "./pages/Products";

function ManagerOnlyRoute({ children }) {
  const { isWarehouseManager } = useAuth();
  return isWarehouseManager ? children : <Navigate to="/" replace />;
}

export default function App() {
  return (
    <ConfigProvider locale={trTR}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route
              element={
                <ProtectedRoute>
                  <AppLayout />
                </ProtectedRoute>
              }
            >
              <Route path="/" element={<Dashboard />} />
              <Route
                path="/suppliers"
                element={
                  <ManagerOnlyRoute>
                    <Suppliers />
                  </ManagerOnlyRoute>
                }
              />
              <Route path="/categories" element={<Categories />} />
              <Route path="/products" element={<Products />} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  );
}
