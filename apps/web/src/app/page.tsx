import { createApiClient } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import Link from "next/link";

export default async function Home() {
  const api = createApiClient();
  const products = await api.products.list({ size: 6 }).catch(() => ({ items: [] }));

  return (
    <div>
      <section className="mx-auto grid max-w-6xl gap-10 px-5 py-16 md:grid-cols-[1.1fr_0.9fr] md:items-center">
        <div>
          <p className="mb-4 text-sm font-bold uppercase tracking-[0.28em] text-amber-700">Single-store essentials</p>
          <h1 className="text-5xl font-black tracking-tight md:text-7xl">Curated goods, simple checkout.</h1>
          <p className="mt-6 max-w-xl text-lg leading-8 text-stone-700">
            A focused storefront for MGMZ products with cart, checkout, orders, and account features wired to the Spring Boot API.
          </p>
          <Link href="/products" className="mt-8 inline-flex rounded-full bg-amber-600 px-6 py-3 font-bold text-white shadow-lg shadow-amber-900/10">
            Shop products
          </Link>
        </div>
        <div className="rounded-[2rem] bg-stone-900 p-8 text-white shadow-2xl">
          <p className="text-sm uppercase tracking-[0.28em] text-amber-300">MVP ready</p>
          <div className="mt-12 text-6xl font-black">Storefront</div>
          <p className="mt-4 text-stone-300">Products, auth, cart, checkout, and orders come next from this foundation.</p>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-5 pb-16">
        <div className="mb-6 flex items-end justify-between">
          <h2 className="text-2xl font-black">Latest products</h2>
          <Link href="/products" className="font-bold text-amber-700">View all</Link>
        </div>
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {products.items.map((product) => (
            <Link key={product.id} href={`/products/${product.slug}`} className="rounded-3xl border border-stone-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-xl">
              <div className="aspect-[4/3] rounded-2xl bg-stone-100" />
              <h3 className="mt-4 text-lg font-black">{product.name}</h3>
              <p className="mt-1 text-sm text-stone-500">{product.categoryName ?? "Product"}</p>
              <p className="mt-3 font-black text-amber-700">{formatCurrency(product.price)}</p>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}
