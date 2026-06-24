"use client";

import { createApiClient, type Cart } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function CartPage() {
  const [cart, setCart] = useState<Cart | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  async function loadCart() {
    const token = getToken();
    if (!token) {
      setError("Login to view your cart.");
      setIsLoading(false);
      return;
    }

    try {
      setCart(await createApiClient().cart.get(token));
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load cart");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadCart();
  }, []);

  async function updateItem(itemId: number, quantity: number) {
    const token = getToken();
    if (!token) return;
    setCart(await createApiClient().cart.updateItem(token, itemId, { quantity }));
  }

  async function removeItem(itemId: number) {
    const token = getToken();
    if (!token) return;
    await createApiClient().cart.removeItem(token, itemId);
    await loadCart();
  }

  return (
    <div className="mx-auto max-w-6xl px-5 py-12">
      <h1 className="text-4xl font-black">Cart</h1>
      {isLoading && <p className="mt-6 text-stone-600">Loading cart...</p>}
      {error && <p className="mt-6 rounded-2xl bg-amber-50 p-4 font-bold text-amber-800">{error} <Link href="/login" className="underline">Login</Link></p>}
      {cart && (
        <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_340px]">
          <div className="space-y-4">
            {cart.items.length === 0 && <div className="rounded-3xl bg-white p-8 shadow-sm">Your cart is empty.</div>}
            {cart.items.map((item) => (
              <div key={item.id} className="grid gap-4 rounded-3xl bg-white p-5 shadow-sm md:grid-cols-[1fr_140px_130px] md:items-center">
                <div>
                  <h2 className="text-lg font-black">{item.productName}</h2>
                  <p className="mt-1 text-sm text-stone-500">{formatCurrency(item.unitPrice)} each</p>
                </div>
                <input
                  type="number"
                  min={1}
                  value={item.quantity}
                  onChange={(event) => void updateItem(item.id, Math.max(1, Number(event.target.value)))}
                  className="rounded-2xl border border-stone-200 px-4 py-3 font-bold"
                />
                <div className="text-left md:text-right">
                  <p className="font-black text-amber-700">{formatCurrency(item.lineTotal)}</p>
                  <button onClick={() => void removeItem(item.id)} className="mt-2 text-sm font-bold text-red-700">Remove</button>
                </div>
              </div>
            ))}
          </div>
          <aside className="h-fit rounded-3xl bg-stone-900 p-6 text-white shadow-xl">
            <p className="text-sm uppercase tracking-[0.24em] text-amber-300">Summary</p>
            <p className="mt-4 text-3xl font-black">{formatCurrency(cart.subtotal)}</p>
            <Link href="/checkout" className="mt-6 block rounded-2xl bg-amber-500 px-5 py-3 text-center font-black text-stone-950">
              Checkout
            </Link>
          </aside>
        </div>
      )}
    </div>
  );
}
