import { createApiClient } from "@mgmz/api-client";
import { ProductCard } from "@/components/product-card";
import Link from "next/link";

type Props = {
  searchParams: Promise<{ q?: string; categoryId?: string; sort?: string }>;
};

export default async function ProductsPage({ searchParams }: Props) {
  const params = await searchParams;
  const api = createApiClient();
  const [productsResponse, categories] = await Promise.all([
    api.products.list({
      q: params.q,
      categoryId: params.categoryId ? Number(params.categoryId) : undefined,
      sort: params.sort ?? "NAME_ASC",
      size: 60
    }),
    api.categories.list().catch(() => [])
  ]);

  const activeCategory = params.categoryId ? categories.find((c) => c.id === Number(params.categoryId)) : null;

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <nav className="mb-4 text-xs text-neutral-500">
        <Link href="/" className="hover:text-red-600">Home</Link> / <span>Products</span>
      </nav>

      <h1 className="text-3xl font-black">
        {activeCategory ? activeCategory.name : "All products"}
      </h1>
      <p className="mt-1 text-sm text-neutral-500">
        {productsResponse.items.length} {productsResponse.items.length === 1 ? "product" : "products"} found
      </p>

      <form className="my-6 grid gap-3 rounded-lg border border-neutral-200 bg-white p-4 md:grid-cols-[1fr_220px_180px_auto]">
        <input name="q" defaultValue={params.q} placeholder="Search products or SKU" className="input-field" />
        <select name="categoryId" defaultValue={params.categoryId ?? ""} className="input-field">
          <option value="">All categories</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>{category.name}</option>
          ))}
        </select>
        <select name="sort" defaultValue={params.sort ?? "NAME_ASC"} className="input-field">
          <option value="NAME_ASC">Name A-Z</option>
          <option value="NEWEST">Newest</option>
          <option value="PRICE_ASC">Lowest price</option>
          <option value="PRICE_DESC">Highest price</option>
        </select>
        <button type="submit" className="btn-primary">Apply</button>
      </form>

      {productsResponse.items.length === 0 ? (
        <div className="rounded-lg border border-neutral-200 bg-white p-10 text-center">
          <p className="text-base font-bold">No products found</p>
          <p className="mt-1 text-sm text-neutral-500">Try a different search term or category.</p>
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
          {productsResponse.items.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </div>
  );
}
