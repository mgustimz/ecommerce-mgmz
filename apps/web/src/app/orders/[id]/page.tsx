"use client";

import { createApiClient, type Order } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function OrderDetailPage() {
  const params = useParams<{ id: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function loadOrder() {
    const token = getToken();
    if (!token) {
      setError("Login to view this order.");
      return;
    }
    setOrder(await createApiClient().orders.get(token, Number(params.id)));
  }

  useEffect(() => {
    loadOrder().catch((err) => setError(err instanceof Error ? err.message : "Failed to load order"));
  }, [params.id]);

  async function cancelOrder(formData: FormData) {
    const token = getToken();
    if (!token || !order) return;
    try {
      setOrder(await createApiClient().orders.cancel(token, order.id, String(formData.get("reason"))));
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to cancel order");
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-5 py-12">
      <Link href="/orders" className="text-sm font-black text-amber-700">Back to orders</Link>
      {error && <p className="mt-6 rounded-2xl bg-red-50 p-4 font-bold text-red-700">{error}</p>}
      {!order && !error && <p className="mt-6 text-stone-600">Loading order...</p>}
      {order && (
        <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_340px]">
          <section className="rounded-3xl bg-white p-6 shadow-sm">
            <h1 className="text-4xl font-black">Order #{order.id}</h1>
            <p className="mt-2 text-stone-500">{new Date(order.createdAt).toLocaleString()}</p>
            <div className="mt-6 space-y-4">
              {order.items.map((item) => (
                <div key={`${item.productId}-${item.productName}`} className="flex justify-between gap-4 border-b border-stone-100 pb-4">
                  <div>
                    <p className="font-black">{item.productName}</p>
                    <p className="text-sm text-stone-500">{item.quantity} x {formatCurrency(item.unitPrice)}</p>
                  </div>
                  <p className="font-black text-amber-700">{formatCurrency(item.lineTotal)}</p>
                </div>
              ))}
            </div>
          </section>
          <aside className="h-fit rounded-3xl bg-stone-900 p-6 text-white shadow-xl">
            <p className="text-sm uppercase tracking-[0.24em] text-amber-300">Status</p>
            <p className="mt-3 text-2xl font-black">{order.status}</p>
            <div className="mt-6 space-y-2 text-sm text-stone-300">
              <p>Payment: {order.paymentStatus}</p>
              <p>Method: {order.paymentMethod}</p>
              <p>Shipping: {order.shippingServiceName ?? order.shippingServiceCode}</p>
            </div>
            <div className="mt-6 border-t border-stone-700 pt-6">
              <p className="flex justify-between"><span>Subtotal</span><b>{formatCurrency(order.subtotal)}</b></p>
              <p className="mt-2 flex justify-between"><span>Shipping</span><b>{formatCurrency(order.shippingFee)}</b></p>
              <p className="mt-4 flex justify-between text-xl"><span>Total</span><b>{formatCurrency(order.total)}</b></p>
            </div>
            <form action={cancelOrder} className="mt-6">
              <input name="reason" required placeholder="Cancel reason" className="w-full rounded-2xl border border-stone-700 bg-stone-800 px-4 py-3 text-white" />
              <button className="mt-3 w-full rounded-2xl bg-red-500 px-5 py-3 font-black text-white">Cancel order</button>
            </form>
          </aside>
        </div>
      )}
    </div>
  );
}
