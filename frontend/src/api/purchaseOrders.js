import apiClient from "./client";

export const purchaseOrdersApi = {
  list: (status) =>
    apiClient
      .get("/purchase-orders", { params: status ? { status } : {} })
      .then((r) => r.data),

  get: (id) =>
    apiClient.get(`/purchase-orders/${id}`).then((r) => r.data),

  create: (payload) =>
    apiClient.post("/purchase-orders", payload).then((r) => r.data),

  update: (id, payload) =>
    apiClient.put(`/purchase-orders/${id}`, payload).then((r) => r.data),

  send: (id) =>
    apiClient.patch(`/purchase-orders/${id}/send`).then((r) => r.data),

  receive: (id) =>
    apiClient.patch(`/purchase-orders/${id}/receive`).then((r) => r.data),

  cancel: (id) =>
    apiClient.patch(`/purchase-orders/${id}/cancel`).then((r) => r.data),
};
