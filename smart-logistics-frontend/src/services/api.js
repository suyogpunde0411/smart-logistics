import axios from "axios";

const getBaseUrl = () => {
  const envUrl = import.meta.env.VITE_API_BASE_URL;
  if (envUrl && !envUrl.includes("localhost")) return envUrl;
  const host = typeof window !== "undefined" && window.location ? window.location.hostname : "localhost";
  return `http://${host}:5000/api/v1`;
};

const api = axios.create({
  baseURL: getBaseUrl(),
});

api.interceptors.request.use((config) => {
  const host = typeof window !== "undefined" && window.location && window.location.hostname
    ? window.location.hostname
    : "localhost";
  config.baseURL = `http://${host}:5000/api/v1`;
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.clear();
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;