"use client";

import { clearSession, getSession } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export function CustomerHeader() {
  const [hasSession, setHasSession] = useState(false);
  const [name, setName] = useState<string | null>(null);

  useEffect(() => {
    const session = getSession();
    setHasSession(Boolean(session));
    setName(session?.name ?? null);
  }, []);

  return (
    <div className="flex items-center gap-5 text-sm font-semibold text-stone-700">
      <Link href="/products">Products</Link>
      <Link href="/cart">Cart</Link>
      <Link href="/orders">Orders</Link>
      <Link href="/account/addresses">Addresses</Link>
      {hasSession ? (
        <button
          onClick={() => {
            clearSession();
            window.location.href = "/";
          }}
          className="rounded-full border border-stone-300 px-4 py-2 text-stone-700"
        >
          Logout{name ? ` ${name}` : ""}
        </button>
      ) : (
        <Link href="/login" className="rounded-full bg-stone-900 px-4 py-2 text-white">Login</Link>
      )}
    </div>
  );
}
