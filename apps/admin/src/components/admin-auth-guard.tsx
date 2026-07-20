"use client";

import { getAdminSession } from "@/lib/session";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export function AdminAuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const session = getAdminSession();
    if (!session || session.role !== "ADMIN") {
      router.replace("/login");
      return;
    }
    setReady(true);
  }, [router]);

  if (!ready) {
    return <div className="text-slate-400">Checking session...</div>;
  }

  return <>{children}</>;
}
