"use client";

import { createApiClient } from "@mgmz/api-client";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useState } from "react";

export function AddToCartButton({ productId }: { productId: number }) {
  const [quantity, setQuantity] = useState(1);
  const [message, setMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  async function addToCart() {
    const token = getToken();
    if (!token) {
      setMessage("Login first to add products to cart.");
      return;
    }

    setIsLoading(true);
    setMessage(null);
    try {
      await createApiClient().cart.addItem(token, { productId, quantity });
      setMessage("Added to cart.");
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "Failed to add to cart");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="mt-8">
      <div className="flex max-w-xs gap-3">
        <input
          type="number"
          min={1}
          value={quantity}
          onChange={(event) => setQuantity(Math.max(1, Number(event.target.value)))}
          className="w-24 rounded-full border border-stone-200 bg-white px-4 py-3 font-bold"
        />
        <button onClick={addToCart} disabled={isLoading} className="flex-1 rounded-full bg-stone-900 px-7 py-4 font-black text-white disabled:opacity-60">
          {isLoading ? "Adding..." : "Add to cart"}
        </button>
      </div>
      {message && <p className="mt-4 text-sm font-bold text-stone-700">{message} <Link href="/cart" className="text-amber-700">View cart</Link></p>}
    </div>
  );
}
