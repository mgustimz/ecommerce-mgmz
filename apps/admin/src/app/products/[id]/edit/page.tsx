"use client";

import { createApiClient, type Product } from "@mgmz/api-client";
import { getAdminToken } from "@/lib/session";
import { useParams } from "next/navigation";
import { useEffect, useState } from "react";
import { ProductForm } from "../../product-form";

export default function EditProductPage() {
  const params = useParams<{ id: string }>();
  const [product, setProduct] = useState<Product | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = getAdminToken();
    if (!token) {
      setError("Login as admin first.");
      return;
    }
    createApiClient().admin.products.get(token, Number(params.id)).then(setProduct).catch((err) => setError(err instanceof Error ? err.message : "Failed to load product"));
  }, [params.id]);

  return (
    <div>
      <h1 className="mb-6 text-4xl font-black">Edit product</h1>
      {error && <p className="rounded-2xl bg-red-950 p-4 font-bold text-red-200">{error}</p>}
      {product ? <ProductForm product={product} /> : !error && <p className="text-slate-400">Loading product...</p>}
    </div>
  );
}
