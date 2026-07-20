"use client";

import { createApiClient, type Cart } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { CustomerAuthGuard } from "@/components/customer-auth-guard";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function CartPage() {
  return (
    <CustomerAuthGuard>
      <CartContent />
    </CustomerAuthGuard>
  );
}

function CartContent() {
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
    <div className="mx-auto max-w-6xl px-5 py-8">
      <nav className="mb-4 text-xs text-neutral-500">
        <Link href="/" className="hover:text-red-600">Home</Link> / <span>Cart</span>
      </nav>
      <h1 className="text-3xl font-black">Your cart</h1>

      <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_360px]">
        <div className="space-y-3">
          {isLoading && <p className="text-sm text-neutral-500">Loading cart...</p>}
          {error && (
            <p className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm font-bold text-amber-800">
              {error} <Link href="/login" className="underline">Login</Link>
            </p>
          )}
          {cart && cart.items.length === 0 && (
            <div className="rounded-lg border border-neutral-200 bg-white p-10 text-center">
              <p className="text-base font-bold">Your cart is empty</p>
              <p className="mt-1 text-sm text-neutral-500">Browse our products to add items.</p>
              <Link href="/products" className="btn-primary mt-4 inline-flex">Shop products</Link>
            </div>
          )}
          {cart && cart.items.map((item) => (
            <div key={item.id} className="grid gap-4 rounded-lg border border-neutral-200 bg-white p-4 md:grid-cols-[120px_1fr_140px_140px] md:items-center">
              <div className="grid h-28 w-28 place-items-center rounded bg-neutral-50 text-xs font-bold uppercase text-neutral-300">
                No image
              </div>
              <div>
                <h2 className="text-base font-black">{item.productName}</h2>
                <p className="text-sm text-neutral-500">{formatCurrency(item.unitPrice)} each</p>
              </div>
              <div className="flex items-center rounded border border-neutral-300">
                <button
                  type="button"
                  onClick={() => void updateItem(item.id, Math.max(1, item.quantity - 1))}
                  className="px-3 py-2 text-lg font-bold text-neutral-500 hover:text-neutral-900"
                >
                  -
                </button>
                <input
                  type="number"
                  min={1}
                  value={item.quantity}
                  onChange={(event) => void updateItem(item.id, Math.max(1, Number(event.target.value)))}
                  className="w-16 border-x border-neutral-300 bg-white py-2 text-center font-bold focus:outline-none"
                />
                <button
                  type="button"
                  onClick={() => void updateItem(item.id, item.quantity + 1)}
                  className="px-3 py-2 text-lg font-bold text-neutral-500 hover:text-neutral-900"
                >
                  +
                </button>
              </div>
              <div className="text-right">
                <p className="text-base font-black" style={{ color: "#cc1d00" }}>{formatCurrency(item.lineTotal)}</p>
                <button onClick={() => void removeItem(item.id)} className="mt-2 text-xs font-bold text-neutral-500 hover:text-red-600">
                  Remove
                </button>
              </div>
            </div>
          ))}
        </div>

        {cart && cart.items.length > 0 && (
          <aside className="h-fit rounded-lg border border-neutral-200 bg-white p-6">
            <h2 className="text-base font-black">Order summary</h2>
            <div className="mt-4 space-y-2 text-sm">
              <div className="flex justify-between"><span>Subtotal</span><b>{formatCurrency(cart.subtotal)}</b></div>
              <p className="text-xs text-neutral-500">Shipping calculated at checkout.</p>
            </div>
            <Link href="/checkout" className="btn-primary mt-5 w-full">Proceed to checkout</Link>
            <Link href="/products" className="mt-3 block text-center text-sm font-bold hover:underline" style={{ color: "#cc1d00" }}>
              Continue shopping
            </Link>
          </aside>
        )}
      </div>
    </div>
  );
}
