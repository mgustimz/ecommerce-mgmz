"use client";

import { createApiClient, type Order } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { CustomerAuthGuard } from "@/components/customer-auth-guard";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function OrderDetailPage() {
  return (
    <CustomerAuthGuard>
      <OrderDetailContent />
    </CustomerAuthGuard>
  );
}

function OrderDetailContent() {
  const params = useParams<{ id: string }>();
  const [order, setOrder] = useState<Order | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  async function loadOrder() {
    const token = getToken();
    if (!token) {
      setIsLoading(false);
      return;
    }
    setOrder(await createApiClient().orders.get(token, Number(params.id)));
  }

  useEffect(() => {
    loadOrder()
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load order"))
      .finally(() => setIsLoading(false));
  }, [params.id]);

  async function cancelOrder(formData: FormData) {
    const token = getToken();
    if (!token || !order) return;
    try {
      setOrder(await createApiClient().orders.cancel(token, order.id, String(formData.get("reason"))));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to cancel order");
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-5 py-8">
      <nav className="mb-4 text-xs text-neutral-500">
        <Link href="/" className="hover:text-red-600">Home</Link> / <Link href="/orders" className="hover:text-red-600">My orders</Link> / <span>Order #{params.id}</span>
      </nav>

      {isLoading && <p className="text-sm text-neutral-500">Loading order...</p>}
      {error && <p className="rounded border border-red-200 bg-red-50 p-4 text-sm font-bold text-red-700">{error}</p>}

      {order && (
        <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
          <section className="rounded-lg border border-neutral-200 bg-white p-6">
            <h1 className="text-2xl font-black">Order #{order.id}</h1>
            <p className="mt-1 text-sm text-neutral-500">{new Date(order.createdAt).toLocaleString()}</p>

            <div className="mt-6">
              <p className="text-sm font-bold text-neutral-700">Shipping address</p>
              <p className="mt-1 text-sm text-neutral-600">{order.shippingAddress}</p>
              {order.notes && (
                <p className="mt-2 text-sm text-neutral-500">Notes: {order.notes}</p>
              )}
            </div>

            <div className="mt-6 space-y-3">
              <h2 className="text-sm font-black uppercase tracking-wider text-neutral-500">Items</h2>
              {order.items.map((item) => (
                <div key={`${item.productId}-${item.productName}`} className="flex justify-between border-b border-neutral-100 pb-3">
                  <div>
                    <p className="font-bold">{item.productName}</p>
                    <p className="text-xs text-neutral-500">{item.quantity} x {formatCurrency(item.unitPrice)}</p>
                  </div>
                  <p className="font-black" style={{ color: "#cc1d00" }}>{formatCurrency(item.lineTotal)}</p>
                </div>
              ))}
            </div>
          </section>

          <aside className="h-fit space-y-5 rounded-lg border border-neutral-200 bg-white p-6">
            <div>
              <h2 className="text-sm font-black uppercase tracking-wider text-neutral-500">Status</h2>
              <p className="mt-1 text-2xl font-black">{order.status}</p>
              <div className="mt-2 space-y-1 text-sm text-neutral-600">
                <p>Payment: {order.paymentStatus}</p>
                <p>Method: {order.paymentMethod}</p>
                <p>Shipping: {order.shippingServiceName ?? order.shippingServiceCode}</p>
              </div>
            </div>

            <div className="border-t border-neutral-200 pt-4">
              <div className="flex justify-between text-sm text-neutral-600"><span>Subtotal</span><b>{formatCurrency(order.subtotal)}</b></div>
              {order.couponCode && Number(order.discountAmount) > 0 && (
                <div className="mt-1 flex justify-between text-sm text-emerald-700">
                  <span>Discount ({order.couponCode})</span>
                  <b>-{formatCurrency(order.discountAmount)}</b>
                </div>
              )}
              <div className="mt-1 flex justify-between text-sm text-neutral-600"><span>Shipping</span><b>{formatCurrency(order.shippingFee)}</b></div>
              <div className="mt-3 flex justify-between border-t border-neutral-200 pt-3 text-lg font-black"><span>Total</span><b style={{ color: "#cc1d00" }}>{formatCurrency(order.total)}</b></div>
            </div>

            <form action={cancelOrder} className="border-t border-neutral-200 pt-4">
              <label className="text-sm font-bold text-neutral-700">Cancel this order</label>
              <input name="reason" required placeholder="Reason" className="input-field mt-2" />
              <button type="submit" className="mt-2 w-full rounded border border-neutral-300 px-5 py-3 text-sm font-bold hover:border-red-600 hover:text-red-600">
                Cancel order
              </button>
            </form>
          </aside>
        </div>
      )}
    </div>
  );
}
