import type { Metadata } from "next";
import Link from "next/link";
import { LogoutButton } from "./logout-button";
import "./globals.css";

export const metadata: Metadata = {
  title: "MGMZ Admin",
  description: "Admin dashboard for MGMZ ecommerce"
};

const links = [
  ["Dashboard", "/dashboard"],
  ["Products", "/products"],
  ["Categories", "/categories"],
  ["Orders", "/orders"],
  ["Inventory", "/inventory"]
];

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <div className="min-h-screen md:grid md:grid-cols-[260px_1fr]">
          <aside className="border-r border-slate-800 bg-slate-950 p-5">
            <Link href="/dashboard" className="text-xl font-black">MGMZ Admin</Link>
            <nav className="mt-8 grid gap-2">
              {links.map(([label, href]) => (
                <Link key={href} href={href} className="rounded-2xl px-4 py-3 text-sm font-bold text-slate-300 hover:bg-slate-900 hover:text-white">
                  {label}
                </Link>
              ))}
            </nav>
            <Link href="/login" className="mt-8 inline-flex rounded-2xl bg-cyan-400 px-4 py-3 text-sm font-black text-slate-950">
              Admin login
            </Link>
            <LogoutButton />
          </aside>
          <main className="p-5 md:p-8">{children}</main>
        </div>
      </body>
    </html>
  );
}
