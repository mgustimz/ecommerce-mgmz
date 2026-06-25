"use client";

import { createApiClient, type Category } from "@mgmz/api-client";
import { getAdminToken } from "@/lib/session";
import { useEffect, useState } from "react";

export default function CategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [editing, setEditing] = useState<Category | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function loadCategories() {
    const token = getAdminToken();
    if (!token) {
      setError("Login as admin to manage categories.");
      return;
    }
    setCategories(await createApiClient().admin.categories.list(token));
    setError(null);
  }

  useEffect(() => {
    loadCategories().catch((err) => setError(err instanceof Error ? err.message : "Failed to load categories"));
  }, []);

  async function saveCategory(formData: FormData) {
    const token = getAdminToken();
    if (!token) return;

    const body = {
      name: String(formData.get("name")),
      description: String(formData.get("description") || "")
    };

    try {
      if (editing) {
        await createApiClient().admin.categories.update(token, editing.id, body);
      } else {
        await createApiClient().admin.categories.create(token, body);
      }
      setEditing(null);
      await loadCategories();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to save category");
    }
  }

  async function deleteCategory(id: number) {
    const token = getAdminToken();
    if (!token || !confirm("Delete this category?")) return;
    try {
      await createApiClient().admin.categories.delete(token, id);
      await loadCategories();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete category");
    }
  }

  return (
    <div className="grid gap-8 lg:grid-cols-[1fr_380px]">
      <section>
        <h1 className="text-4xl font-black">Categories</h1>
        {error && <p className="mt-5 rounded-2xl bg-red-950 p-4 font-bold text-red-200">{error}</p>}
        <div className="mt-6 grid gap-4 md:grid-cols-2">
          {categories.map((category) => (
            <div key={category.id} className="rounded-3xl border border-slate-800 bg-slate-950 p-5">
              <h2 className="text-xl font-black text-white">{category.name}</h2>
              <p className="mt-2 min-h-10 text-slate-400">{category.description ?? "No description"}</p>
              <div className="mt-4 flex gap-3 text-sm font-bold">
                <button onClick={() => setEditing(category)} className="text-cyan-300">Edit</button>
                <button onClick={() => void deleteCategory(category.id)} className="text-red-300">Delete</button>
              </div>
            </div>
          ))}
        </div>
      </section>

      <form key={editing?.id ?? "new"} action={saveCategory} className="h-fit rounded-3xl border border-slate-800 bg-slate-950 p-6">
        <h2 className="text-2xl font-black">{editing ? "Edit category" : "New category"}</h2>
        <label className="mt-5 block text-sm font-bold text-slate-300">
          Name
          <input name="name" required defaultValue={editing?.name} className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
        </label>
        <label className="mt-4 block text-sm font-bold text-slate-300">
          Description
          <textarea name="description" defaultValue={editing?.description ?? ""} className="mt-2 min-h-28 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
        </label>
        <div className="mt-5 flex gap-3">
          <button className="rounded-2xl bg-cyan-400 px-5 py-3 font-black text-slate-950">Save</button>
          {editing && <button type="button" onClick={() => setEditing(null)} className="rounded-2xl border border-slate-700 px-5 py-3 font-black text-white">Cancel</button>}
        </div>
      </form>
    </div>
  );
}
