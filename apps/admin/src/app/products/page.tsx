"use client";

import { createApiClient, type Product } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { getAdminToken } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function ProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [error, setError] = useState<string | null>(null);

  async function loadProducts() {
    const token = getAdminToken();
    if (!token) {
      setError("Login as admin to manage products.");
      return;
    }

    const response = await createApiClient().admin.products.list(token, { size: 100, sort: "NEWEST" });
    setProducts(response.items);
    setError(null);
  }

  useEffect(() => {
    loadProducts().catch((err) => setError(err instanceof Error ? err.message : "Failed to load products"));
  }, []);

  async function deleteProduct(id: number) {
    const token = getAdminToken();
    if (!token || !confirm("Archive this product?")) return;
    await createApiClient().admin.products.delete(token, id);
    await loadProducts();
  }

  return (
    <div>
      <div className="flex items-center justify-between gap-4">
        <h1 className="text-4xl font-black">Products</h1>
        <Link href="/products/new" className="rounded-2xl bg-cyan-400 px-4 py-3 font-black text-slate-950">New product</Link>
      </div>
      {error && <p className="mt-5 rounded-2xl bg-amber-400/10 p-4 font-bold text-amber-200">{error}</p>}
      <div className="mt-6 overflow-hidden rounded-3xl border border-slate-800 bg-slate-950">
        {products.map((product) => (
          <div key={product.id} className="grid gap-3 border-b border-slate-800 p-4 md:grid-cols-[1fr_150px_120px_170px] md:items-center">
            <div>
              <p className="font-black text-white">{product.name}</p>
              <p className="text-sm text-slate-400">{product.sku} · {product.status}</p>
            </div>
            <p className="font-bold text-cyan-300">{formatCurrency(product.price)}</p>
            <p className="text-sm text-slate-300">Stock {product.stock}</p>
            <div className="flex gap-3 text-sm font-bold">
              <Link href={`/products/${product.id}/edit`} className="text-cyan-300">Edit</Link>
              <button onClick={() => void deleteProduct(product.id)} className="text-red-300">Archive</button>
            </div>
          </div>
        ))}
        {products.length === 0 && !error && <p className="p-6 text-slate-400">No products yet.</p>}
      </div>
    </div>
  );
}
