import apiClient from "./client";

export const reportsApi = {
  currentStock: () => apiClient.get("/reports/current-stock").then((r) => r.data),

  topMovers: (params) =>
    apiClient.get("/reports/top-movers", { params }).then((r) => r.data),

  supplierTotals: (params) =>
    apiClient.get("/reports/supplier-totals", { params }).then((r) => r.data),

  purchaseSummary: (params) =>
    apiClient.get("/reports/purchase-summary", { params }).then((r) => r.data),
};
