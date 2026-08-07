"use client";

import { getSession } from "@/lib/session";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export function CustomerAuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const session = getSession();
    if (!session) {
      router.replace("/login");
      return;
    }
    setReady(true);
  }, [router]);

  if (!ready) {
    return <div className="text-stone-600">Checking session...</div>;
  }

  return <>{children}</>;
}
