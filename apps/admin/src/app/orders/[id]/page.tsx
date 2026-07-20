"use client";

import { createApiClient, type Order } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { AdminAuthGuard } from "@/components/admin-auth-guard";
import { getAdminToken } from "@/lib/session";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

const ORDER_STATUSES = ["PENDING_PAYMENT", "PAID", "PROCESSING", "SHIPPED", "COMPLETED", "CANCELLED"];

export default function AdminOrderDetailPage() {
  return (
    <AdminAuthGuard>
      <AdminOrderDetailContent />
    </AdminAuthGuard>
  );
}

function AdminOrderDetailContent() {
  const params = useParams<{ id: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  async function loadOrder() {
    const token = getAdminToken();
    if (!token) {
      setError("Login as admin first.");
      return;
    }
    setOrder(await createApiClient().admin.orders.get(token, Number(params.id)));
    setError(null);
  }

  useEffect(() => {
    loadOrder().catch((err) => setError(err instanceof Error ? err.message : "Failed to load order"));
  }, [params.id]);

  async function updateStatus(formData: FormData) {
    const token = getAdminToken();
    if (!token || !order) return;
    setIsSaving(true);
    setError(null);
    try {
      setOrder(await createApiClient().admin.orders.updateStatus(token, order.id, String(formData.get("status"))));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update status");
    } finally {
      setIsSaving(false);
    }
  }

  async function cancelOrder(formData: FormData) {
    const token = getAdminToken();
    if (!token || !order) return;
    if (!confirm("Cancel this order?")) return;
    setIsSaving(true);
    setError(null);
    try {
      setOrder(await createApiClient().admin.orders.cancel(token, order.id, String(formData.get("reason"))));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to cancel order");
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <div>
      <Link href="/orders" className="text-sm font-black text-cyan-300">Back to orders</Link>
      {error && <p className="mt-5 rounded-2xl bg-red-950 p-4 font-bold text-red-200">{error}</p>}
      {!order && !error && <p className="mt-6 text-slate-400">Loading order...</p>}
      {order && (
        <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_360px]">
          <section className="rounded-3xl border border-slate-800 bg-slate-950 p-6">
            <h1 className="text-4xl font-black">Order #{order.id}</h1>
            <p className="mt-2 text-slate-400">{new Date(order.createdAt).toLocaleString()}</p>
            <p className="mt-4 text-sm text-slate-300">
              <span className="font-bold">Shipping address:</span> {order.shippingAddress}
            </p>
            {order.notes && (
              <p className="mt-2 text-sm text-slate-300">
                <span className="font-bold">Notes:</span> {order.notes}
              </p>
            )}
            <div className="mt-6 space-y-4">
              {order.items.map((item) => (
                <div key={`${item.productId}-${item.productName}`} className="flex justify-between gap-4 border-b border-slate-800 pb-4">
                  <div>
                    <p className="font-black text-white">{item.productName}</p>
                    <p className="text-sm text-slate-400">{item.quantity} x {formatCurrency(item.unitPrice)}</p>
                  </div>
                  <p className="font-black text-cyan-300">{formatCurrency(item.lineTotal)}</p>
                </div>
              ))}
            </div>
          </section>

          <aside className="flex h-fit flex-col gap-5 rounded-3xl border border-slate-800 bg-slate-950 p-6">
            <div>
              <p className="text-sm uppercase tracking-[0.24em] text-cyan-300">Status</p>
              <p className="mt-2 text-2xl font-black text-white">{order.status}</p>
              <div className="mt-3 space-y-1 text-sm text-slate-300">
                <p>Payment: {order.paymentStatus}</p>
                <p>Method: {order.paymentMethod}</p>
                <p>Shipping: {order.shippingServiceName ?? order.shippingServiceCode ?? "-"}</p>
              </div>
            </div>

            <div className="border-t border-slate-800 pt-5">
              <p className="flex justify-between text-sm text-slate-300"><span>Subtotal</span><b>{formatCurrency(order.subtotal)}</b></p>
              <p className="mt-2 flex justify-between text-sm text-slate-300"><span>Shipping</span><b>{formatCurrency(order.shippingFee)}</b></p>
              <p className="mt-4 flex justify-between text-xl font-black text-white"><span>Total</span><b>{formatCurrency(order.total)}</b></p>
            </div>

            <form action={updateStatus} className="border-t border-slate-800 pt-5">
              <label className="block text-sm font-bold text-slate-300">Update status</label>
              <select name="status" defaultValue={order.status} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white">
                {ORDER_STATUSES.map((status) => <option key={status} value={status}>{status}</option>)}
              </select>
              <button disabled={isSaving} className="mt-3 w-full rounded-2xl bg-cyan-400 px-5 py-3 font-black text-slate-950 disabled:opacity-60">Save status</button>
            </form>

            <form action={cancelOrder} className="border-t border-slate-800 pt-5">
              <label className="block text-sm font-bold text-slate-300">Cancel order</label>
              <input name="reason" required placeholder="Reason" className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
              <button disabled={isSaving} className="mt-3 w-full rounded-2xl bg-red-500 px-5 py-3 font-black text-white disabled:opacity-60">Cancel order</button>
            </form>
          </aside>
        </div>
      )}
    </div>
  );
}
