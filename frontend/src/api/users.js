import apiClient from "./client";

export const usersApi = {
  list: () => apiClient.get("/users").then((r) => r.data),

  get: (id) => apiClient.get(`/users/${id}`).then((r) => r.data),

  create: (payload) => apiClient.post("/auth/register", payload).then((r) => r.data),

  update: (id, payload) => apiClient.put(`/users/${id}`, payload).then((r) => r.data),

  updateRole: (id, roleName) =>
    apiClient.patch(`/users/${id}/role`, { roleName }).then((r) => r.data),

  activate: (id) => apiClient.patch(`/users/${id}/activate`),

  deactivate: (id) => apiClient.patch(`/users/${id}/deactivate`),
};
