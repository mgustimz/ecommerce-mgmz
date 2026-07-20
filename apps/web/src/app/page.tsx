import { createApiClient, type Category, type Product } from "@mgmz/api-client";
import { ProductCard } from "@/components/product-card";
import Link from "next/link";

export default async function Home() {
  const api = createApiClient();
  const [productsResponse, categories] = await Promise.all([
    api.products.list({ size: 12, sort: "NEWEST" }).catch(() => ({ items: [] as Product[] })),
    api.categories.list().catch(() => [] as Category[])
  ]);
  const products = productsResponse.items;

  return (
    <div className="mx-auto max-w-6xl space-y-10 px-5 py-8">
      <section className="grid gap-4 md:grid-cols-[1.6fr_1fr]">
        <div className="relative overflow-hidden rounded-lg bg-neutral-900 p-10 text-white" style={{ background: "linear-gradient(135deg, #cc1d00 0%, #1c1818 100%)" }}>
          <p className="text-xs font-bold uppercase tracking-[0.32em] text-amber-300">MGMZ Store</p>
          <h1 className="mt-4 text-4xl font-black leading-tight md:text-5xl">Original parts &amp; essentials, delivered fast.</h1>
          <p className="mt-3 max-w-md text-sm text-white/80">Curated single-store catalog with secure checkout, multiple payment methods, and same-day pickup at our dealer location.</p>
          <div className="mt-6 flex flex-wrap gap-3">
            <Link href="/products" className="btn-primary bg-white text-neutral-900 hover:bg-neutral-200">Shop now</Link>
            <Link href="/products?sort=PRICE_ASC" className="btn-outline border-white text-white hover:bg-white hover:text-neutral-900">Best deals</Link>
          </div>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div className="rounded-lg border border-neutral-200 bg-white p-5">
            <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">Free shipping</p>
            <p className="mt-2 text-2xl font-black" style={{ color: "#cc1d00" }}>Rp 250K+</p>
            <p className="text-xs text-neutral-500">All categories</p>
          </div>
          <div className="rounded-lg border border-neutral-200 bg-white p-5">
            <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">Same day</p>
            <p className="mt-2 text-2xl font-black" style={{ color: "#cc1d00" }}>Pickup</p>
            <p className="text-xs text-neutral-500">Before 14.00</p>
          </div>
          <div className="rounded-lg border border-neutral-200 bg-white p-5">
            <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">Secure</p>
            <p className="mt-2 text-2xl font-black" style={{ color: "#cc1d00" }}>Checkout</p>
            <p className="text-xs text-neutral-500">JWT + 5 methods</p>
          </div>
          <div className="rounded-lg border border-neutral-200 bg-white p-5">
            <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">Warranty</p>
            <p className="mt-2 text-2xl font-black" style={{ color: "#cc1d00" }}>Original</p>
            <p className="text-xs text-neutral-500">100% genuine</p>
          </div>
        </div>
      </section>

      <section>
        <div className="section-title">
          <span>Shop by category</span>
          <Link href="/products">View all products</Link>
        </div>
        <div className="grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-4">
          {categories.slice(0, 8).map((category) => (
            <Link
              key={category.id}
              href={`/products?categoryId=${category.id}`}
              className="rounded-lg border border-neutral-200 bg-white p-5 transition hover:border-red-600 hover:shadow"
              style={{ borderColor: undefined }}
            >
              <div className="grid h-12 w-12 place-items-center rounded-full text-sm font-black text-white" style={{ background: "#cc1d00" }}>
                {category.name.slice(0, 2).toUpperCase()}
              </div>
              <h3 className="mt-3 text-base font-black">{category.name}</h3>
              <p className="mt-1 text-xs text-neutral-500">{category.description ?? "Browse this category"}</p>
            </Link>
          ))}
        </div>
      </section>

      <section>
        <div className="section-title">
          <span>Featured products</span>
          <Link href="/products?sort=NEWEST">See all</Link>
        </div>
        <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
          {products.slice(0, 8).map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2">
        <div className="rounded-lg border border-neutral-200 bg-white p-8">
          <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">For customers</p>
          <h3 className="mt-2 text-2xl font-black">What our customers say</h3>
          <div className="mt-3 flex items-center gap-3">
            <span className="star-row" aria-hidden>★★★★★</span>
            <span className="text-2xl font-black">4.7 / 5</span>
            <span className="text-sm text-neutral-500">from 120+ verified buyers</span>
          </div>
        </div>
        <div className="rounded-lg border border-neutral-200 bg-white p-8">
          <p className="text-xs font-bold uppercase tracking-wider text-neutral-500">Stay in touch</p>
          <h3 className="mt-2 text-2xl font-black">Visit our store</h3>
          <p className="mt-2 text-sm text-neutral-600">Jl. Cengkareng Raya No. 12A, Cengkareng Timur, Jakarta 11730</p>
          <p className="mt-1 text-sm text-neutral-600">Open 7 days a week, 09.00 - 19.00</p>
        </div>
      </section>
    </div>
  );
}
