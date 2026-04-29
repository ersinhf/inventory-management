import apiClient from "./client";

export const suppliersApi = {
  list: (activeOnly = false) =>
    apiClient.get("/suppliers", { params: { activeOnly } }).then((r) => r.data),

  get: (id) => apiClient.get(`/suppliers/${id}`).then((r) => r.data),

  create: (payload) => apiClient.post("/suppliers", payload).then((r) => r.data),

  update: (id, payload) =>
    apiClient.put(`/suppliers/${id}`, payload).then((r) => r.data),

  activate: (id) => apiClient.patch(`/suppliers/${id}/activate`),

  deactivate: (id) => apiClient.patch(`/suppliers/${id}/deactivate`),
};
