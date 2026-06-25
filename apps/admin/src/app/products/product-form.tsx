"use client";

import { createApiClient, type Category, type Product, type ProductInput } from "@mgmz/api-client";
import { getAdminToken } from "@/lib/session";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

type Props = {
  product?: Product;
};

export function ProductForm({ product }: Props) {
  const router = useRouter();
  const [categories, setCategories] = useState<Category[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    const token = getAdminToken();
    if (!token) {
      setError("Login as admin first.");
      return;
    }
    createApiClient().admin.categories.list(token).then(setCategories).catch((err) => setError(err instanceof Error ? err.message : "Failed to load categories"));
  }, []);

  async function save(formData: FormData) {
    const token = getAdminToken();
    if (!token) {
      setError("Login as admin first.");
      return;
    }

    const categoryId = String(formData.get("categoryId") ?? "");
    const imageUrls = String(formData.get("imageUrls") ?? "")
      .split("\n")
      .map((url) => url.trim())
      .filter(Boolean);
    const body: ProductInput = {
      name: String(formData.get("name")),
      slug: String(formData.get("slug") || ""),
      sku: String(formData.get("sku")),
      description: String(formData.get("description") || ""),
      price: String(formData.get("price")),
      stock: Number(formData.get("stock")),
      weightGram: Number(formData.get("weightGram")),
      lengthCm: Number(formData.get("lengthCm")),
      widthCm: Number(formData.get("widthCm")),
      heightCm: Number(formData.get("heightCm")),
      shippingCategory: String(formData.get("shippingCategory")),
      imageUrls,
      categoryId: categoryId ? Number(categoryId) : null,
      status: String(formData.get("status"))
    };

    setIsSaving(true);
    setError(null);
    try {
      if (product) {
        await createApiClient().admin.products.update(token, product.id, body);
      } else {
        await createApiClient().admin.products.create(token, body);
      }
      router.push("/products");
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to save product");
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form action={save} className="max-w-4xl rounded-3xl border border-slate-800 bg-slate-950 p-6">
      {error && <p className="mb-5 rounded-2xl bg-red-950 p-4 font-bold text-red-200">{error}</p>}
      <div className="grid gap-4 md:grid-cols-2">
        <Field label="Name" name="name" defaultValue={product?.name} required />
        <Field label="Slug optional" name="slug" defaultValue={product?.slug} />
        <Field label="SKU" name="sku" defaultValue={product?.sku} required />
        <Field label="Price" name="price" type="number" step="0.01" min="0.01" defaultValue={product?.price} required />
        <Field label="Stock" name="stock" type="number" min="0" defaultValue={product?.stock ?? 0} required />
        <Field label="Weight gram" name="weightGram" type="number" min="0" defaultValue={product?.weightGram ?? 0} required />
        <Field label="Length cm" name="lengthCm" type="number" min="0" defaultValue={product?.lengthCm ?? 0} required />
        <Field label="Width cm" name="widthCm" type="number" min="0" defaultValue={product?.widthCm ?? 0} required />
        <Field label="Height cm" name="heightCm" type="number" min="0" defaultValue={product?.heightCm ?? 0} required />
        <label className="text-sm font-bold text-slate-300">
          Category
          <select name="categoryId" defaultValue={product?.categoryId ?? ""} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white">
            <option value="">No category</option>
            {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
          </select>
        </label>
        <label className="text-sm font-bold text-slate-300">
          Shipping category
          <select name="shippingCategory" defaultValue={product?.shippingCategory ?? "others"} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white">
            <option value="others">Others</option>
            <option value="fashion">Fashion</option>
            <option value="healthcare">Healthcare</option>
            <option value="food_and_drink">Food and drink</option>
            <option value="electronic">Electronic</option>
            <option value="beauty">Beauty</option>
            <option value="outdoor_gear">Outdoor gear</option>
            <option value="home_accessories">Home accessories</option>
            <option value="hobby">Hobby</option>
            <option value="collection">Collection</option>
            <option value="sparepart">Sparepart</option>
            <option value="groceries">Groceries</option>
            <option value="frozen_food">Frozen food</option>
          </select>
        </label>
        <label className="text-sm font-bold text-slate-300">
          Status
          <select name="status" defaultValue={product?.status ?? "ACTIVE"} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white">
            <option value="ACTIVE">Active</option>
            <option value="DRAFT">Draft</option>
            <option value="ARCHIVED">Archived</option>
          </select>
        </label>
      </div>
      <label className="mt-4 block text-sm font-bold text-slate-300">
        Description
        <textarea name="description" defaultValue={product?.description ?? ""} className="mt-2 min-h-32 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
      </label>
      <label className="mt-4 block text-sm font-bold text-slate-300">
        Image URLs, one per line
        <textarea name="imageUrls" defaultValue={product?.imageUrls.join("\n") ?? ""} className="mt-2 min-h-28 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
      </label>
      <button disabled={isSaving} className="mt-6 rounded-2xl bg-cyan-400 px-5 py-3 font-black text-slate-950 disabled:opacity-60">
        {isSaving ? "Saving..." : "Save product"}
      </button>
    </form>
  );
}

function Field({ label, ...props }: React.InputHTMLAttributes<HTMLInputElement> & { label: string }) {
  return (
    <label className="text-sm font-bold text-slate-300">
      {label}
      <input {...props} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
    </label>
  );
}
