"use client";

import { createApiClient } from "@mgmz/api-client";
import { clearAnonCartToken, getAnonCartToken } from "@/lib/cart-cookie";
import { setSession } from "@/lib/session";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function RegisterPage() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(formData: FormData) {
    setIsSubmitting(true);
    setError(null);
    try {
      const session = await createApiClient().auth.register({
        name: String(formData.get("name")),
        email: String(formData.get("email")),
        password: String(formData.get("password"))
      });
      setSession(session);

      const anonToken = getAnonCartToken();
      if (anonToken) {
        try {
          await createApiClient().anonymousCart.merge(session.token, anonToken);
        } catch {
          // Ignore merge failures; the guest cart may simply be empty.
        }
        clearAnonCartToken();
      }

      const params = new URLSearchParams(window.location.search);
      const next = params.get("next");
      router.push(next || "/products");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-md px-5 py-12">
      <div className="rounded-lg border border-neutral-200 bg-white p-8">
        <h1 className="text-2xl font-black">Create your account</h1>
        <p className="mt-1 text-sm text-neutral-500">Join MGMZ Store to start shopping.</p>

        <form action={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="text-sm font-bold text-neutral-700">Full name</label>
            <input name="name" required className="input-field mt-2" placeholder="Your name" />
          </div>
          <div>
            <label className="text-sm font-bold text-neutral-700">Email</label>
            <input name="email" type="email" required className="input-field mt-2" placeholder="you@example.com" />
          </div>
          <div>
            <label className="text-sm font-bold text-neutral-700">Password</label>
            <input name="password" type="password" minLength={8} required className="input-field mt-2" placeholder="At least 8 characters" />
          </div>

          {error && (
            <p className="rounded border border-red-200 bg-red-50 p-3 text-sm font-bold text-red-700">
              {error}
            </p>
          )}

          <button type="submit" disabled={isSubmitting} className="btn-primary w-full disabled:opacity-60">
            {isSubmitting ? "Creating account..." : "Create account"}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-neutral-600">
          Already have an account?{" "}
          <Link href="/login" className="font-bold" style={{ color: "#cc1d00" }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
