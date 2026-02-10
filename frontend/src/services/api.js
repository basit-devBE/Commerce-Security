import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Enable sending cookies with requests
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Log detailed error information for debugging
    if (error.response?.data) {
      console.error('API Error:', {
        message: error.response.data.message,
        status: error.response.data.status,
        path: error.response.data.path,
        timestamp: error.response.data.timestamp,
      });
    }
    
    if (error.response?.status === 401) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth APIs
export const authAPI = {
  register: (data) => api.post('/users/public/register', data),
  login: (data) => api.post('/users/public/login', data),
  getProfile: () => api.get('/users/profile'),
  updateProfile: (data) => api.put('/users/updateProfile', data),
};

// Product APIs
export const productAPI = {
  getAll: (params) => api.get('/products/public/all', { params }),
  getById: (id) => api.get(`/products/${id}`),
  getByPriceRange: (minPrice, maxPrice, params) => 
    api.get('/products/public/price-range', { params: { minPrice, maxPrice, ...params } }),
  create: (data) => api.post('/products/admin/add', data),
  update: (id, data) => api.put(`/products/admin/update/${id}`, data),
  delete: (id) => api.delete(`/products/admin/${id}`),
};

// Category APIs
export const categoryAPI = {
  getAll: (params) => api.get('/categories/public/all', { params }),
  getById: (id) => api.get(`/categories/${id}`),
  create: (data) => api.post('/categories/admin/add', data),
  update: (id, data) => api.put(`/categories/admin/update/${id}`, data),
  delete: (id) => api.delete(`/categories/admin/${id}`),
};

// Order APIs
export const orderAPI = {
  create: (data) => api.post('/orders/create', data),
  getUserOrders: (params) => api.get('/orders/user', { params }),
  getAll: (params) => api.get('/orders/admin/all', { params }),
  getById: (id) => api.get(`/orders/${id}`),
  updateStatus: (id, data) => api.put(`/orders/admin/update/${id}`, data),
};

// Inventory APIs
export const inventoryAPI = {
  getAll: (params) => api.get('/inventory/admin/all', { params }),
  getById: (id) => api.get(`/inventory/${id}`),
  getByProductId: (productId) => api.get(`/inventory/product/${productId}`),
  create: (data) => api.post('/inventory/admin/add', data),
  update: (id, data) => api.put(`/inventory/admin/update/${id}`, data),
  adjustQuantity: (id, quantityChange) => api.patch(`/inventory/admin/adjust/${id}`, null, { params: { quantityChange } }),
  delete: (id) => api.delete(`/inventory/admin/${id}`),
};

// User Management APIs (Admin)
export const userAPI = {
  getAll: (params) => api.get('/users/admin/all', { params }),
  getById: (id) => api.get(`/users/${id}`),
  update: (id, data) => api.put(`/users/admin/update/${id}`, data),
  delete: (id) => api.delete(`/users/admin/${id}`),
};

// Cart APIs
export const cartAPI = {
  get: () => api.get('/cart'),
  addItem: (data) => api.post('/cart/add', data),
  updateItem: (productId, data) => api.put(`/cart/update/${productId}`, data),
  removeItem: (productId) => api.delete(`/cart/remove/${productId}`),
  clear: () => api.delete('/cart/clear'),
};

// Performance APIs
export const performanceAPI = {
  getDbMetrics: () => api.get('/performance/admin/db-metrics'),
  getCacheMetrics: () => api.get('/performance/admin/cache-metrics'),
  clearMetrics: () => api.delete('/performance/admin/clear-metrics'),
};

export default api;
