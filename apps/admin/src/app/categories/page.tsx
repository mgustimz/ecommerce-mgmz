import { createApiClient } from "@mgmz/api-client";

export default async function CategoriesPage() {
  const categories = await createApiClient().categories.list().catch(() => []);

  return (
    <div>
      <h1 className="text-4xl font-black">Categories</h1>
      <div className="mt-6 grid gap-4 md:grid-cols-2">
        {categories.map((category) => (
          <div key={category.id} className="rounded-3xl border border-slate-800 bg-slate-950 p-5">
            <h2 className="text-xl font-black text-white">{category.name}</h2>
            <p className="mt-2 text-slate-400">{category.description ?? "No description"}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
