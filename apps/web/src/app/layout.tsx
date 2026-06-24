import type { Metadata } from "next";
import Link from "next/link";
import "./globals.css";

export const metadata: Metadata = {
  title: "MGMZ Store",
  description: "Single-store ecommerce storefront"
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <header className="border-b border-stone-200 bg-[#fffaf2]/90 backdrop-blur">
          <div className="mx-auto flex max-w-6xl items-center justify-between px-5 py-4">
            <Link href="/" className="text-xl font-black tracking-tight">
              MGMZ Store
            </Link>
            <nav className="flex items-center gap-5 text-sm font-semibold text-stone-700">
              <Link href="/products">Products</Link>
              <Link href="/cart">Cart</Link>
              <Link href="/orders">Orders</Link>
              <Link href="/account/addresses">Addresses</Link>
              <Link href="/login" className="rounded-full bg-stone-900 px-4 py-2 text-white">
                Login
              </Link>
            </nav>
          </div>
        </header>
        <main>{children}</main>
      </body>
    </html>
  );
}
