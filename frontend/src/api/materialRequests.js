import apiClient from "./client";

export const materialRequestsApi = {
  list: (params = {}) =>
    apiClient
      .get("/material-requests", { params })
      .then((r) => r.data),

  get: (id) =>
    apiClient.get(`/material-requests/${id}`).then((r) => r.data),

  create: (payload) =>
    apiClient.post("/material-requests", payload).then((r) => r.data),

  approve: (id, decisionNote) =>
    apiClient
      .patch(`/material-requests/${id}/approve`, { decisionNote })
      .then((r) => r.data),

  reject: (id, decisionNote) =>
    apiClient
      .patch(`/material-requests/${id}/reject`, { decisionNote })
      .then((r) => r.data),
};
