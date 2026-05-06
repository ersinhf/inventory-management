import apiClient from "./client";

export const stockMovementsApi = {
  filter: (params = {}) =>
    apiClient
      .get("/stock-movements", { params })
      .then((r) => r.data),

  getByProduct: (productId) =>
    apiClient
      .get(`/stock-movements/product/${productId}`)
      .then((r) => r.data),

  create: (payload) =>
    apiClient.post("/stock-movements", payload).then((r) => r.data),

  cancel: (id) =>
    apiClient.patch(`/stock-movements/${id}/cancel`).then((r) => r.data),
};