import type { Metadata } from "next";
import Link from "next/link";
import { CustomerHeader } from "@/components/customer-header";
import "./globals.css";

export const metadata: Metadata = {
  title: "MGMZ Store | Single-store Ecommerce",
  description: "Curated single-store essentials with fast checkout and secure payment."
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body className="bg-white text-neutral-900">
        <div className="bg-neutral-900 text-white text-xs">
          <div className="mx-auto flex max-w-6xl items-center justify-between px-5 py-2">
            <span>Free shipping on orders over Rp 250.000</span>
            <CustomerHeader />
          </div>
        </div>

        <header className="border-b border-neutral-200">
          <div className="mx-auto grid max-w-6xl grid-cols-1 items-center gap-4 px-5 py-4 md:grid-cols-[auto_1fr_auto]">
            <Link href="/" className="flex items-center gap-2">
              <span className="grid h-12 w-12 place-items-center rounded-full bg-red-600 text-2xl font-black text-white" style={{ background: "#cc1d00" }}>M</span>
              <span className="leading-tight">
                <span className="block text-lg font-black tracking-tight">MGMZ Store</span>
                <span className="block text-xs font-bold text-neutral-500">Dealer &amp; Retail Store</span>
              </span>
            </Link>
            <form action="/products" method="get" className="flex w-full">
              <input
                name="q"
                type="search"
                placeholder="Search products, SKU, or part code"
                className="input-field rounded-r-none border-r-0"
              />
              <button type="submit" className="btn-primary rounded-l-none px-6" style={{ background: "#cc1d00" }}>
                Search
              </button>
            </form>
            <div className="hidden text-right md:block">
              <p className="text-sm font-bold">Customer Service</p>
              <p className="text-sm text-neutral-600">+62 21 555 0000</p>
            </div>
          </div>
        </header>

        <nav className="border-b border-neutral-200 bg-white">
          <div className="mx-auto flex max-w-6xl items-center gap-1 overflow-x-auto px-5 text-sm font-bold">
            <Link href="/products" className="rounded px-4 py-3 hover:bg-neutral-100">Products</Link>
            <Link href="/products?sort=NEWEST" className="rounded px-4 py-3 hover:bg-neutral-100">New Arrivals</Link>
            <Link href="/products?sort=PRICE_ASC" className="rounded px-4 py-3 hover:bg-neutral-100">Best Deals</Link>
            <Link href="/cart" className="rounded px-4 py-3 hover:bg-neutral-100">Cart</Link>
            <Link href="/orders" className="rounded px-4 py-3 hover:bg-neutral-100">My Orders</Link>
            <Link href="/account/addresses" className="rounded px-4 py-3 hover:bg-neutral-100">Addresses</Link>
          </div>
        </nav>

        <main className="bg-neutral-50">{children}</main>

        <footer className="border-t border-neutral-200 bg-white">
          <div className="mx-auto grid max-w-6xl gap-8 px-5 py-10 md:grid-cols-4">
            <section>
              <h3 className="mb-3 text-sm font-black uppercase tracking-wider text-neutral-700">Customer Service</h3>
              <ul className="space-y-2 text-sm text-neutral-600">
                <li><Link href="/account/addresses">My Addresses</Link></li>
                <li><Link href="/orders">Order Tracking</Link></li>
                <li><Link href="/login">Account</Link></li>
              </ul>
            </section>
            <section>
              <h3 className="mb-3 text-sm font-black uppercase tracking-wider text-neutral-700">Shop</h3>
              <ul className="space-y-2 text-sm text-neutral-600">
                <li><Link href="/products">All Products</Link></li>
                <li><Link href="/products?sort=NEWEST">New Arrivals</Link></li>
                <li><Link href="/products?sort=PRICE_ASC">Best Deals</Link></li>
              </ul>
            </section>
            <section>
              <h3 className="mb-3 text-sm font-black uppercase tracking-wider text-neutral-700">Payment Methods</h3>
              <div className="grid grid-cols-3 gap-2 text-xs font-bold text-neutral-700">
                <span className="rounded border border-neutral-200 bg-neutral-50 px-2 py-2 text-center">Bank Transfer</span>
                <span className="rounded border border-neutral-200 bg-neutral-50 px-2 py-2 text-center">Virtual Account</span>
                <span className="rounded border border-neutral-200 bg-neutral-50 px-2 py-2 text-center">E-Wallet</span>
                <span className="rounded border border-neutral-200 bg-neutral-50 px-2 py-2 text-center">QRIS</span>
                <span className="rounded border border-neutral-200 bg-neutral-50 px-2 py-2 text-center">COD</span>
              </div>
            </section>
            <section>
              <h3 className="mb-3 text-sm font-black uppercase tracking-wider text-neutral-700">Store Location</h3>
              <p className="text-sm text-neutral-600">Jl. Cengkareng Raya No. 12A, Cengkareng Timur, Jakarta 11730</p>
              <p className="mt-2 text-sm text-neutral-600">Open 7 days a week, 09.00 - 19.00</p>
            </section>
          </div>
          <div className="border-t border-neutral-200 bg-neutral-100">
            <div className="mx-auto max-w-6xl px-5 py-4 text-center text-xs text-neutral-500">
              MGMZ Store. Single-store ecommerce demo built with Next.js &amp; Spring Boot.
            </div>
          </div>
        </footer>
      </body>
    </html>
  );
}
