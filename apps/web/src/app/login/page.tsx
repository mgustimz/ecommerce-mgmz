"use client";

import { createApiClient } from "@mgmz/api-client";
import { setSession } from "@/lib/session";
import Link from "next/link";
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
      setSession(session);
      router.push("/products");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    }
  }

  return (
    <div className="mx-auto max-w-md px-5 py-16">
      <form action={handleSubmit} className="rounded-3xl bg-white p-8 shadow-sm">
        <h1 className="text-3xl font-black">Customer login</h1>
        <label className="mt-6 block text-sm font-bold">Email</label>
        <input name="email" type="email" required className="mt-2 w-full rounded-2xl border border-stone-200 px-4 py-3" />
        <label className="mt-4 block text-sm font-bold">Password</label>
        <input name="password" type="password" required className="mt-2 w-full rounded-2xl border border-stone-200 px-4 py-3" />
        {error && <p className="mt-4 rounded-2xl bg-red-50 p-3 text-sm font-bold text-red-700">{error}</p>}
        <button className="mt-6 w-full rounded-2xl bg-stone-900 px-5 py-3 font-black text-white">Login</button>
        <p className="mt-5 text-center text-sm text-stone-600">
          New customer? <Link href="/register" className="font-black text-amber-700">Create account</Link>
        </p>
      </form>
    </div>
  );
}
