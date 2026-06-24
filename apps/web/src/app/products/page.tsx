import { createApiClient } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import Link from "next/link";

type Props = {
  searchParams: Promise<{ q?: string; categoryId?: string; sort?: string }>;
};

export default async function ProductsPage({ searchParams }: Props) {
  const params = await searchParams;
  const api = createApiClient();
  const [products, categories] = await Promise.all([
    api.products.list({
      q: params.q,
      categoryId: params.categoryId ? Number(params.categoryId) : undefined,
      sort: params.sort ?? "NAME_ASC",
      size: 24
    }),
    api.categories.list().catch(() => [])
  ]);

  return (
    <div className="mx-auto max-w-6xl px-5 py-10">
      <div className="mb-8 rounded-3xl bg-white p-6 shadow-sm">
        <h1 className="text-4xl font-black">Products</h1>
        <form className="mt-6 grid gap-3 md:grid-cols-[1fr_220px_180px_auto]">
          <input name="q" defaultValue={params.q} placeholder="Search products" className="rounded-2xl border border-stone-200 px-4 py-3" />
          <select name="categoryId" defaultValue={params.categoryId ?? ""} className="rounded-2xl border border-stone-200 px-4 py-3">
            <option value="">All categories</option>
            {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
          </select>
          <select name="sort" defaultValue={params.sort ?? "NAME_ASC"} className="rounded-2xl border border-stone-200 px-4 py-3">
            <option value="NAME_ASC">Name A-Z</option>
            <option value="PRICE_ASC">Lowest price</option>
            <option value="PRICE_DESC">Highest price</option>
          </select>
          <button className="rounded-2xl bg-stone-900 px-5 py-3 font-bold text-white">Apply</button>
        </form>
      </div>

      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {products.items.map((product) => (
          <Link key={product.id} href={`/products/${product.slug}`} className="rounded-3xl border border-stone-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-xl">
            <div className="aspect-[4/3] rounded-2xl bg-stone-100" />
            <h2 className="mt-4 text-lg font-black">{product.name}</h2>
            <p className="mt-1 text-sm text-stone-500">Stock: {product.stock}</p>
            <p className="mt-3 font-black text-amber-700">{formatCurrency(product.price)}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
