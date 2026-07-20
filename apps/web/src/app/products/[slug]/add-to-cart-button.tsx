"use client";

import { createApiClient } from "@mgmz/api-client";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useState } from "react";

export function AddToCartButton({ productId, disabled }: { productId: number; disabled?: boolean }) {
  const [quantity, setQuantity] = useState(1);
  const [message, setMessage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  async function addToCart() {
    const token = getToken();
    if (!token) {
      setMessage("Please login to add products to cart.");
      return;
    }

    setIsLoading(true);
    setMessage(null);
    try {
      await createApiClient().cart.addItem(token, { productId, quantity });
      setMessage("Added to cart successfully.");
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "Failed to add to cart");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="mt-8">
      <div className="flex max-w-md gap-3">
        <div className="flex items-center rounded border border-neutral-300">
          <button
            type="button"
            onClick={() => setQuantity((q) => Math.max(1, q - 1))}
            className="px-3 py-2 text-lg font-bold text-neutral-500 hover:text-neutral-900"
            disabled={disabled}
          >
            -
          </button>
          <input
            type="number"
            min={1}
            value={quantity}
            onChange={(event) => setQuantity(Math.max(1, Number(event.target.value)))}
            className="w-16 border-x border-neutral-300 bg-white py-2 text-center font-bold focus:outline-none"
            disabled={disabled}
          />
          <button
            type="button"
            onClick={() => setQuantity((q) => q + 1)}
            className="px-3 py-2 text-lg font-bold text-neutral-500 hover:text-neutral-900"
            disabled={disabled}
          >
            +
          </button>
        </div>
        <button
          onClick={addToCart}
          disabled={isLoading || disabled}
          className="btn-primary flex-1 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {isLoading ? "Adding..." : disabled ? "Out of stock" : "Add to cart"}
        </button>
      </div>
      {message && (
        <p className="mt-3 text-sm text-neutral-700">
          {message}{" "}
          <Link href="/cart" className="font-bold" style={{ color: "#cc1d00" }}>
            View cart
          </Link>
        </p>
      )}
    </div>
  );
}
