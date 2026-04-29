import apiClient from "./client";

export const productsApi = {
  list: (activeOnly = false) =>
    apiClient.get("/products", { params: { activeOnly } }).then((r) => r.data),

  get: (id) => apiClient.get(`/products/${id}`).then((r) => r.data),

  getByBarcode: (barcode) =>
    apiClient.get(`/products/barcode/${barcode}`).then((r) => r.data),

  lowStock: () => apiClient.get("/products/low-stock").then((r) => r.data),

  create: (payload) => apiClient.post("/products", payload).then((r) => r.data),

  update: (id, payload) =>
    apiClient.put(`/products/${id}`, payload).then((r) => r.data),

  activate: (id) => apiClient.patch(`/products/${id}/activate`),

  deactivate: (id) => apiClient.patch(`/products/${id}/deactivate`),
};
