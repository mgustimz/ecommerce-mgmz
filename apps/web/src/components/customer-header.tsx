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
        <Link href="/account/addresses">My Account</Link>
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
      <Link href="/login">My Account</Link>
      <Link href="/cart">Cart</Link>
    </div>
  );
}
