"use client";

import { clearSession, getSession } from "@/lib/session";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";

export function CustomerHeader() {
  const [hasSession, setHasSession] = useState(false);
  const pathname = usePathname();

  useEffect(() => {
    setHasSession(Boolean(getSession()));
  }, [pathname]);

  if (hasSession) {
    return (
      <div className="flex items-center gap-4">
        <Link href="/account/profile">My Account</Link>
        <Link href="/account/addresses">Addresses</Link>
        <Link href="/cart">Cart</Link>
        <button
          onClick={() => {
            clearSession();
            window.location.href = "/";
          }}
          className="underline-offset-2 hover:underline"
        >
          Logout
        </button>
      </div>
    );
  }

  return (
    <div className="flex items-center gap-4">
      <Link href="/register" className="underline-offset-2 hover:underline">Register</Link>
      <Link href="/login" className="underline-offset-2 hover:underline">Login</Link>
    </div>
  );
}
