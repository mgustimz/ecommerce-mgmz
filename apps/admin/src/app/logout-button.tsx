"use client";

import { clearAdminSession } from "@/lib/session";
import { useRouter } from "next/navigation";

export function LogoutButton() {
  const router = useRouter();

  return (
    <button
      onClick={() => {
        clearAdminSession();
        router.push("/login");
      }}
      className="mt-3 w-full rounded-2xl border border-slate-800 px-4 py-3 text-left text-sm font-bold text-slate-300 hover:bg-slate-900"
    >
      Logout
    </button>
  );
}
