"use client";

import Link from "next/link";
import { useCartCount } from "@/lib/use-cart-count";

export function CartNavItem() {
  const { count } = useCartCount();

  return (
    <Link
      href="/cart"
      aria-label={`Cart, ${count} ${count === 1 ? "item" : "items"}`}
      className="relative inline-flex items-center justify-center rounded-full p-2 text-neutral-700 transition hover:bg-neutral-100 hover:text-neutral-900"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <circle cx="9" cy="21" r="1" />
        <circle cx="20" cy="21" r="1" />
        <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
      </svg>
      {count > 0 && (
        <span
          className="absolute -right-1 -top-1 inline-flex min-w-5 items-center justify-center rounded-full px-1.5 py-0.5 text-xs font-black text-white"
          style={{ background: "#cc1d00" }}
        >
          {count}
        </span>
      )}
    </Link>
  );
}

