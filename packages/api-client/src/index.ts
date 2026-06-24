import type { AuthSession, UserRole } from "@mgmz/shared";

export type PageResponse<T> = {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
};

export type Category = {
  id: number;
  name: string;
  description: string | null;
};

export type Product = {
  id: number;
  name: string;
  slug: string;
  sku: string;
  description: string | null;
  price: string | number;
  stock: number;
  weightGram: number;
  lengthCm: number;
  widthCm: number;
  heightCm: number;
  shippingCategory: string;
  status: string;
  imageUrls: string[];
  categoryId: number | null;
  categoryName: string | null;
};

export type CartItem = {
  id: number;
  productId: number;
  productName: string;
  unitPrice: string | number;
  quantity: number;
  lineTotal: string | number;
};

export type Cart = {
  customerId: number;
  items: CartItem[];
  subtotal: string | number;
};

export type Address = {
  id: number;
  label: string;
  recipientName: string;
  phone: string;
  street: string;
  city: string;
  province: string;
  postalCode: string;
  areaId: string | null;
  latitude: string | number | null;
  longitude: string | number | null;
  defaultAddress: boolean;
};

export type AddressInput = {
  label: string;
  recipientName: string;
  phone: string;
  street: string;
  city: string;
  province: string;
  postalCode: string;
  areaId?: string | null;
  latitude?: string | number | null;
  longitude?: string | number | null;
  defaultAddress: boolean;
};

export type OrderItem = {
  productId: number;
  productName: string;
  unitPrice: string | number;
  quantity: number;
  lineTotal: string | number;
};

export type Order = {
  id: number;
  customerId: number;
  subtotal: string | number;
  shippingFee: string | number;
  total: string | number;
  shippingAddress: string;
  notes: string | null;
  cancellationReason: string | null;
  cancelledAt: string | null;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  paymentReference: string | null;
  paymentExpiresAt: string | null;
  shippingServiceCode: string | null;
  shippingServiceName: string | null;
  createdAt: string;
  items: OrderItem[];
};

export type DashboardSummary = {
  totalOrders: number;
  pendingPaymentOrders: number;
  paidOrders: number;
  processingOrders: number;
  shippedOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  grossRevenue: string | number;
  paidRevenue: string | number;
  lowStockProducts: number;
  recentOrders: unknown[];
};

type RequestOptions = Omit<RequestInit, "body"> & {
  token?: string | null;
  body?: unknown;
  query?: Record<string, string | number | boolean | null | undefined>;
};

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = "ApiError";
  }
}

export function createApiClient(baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api") {
  async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const url = new URL(`${baseUrl}${path}`);

    for (const [key, value] of Object.entries(options.query ?? {})) {
      if (value !== null && value !== undefined && value !== "") {
        url.searchParams.set(key, String(value));
      }
    }

    const response = await fetch(url, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
        ...options.headers
      },
      body: options.body === undefined ? undefined : JSON.stringify(options.body)
    });

    if (!response.ok) {
      const fallback = `Request failed with status ${response.status}`;
      const text = await response.text();
      let message = text || fallback;

      try {
        const parsed = JSON.parse(text) as { message?: string; error?: string };
        message = parsed.message ?? parsed.error ?? fallback;
      } catch {
        // Keep plain text backend errors readable.
      }

      throw new ApiError(response.status, message);
    }

    if (response.status === 204) {
      return undefined as T;
    }

    return response.json() as Promise<T>;
  }

  return {
    auth: {
      login: (body: { email: string; password: string }) => request<AuthSession>("/auth/login", { method: "POST", body }),
      register: (body: { name: string; email: string; password: string }) => request<AuthSession>("/auth/register", { method: "POST", body })
    },
    categories: {
      list: () => request<Category[]>("/categories")
    },
    products: {
      list: (query?: { q?: string; categoryId?: number; sort?: string; page?: number; size?: number }) =>
        request<PageResponse<Product>>("/products", { query }),
      getBySlug: (slug: string) => request<Product>(`/products/slug/${slug}`)
    },
    cart: {
      get: (token: string) => request<Cart>("/cart", { token }),
      addItem: (token: string, body: { productId: number; quantity: number }) =>
        request<Cart>("/cart/items", { method: "POST", token, body }),
      updateItem: (token: string, itemId: number, body: { quantity: number }) =>
        request<Cart>(`/cart/items/${itemId}`, { method: "PUT", token, body }),
      removeItem: (token: string, itemId: number) => request<void>(`/cart/items/${itemId}`, { method: "DELETE", token })
    },
    addresses: {
      list: (token: string) => request<Address[]>("/me/addresses", { token }),
      create: (token: string, body: AddressInput) => request<Address>("/me/addresses", { method: "POST", token, body }),
      update: (token: string, id: number, body: AddressInput) => request<Address>(`/me/addresses/${id}`, { method: "PUT", token, body }),
      setDefault: (token: string, id: number) => request<Address>(`/me/addresses/${id}/default`, { method: "PUT", token }),
      delete: (token: string, id: number) => request<void>(`/me/addresses/${id}`, { method: "DELETE", token })
    },
    orders: {
      listMine: (token: string) => request<Order[]>("/orders", { token }),
      get: (token: string, id: number) => request<Order>(`/orders/${id}`, { token }),
      checkout: (token: string, body: { addressId: number; shippingServiceCode: string; paymentMethod: string; notes?: string }) =>
        request<Order>("/orders/checkout", { method: "POST", token, body }),
      cancel: (token: string, id: number, reason: string) => request<Order>(`/orders/${id}/cancel`, { method: "POST", token, body: { reason } })
    },
    admin: {
      dashboardSummary: (token: string) => request<DashboardSummary>("/admin/dashboard/summary", { token }),
      products: {
        create: (token: string, body: unknown) => request<Product>("/admin/products", { method: "POST", token, body }),
        update: (token: string, id: number, body: unknown) => request<Product>(`/admin/products/${id}`, { method: "PUT", token, body }),
        delete: (token: string, id: number) => request<void>(`/admin/products/${id}`, { method: "DELETE", token })
      },
      categories: {
        create: (token: string, body: unknown) => request<Category>("/admin/categories", { method: "POST", token, body }),
        update: (token: string, id: number, body: unknown) => request<Category>(`/admin/categories/${id}`, { method: "PUT", token, body }),
        delete: (token: string, id: number) => request<void>(`/admin/categories/${id}`, { method: "DELETE", token })
      },
      orders: {
        list: (token: string) => request<Order[]>("/admin/orders", { token }),
        updateStatus: (token: string, id: number, status: string) =>
          request<Order>(`/admin/orders/${id}/status`, { method: "PUT", token, body: { status } })
      }
    }
  };
}

export type { AuthSession, UserRole };
