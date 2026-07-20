"use client";

import { createApiClient, type Order } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { AdminAuthGuard } from "@/components/admin-auth-guard";
import { forceAdminReLogin, getAdminToken } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function AdminOrdersPage() {
  return (
    <AdminAuthGuard>
      <AdminOrdersContent />
    </AdminAuthGuard>
  );
}

function AdminOrdersContent() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [error, setError] = useState<string | null>(null);

  async function loadOrders() {
    const token = getAdminToken();
    if (!token) {
      setError("Login as admin to view orders.");
      return;
    }
    setOrders(await createApiClient().admin.orders.list(token));
    setError(null);
  }

  useEffect(() => {
    loadOrders().catch((err) => {
      if (err instanceof Error && /status 401/.test(err.message)) {
        forceAdminReLogin();
        return;
      }
      setError(err instanceof Error ? err.message : "Failed to load orders");
    });
  }, []);

  return (
    <div>
      <h1 className="text-4xl font-black">Orders</h1>
      {error && <p className="mt-5 rounded-2xl bg-amber-400/10 p-4 font-bold text-amber-200">{error}</p>}
      <div className="mt-6 overflow-hidden rounded-3xl border border-slate-800 bg-slate-950">
        {orders.map((order) => (
          <Link key={order.id} href={`/orders/${order.id}`} className="grid gap-3 border-b border-slate-800 p-4 md:grid-cols-[120px_1fr_160px_180px_140px] md:items-center">
            <p className="font-black text-white">#{order.id}</p>
            <div>
              <p className="text-sm text-slate-400">{new Date(order.createdAt).toLocaleString()}</p>
              <p className="text-sm text-slate-400">Customer {order.customerId}</p>
            </div>
            <p className="rounded-full bg-slate-900 px-4 py-2 text-center text-xs font-black text-slate-200">{order.status}</p>
            <p className="rounded-full bg-slate-900 px-4 py-2 text-center text-xs font-black text-slate-200">{order.paymentStatus}</p>
            <p className="font-black text-cyan-300">{formatCurrency(order.total)}</p>
          </Link>
        ))}
        {orders.length === 0 && !error && <p className="p-6 text-slate-400">No orders yet.</p>}
      </div>
    </div>
  );
}
