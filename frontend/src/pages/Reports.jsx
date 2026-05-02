import { useCallback, useEffect, useState } from "react";
import { Tabs, Table, DatePicker, Button, Space, Tag, message, Typography } from "antd";
import { ReloadOutlined, BarChartOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import { reportsApi } from "../api/reports";

const { Title } = Typography;
const { RangePicker } = DatePicker;

const PO_STATUS_LABEL = {
  DRAFT: "Taslak",
  SENT: "Gönderildi",
  RECEIVED: "Teslim alındı",
  CANCELLED: "İptal",
};

const moneyFmt = (n) =>
  typeof n === "number"
    ? n.toLocaleString("tr-TR", { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    : n;

function useReportRange() {
  const [range, setRange] = useState(() => [
    dayjs().subtract(30, "day").startOf("day"),
    dayjs().endOf("day"),
  ]);
  const rangeParams = () => {
    if (!range?.[0] || !range?.[1]) return {};
    return {
      from: range[0].format("YYYY-MM-DDTHH:mm:ss"),
      to: range[1].format("YYYY-MM-DDTHH:mm:ss"),
    };
  };
  return { range, setRange, rangeParams };
}

export default function Reports() {
  const { range, setRange, rangeParams } = useReportRange();
  const [active, setActive] = useState("stock");

  const [stockRows, setStockRows] = useState([]);
  const [movers, setMovers] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [purchases, setPurchases] = useState([]);

  const [loadingStock, setLoadingStock] = useState(false);
  const [loadingMovers, setLoadingMovers] = useState(false);
  const [loadingSuppliers, setLoadingSuppliers] = useState(false);
  const [loadingPurchases, setLoadingPurchases] = useState(false);

  const loadStock = useCallback(async () => {
    setLoadingStock(true);
    try {
      const data = await reportsApi.currentStock();
      setStockRows(data);
    } catch (err) {
      message.error(err.response?.data?.message || "Güncel stok yüklenemedi");
    } finally {
      setLoadingStock(false);
    }
  }, []);

  const loadMovers = useCallback(async () => {
    setLoadingMovers(true);
    try {
      const params = { ...rangeParams(), limit: 30 };
      const data = await reportsApi.topMovers(params);
      setMovers(data);
    } catch (err) {
      message.error(err.response?.data?.message || "Hareket raporu yüklenemedi");
    } finally {
      setLoadingMovers(false);
    }
  }, [range]);

  const loadSuppliers = useCallback(async () => {
    setLoadingSuppliers(true);
    try {
      const data = await reportsApi.supplierTotals(rangeParams());
      setSuppliers(data);
    } catch (err) {
      message.error(err.response?.data?.message || "Tedarikçi raporu yüklenemedi");
    } finally {
      setLoadingSuppliers(false);
    }
  }, [range]);

  const loadPurchases = useCallback(async () => {
    setLoadingPurchases(true);
    try {
      const data = await reportsApi.purchaseSummary(rangeParams());
      setPurchases(data);
    } catch (err) {
      message.error(err.response?.data?.message || "Satın alma özeti yüklenemedi");
    } finally {
      setLoadingPurchases(false);
    }
  }, [range]);

  useEffect(() => {
    if (active === "stock") loadStock();
  }, [active, loadStock]);

  useEffect(() => {
    if (active === "movers") loadMovers();
  }, [active, loadMovers]);

  useEffect(() => {
    if (active === "suppliers") loadSuppliers();
  }, [active, loadSuppliers]);

  useEffect(() => {
    if (active === "purchases") loadPurchases();
  }, [active, loadPurchases]);

  const stockColumns = [
    { title: "Barkod", dataIndex: "barcode", key: "barcode", width: 120 },
    { title: "Ürün", dataIndex: "name", key: "name" },
    { title: "Kategori", dataIndex: "categoryName", key: "categoryName" },
    {
      title: "Stok",
      dataIndex: "currentStock",
      key: "currentStock",
      width: 90,
      render: (v, r) => (
        <span>
          {v}
          {r.lowStock ? (
            <Tag color="red" style={{ marginLeft: 6 }}>
              Eşik
            </Tag>
          ) : null}
        </span>
      ),
    },
    { title: "Min.", dataIndex: "minimumStockLevel", key: "minimumStockLevel", width: 70 },
    {
      title: "Birim fiyat",
      dataIndex: "unitPrice",
      key: "unitPrice",
      align: "right",
      render: (v) => moneyFmt(Number(v)),
    },
    {
      title: "Stok değeri",
      dataIndex: "totalValue",
      key: "totalValue",
      align: "right",
      render: (v) => moneyFmt(Number(v)),
    },
  ];

  const moverColumns = [
    { title: "Barkod", dataIndex: "barcode", key: "barcode" },
    { title: "Ürün", dataIndex: "name", key: "name" },
    { title: "Giriş", dataIndex: "totalIn", key: "totalIn", align: "right" },
    { title: "Çıkış", dataIndex: "totalOut", key: "totalOut", align: "right" },
    { title: "Hareket sayısı", dataIndex: "movementCount", key: "movementCount", align: "right" },
    {
      title: "Toplam hareket (adet)",
      key: "activity",
      align: "right",
      render: (_, r) => (r.totalIn ?? 0) + (r.totalOut ?? 0),
    },
  ];

  const supplierColumns = [
    { title: "Tedarikçi", dataIndex: "supplierName", key: "supplierName" },
    { title: "Sipariş sayısı", dataIndex: "orderCount", key: "orderCount", align: "right" },
    {
      title: "Toplam tutar",
      dataIndex: "totalAmount",
      key: "totalAmount",
      align: "right",
      render: (v) => moneyFmt(Number(v ?? 0)),
    },
  ];

  const purchaseColumns = [
    { title: "No", dataIndex: "orderId", key: "orderId", width: 70 },
    { title: "Tedarikçi", dataIndex: "supplierName", key: "supplierName" },
    {
      title: "Durum",
      dataIndex: "status",
      key: "status",
      width: 130,
      render: (s) => <Tag>{PO_STATUS_LABEL[s] ?? s}</Tag>,
    },
    {
      title: "Tutar",
      dataIndex: "totalAmount",
      key: "totalAmount",
      align: "right",
      render: (v) => moneyFmt(Number(v ?? 0)),
    },
    { title: "Kalem", dataIndex: "itemCount", key: "itemCount", align: "right", width: 80 },
    {
      title: "Oluşturulma",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 170,
      render: (t) => (t ? dayjs(t).format("DD.MM.YYYY HH:mm") : "—"),
    },
  ];

  const dateToolbar = (onRefresh) => (
    <Space wrap style={{ marginBottom: 16 }}>
      <span>Tarih aralığı:</span>
      <RangePicker
        showTime
        value={range}
        onChange={(v) => setRange(v || [])}
        format="DD.MM.YYYY HH:mm"
      />
      <Button icon={<ReloadOutlined />} onClick={onRefresh}>
        Yenile
      </Button>
    </Space>
  );

  const tabItems = [
    {
      key: "stock",
      label: "Güncel stok",
      children: (
        <div>
          <Space style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<ReloadOutlined />} onClick={loadStock}>
              Yenile
            </Button>
          </Space>
          <Table
            rowKey="productId"
            loading={loadingStock}
            columns={stockColumns}
            dataSource={stockRows}
            pagination={{ pageSize: 15 }}
            scroll={{ x: 900 }}
          />
        </div>
      ),
    },
    {
      key: "movers",
      label: "En çok hareket",
      children: (
        <div>
          {dateToolbar(loadMovers)}
          <Table
            rowKey={(r) => `${r.productId}-${r.barcode}`}
            loading={loadingMovers}
            columns={moverColumns}
            dataSource={movers}
            pagination={{ pageSize: 15 }}
          />
        </div>
      ),
    },
    {
      key: "suppliers",
      label: "Tedarikçi toplamları",
      children: (
        <div>
          {dateToolbar(loadSuppliers)}
          <Table
            rowKey="supplierId"
            loading={loadingSuppliers}
            columns={supplierColumns}
            dataSource={suppliers}
            pagination={{ pageSize: 15 }}
          />
        </div>
      ),
    },
    {
      key: "purchases",
      label: "Satın alma özeti",
      children: (
        <div>
          {dateToolbar(loadPurchases)}
          <Table
            rowKey="orderId"
            loading={loadingPurchases}
            columns={purchaseColumns}
            dataSource={purchases}
            pagination={{ pageSize: 15 }}
            scroll={{ x: 900 }}
          />
        </div>
      ),
    },
  ];

  return (
    <div>
      <Title level={3} style={{ marginTop: 0 }}>
        <BarChartOutlined style={{ marginRight: 8 }} />
        Raporlar
      </Title>
      <Tabs activeKey={active} onChange={setActive} items={tabItems} />
    </div>
  );
}
