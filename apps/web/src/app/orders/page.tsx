"use client";

import { createApiClient, type Order } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { CustomerAuthGuard } from "@/components/customer-auth-guard";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function OrdersPage() {
  return (
    <CustomerAuthGuard>
      <OrdersContent />
    </CustomerAuthGuard>
  );
}

function OrdersContent() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      setIsLoading(false);
      return;
    }
    createApiClient().orders.listMine(token)
      .then(setOrders)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load orders"))
      .finally(() => setIsLoading(false));
  }, []);

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <nav className="mb-4 text-xs text-neutral-500">
        <Link href="/" className="hover:text-red-600">Home</Link> / <span>My orders</span>
      </nav>
      <h1 className="text-3xl font-black">My orders</h1>

      {isLoading && <p className="mt-6 text-sm text-neutral-500">Loading orders...</p>}
      {error && <p className="mt-6 rounded border border-red-200 bg-red-50 p-4 text-sm font-bold text-red-700">{error}</p>}

      <div className="mt-6 space-y-3">
        {orders.map((order) => (
          <Link key={order.id} href={`/orders/${order.id}`} className="grid gap-3 rounded-lg border border-neutral-200 bg-white p-4 transition hover:border-red-600 md:grid-cols-[1fr_140px_140px_140px] md:items-center">
            <div>
              <p className="text-sm font-black">Order #{order.id}</p>
              <p className="text-xs text-neutral-500">{new Date(order.createdAt).toLocaleString()}</p>
            </div>
            <p className="rounded-full bg-neutral-100 px-3 py-1 text-center text-xs font-black">{order.status}</p>
            <p className="rounded-full bg-neutral-100 px-3 py-1 text-center text-xs font-black">{order.paymentStatus}</p>
            <p className="text-right text-base font-black" style={{ color: "#cc1d00" }}>{formatCurrency(order.total)}</p>
          </Link>
        ))}
        {!isLoading && !error && orders.length === 0 && (
          <div className="rounded-lg border border-neutral-200 bg-white p-10 text-center">
            <p className="text-base font-bold">No orders yet</p>
            <p className="mt-1 text-sm text-neutral-500">Place your first order to see it here.</p>
            <Link href="/products" className="btn-primary mt-4 inline-flex">Shop products</Link>
          </div>
        )}
      </div>
    </div>
  );
}
