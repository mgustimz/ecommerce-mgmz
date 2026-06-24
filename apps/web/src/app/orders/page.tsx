"use client";

import { createApiClient, type Order } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      setError("Login to view orders.");
      return;
    }
    createApiClient().orders.listMine(token).then(setOrders).catch((err) => setError(err instanceof Error ? err.message : "Failed to load orders"));
  }, []);

  return (
    <div className="mx-auto max-w-6xl px-5 py-12">
      <h1 className="text-4xl font-black">My orders</h1>
      {error && <p className="mt-6 rounded-2xl bg-amber-50 p-4 font-bold text-amber-800">{error} <Link href="/login" className="underline">Login</Link></p>}
      <div className="mt-6 space-y-4">
        {orders.map((order) => (
          <Link key={order.id} href={`/orders/${order.id}`} className="grid gap-4 rounded-3xl bg-white p-5 shadow-sm md:grid-cols-[1fr_180px_160px] md:items-center">
            <div>
              <h2 className="text-xl font-black">Order #{order.id}</h2>
              <p className="mt-1 text-sm text-stone-500">{new Date(order.createdAt).toLocaleString()}</p>
            </div>
            <p className="font-black text-amber-700">{formatCurrency(order.total)}</p>
            <p className="rounded-full bg-stone-100 px-4 py-2 text-center text-sm font-black text-stone-700">{order.status}</p>
          </Link>
        ))}
        {!error && orders.length === 0 && <div className="rounded-3xl bg-white p-8 shadow-sm">No orders yet.</div>}
      </div>
    </div>
  );
}
