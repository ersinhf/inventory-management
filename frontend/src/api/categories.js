import apiClient from "./client";

export const categoriesApi = {
  list: () => apiClient.get("/categories").then((r) => r.data),

  create: (payload) => apiClient.post("/categories", payload).then((r) => r.data),

  update: (id, payload) =>
    apiClient.put(`/categories/${id}`, payload).then((r) => r.data),

  delete: (id) => apiClient.delete(`/categories/${id}`),
};
