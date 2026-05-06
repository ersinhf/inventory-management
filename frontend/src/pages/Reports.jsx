import { useCallback, useEffect, useState, useRef } from "react";
import {
  Tabs,
  Table,
  DatePicker,
  Button,
  Space,
  Tag,
  message,
  Typography,
  Select,
} from "antd";
import { ReloadOutlined, BarChartOutlined, PrinterOutlined } from "@ant-design/icons";
import dayjs from "dayjs";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from "recharts";
import { reportsApi } from "../api/reports";

const { Title } = Typography;
const { RangePicker } = DatePicker;

const PO_STATUS_LABEL = {
  DRAFT: "Taslak",
  SENT: "Gönderildi",
  RECEIVED: "Teslim alındı",
  CANCELLED: "İptal",
};

const CHART_COLORS = [
  "#4f6ef7", "#36b37e", "#ff7452", "#ffab00", "#6554c0",
  "#00b8d9", "#ff5630", "#57d9a3", "#998dd9", "#79e2f2",
];

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

// Yazdır butonu: sadece ilgili alanı yazdırır
function handlePrint(ref) {
  const content = ref.current?.innerHTML;
  if (!content) return;
  const win = window.open("", "_blank");
  win.document.write(`
    <html>
      <head>
        <title>Rapor</title>
        <style>
          body { font-family: sans-serif; padding: 24px; }
          table { width: 100%; border-collapse: collapse; }
          th, td { border: 1px solid #ddd; padding: 8px; text-align: left; font-size: 13px; }
          th { background: #f0f0f0; }
          h3 { margin-bottom: 16px; }
          .ant-tag { border: 1px solid #ccc; padding: 1px 6px; border-radius: 4px; font-size: 12px; }
          .ant-table-pagination, button, .ant-space, .ant-picker { display: none !important; }
        </style>
      </head>
      <body>${content}</body>
    </html>
  `);
  win.document.close();
  win.focus();
  win.print();
  win.close();
}

export default function Reports() {
  const { range, setRange, rangeParams } = useReportRange();
  const [active, setActive] = useState("stock");

  const [stockRows, setStockRows] = useState([]);
  const [movers, setMovers] = useState([]);
  const [suppliers, setSuppliers] = useState([]);
  const [purchases, setPurchases] = useState([]);

  // Tedarikçi filtresi
  const [selectedSupplier, setSelectedSupplier] = useState(null);

  const [loadingStock, setLoadingStock] = useState(false);
  const [loadingMovers, setLoadingMovers] = useState(false);
  const [loadingSuppliers, setLoadingSuppliers] = useState(false);
  const [loadingPurchases, setLoadingPurchases] = useState(false);

  // Print ref'leri
  const stockPrintRef = useRef(null);
  const moversPrintRef = useRef(null);
  const suppliersPrintRef = useRef(null);
  const purchasesPrintRef = useRef(null);

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
      setSelectedSupplier(null);
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

  // Tedarikçi filtresi uygulanmış veri
  const filteredSuppliers = selectedSupplier
    ? suppliers.filter((s) => s.supplierId === selectedSupplier)
    : suppliers;

  // Grafik için veri (ilk 10)
  const chartData = filteredSuppliers
    .slice(0, 10)
    .map((s) => ({
      name: s.supplierName,
      tutar: Number(s.totalAmount ?? 0),
    }));

  // Tedarikçi dropdown seçenekleri
  const supplierOptions = suppliers.map((s) => ({
    value: s.supplierId,
    label: s.supplierName,
  }));

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

  const dateToolbar = (onRefresh, printRef) => (
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
      <Button icon={<PrinterOutlined />} onClick={() => handlePrint(printRef)}>
        Yazdır
      </Button>
    </Space>
  );

  const tabItems = [
    {
      key: "stock",
      label: "Güncel stok",
      children: (
        <div ref={stockPrintRef}>
          <Space style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<ReloadOutlined />} onClick={loadStock}>
              Yenile
            </Button>
            <Button icon={<PrinterOutlined />} onClick={() => handlePrint(stockPrintRef)}>
              Yazdır
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
        <div ref={moversPrintRef}>
          {dateToolbar(loadMovers, moversPrintRef)}
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
        <div ref={suppliersPrintRef}>
          {/* Toolbar */}
          <Space wrap style={{ marginBottom: 16 }}>
            <span>Tarih aralığı:</span>
            <RangePicker
              showTime
              value={range}
              onChange={(v) => setRange(v || [])}
              format="DD.MM.YYYY HH:mm"
            />
            <Button icon={<ReloadOutlined />} onClick={loadSuppliers}>
              Yenile
            </Button>
            <Button icon={<PrinterOutlined />} onClick={() => handlePrint(suppliersPrintRef)}>
              Yazdır
            </Button>
          </Space>

          {/* Tedarikçi filtresi */}
          <Space wrap style={{ marginBottom: 16 }}>
            <span>Tedarikçi filtresi:</span>
            <Select
              allowClear
              placeholder="Tüm tedarikçiler"
              style={{ width: 240 }}
              options={supplierOptions}
              value={selectedSupplier}
              onChange={(v) => setSelectedSupplier(v ?? null)}
            />
          </Space>

          {/* Grafik */}
          {chartData.length > 0 && (
            <div style={{ marginBottom: 24 }}>
              <Title level={5} style={{ marginBottom: 8 }}>
                Tedarikçi Bazlı Harcama Grafiği
              </Title>
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={chartData} margin={{ top: 8, right: 24, left: 16, bottom: 48 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis
                    dataKey="name"
                    angle={-30}
                    textAnchor="end"
                    interval={0}
                    tick={{ fontSize: 12 }}
                  />
                  <YAxis
                    tickFormatter={(v) =>
                      v >= 1000 ? `${(v / 1000).toFixed(0)}K` : v
                    }
                    tick={{ fontSize: 12 }}
                  />
                  <Tooltip
                    formatter={(value) =>
                      value.toLocaleString("tr-TR", {
                        minimumFractionDigits: 2,
                        maximumFractionDigits: 2,
                      })
                    }
                  />
                  <Bar dataKey="tutar" name="Toplam Tutar (₺)" radius={[4, 4, 0, 0]}>
                    {chartData.map((_, index) => (
                      <Cell key={index} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}

          {/* Tablo */}
          <Table
            rowKey="supplierId"
            loading={loadingSuppliers}
            columns={supplierColumns}
            dataSource={filteredSuppliers}
            pagination={{ pageSize: 15 }}
          />
        </div>
      ),
    },
    {
      key: "purchases",
      label: "Satın alma özeti",
      children: (
        <div ref={purchasesPrintRef}>
          {dateToolbar(loadPurchases, purchasesPrintRef)}
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
