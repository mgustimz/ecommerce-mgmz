"use client";

import { createApiClient } from "@mgmz/api-client";
import { setAdminSession } from "@/lib/session";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function LoginPage() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(formData: FormData) {
    setError(null);
    try {
      const session = await createApiClient().auth.login({
        email: String(formData.get("email")),
        password: String(formData.get("password"))
      });

      if (session.role !== "ADMIN") {
        setError("This account is not an admin.");
        return;
      }

      setAdminSession(session);
      router.push("/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    }
  }

  return (
    <div className="mx-auto flex min-h-[80vh] max-w-md items-center">
      <form action={handleSubmit} className="w-full rounded-3xl border border-slate-800 bg-slate-950 p-8 shadow-2xl">
        <p className="text-sm font-bold uppercase tracking-[0.24em] text-cyan-300">Admin access</p>
        <h1 className="mt-3 text-3xl font-black">Sign in</h1>
        <label className="mt-6 block text-sm font-bold">Email</label>
        <input name="email" type="email" required className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
        <label className="mt-4 block text-sm font-bold">Password</label>
        <input name="password" type="password" required className="mt-2 w-full rounded-2xl border border-slate-700 bg-slate-900 px-4 py-3 text-white" />
        {error && <p className="mt-4 rounded-2xl bg-red-950 p-3 text-sm font-bold text-red-200">{error}</p>}
        <button className="mt-6 w-full rounded-2xl bg-cyan-400 px-5 py-3 font-black text-slate-950">Login</button>
      </form>
    </div>
  );
}
