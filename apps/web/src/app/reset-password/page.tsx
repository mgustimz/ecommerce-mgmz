"use client";

import { createApiClient } from "@mgmz/api-client";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useRef, useState } from "react";

export default function ResetPasswordPage() {
  return (
    <Suspense fallback={<div className="mx-auto max-w-md px-5 py-12 text-sm text-neutral-500">Loading...</div>}>
      <ResetPasswordContent />
    </Suspense>
  );
}

const REDIRECT_DELAY_MS = 3000;

function ResetPasswordContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isRedirecting, setIsRedirecting] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const redirectTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    return () => {
      if (redirectTimer.current) {
        clearTimeout(redirectTimer.current);
      }
    };
  }, []);

  async function handleSubmit(formData: FormData) {
    setIsSubmitting(true);
    setMessage(null);
    setError(null);
    try {
      const response = await createApiClient().auth.resetPassword({
        token: searchParams.get("token") ?? "",
        password: String(formData.get("password"))
      });
      setMessage(response.message);
      setIsRedirecting(true);
      redirectTimer.current = setTimeout(() => router.replace("/login"), REDIRECT_DELAY_MS);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to reset password");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-md px-5 py-12">
      <div className="rounded-lg border border-neutral-200 bg-white p-8">
        <h1 className="text-2xl font-black">Choose a new password</h1>
        <p className="mt-1 text-sm text-neutral-500">At least 8 characters with uppercase, lowercase, and a number.</p>

        <form action={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="text-sm font-bold text-neutral-700">New password</label>
            <input name="password" type="password" minLength={8} required className="input-field mt-2" placeholder="New password" />
          </div>
          <div>
            <label className="text-sm font-bold text-neutral-700">Confirm new password</label>
            <input name="confirmPassword" type="password" minLength={8} required className="input-field mt-2" placeholder="Repeat new password" />
          </div>

          {error && (
            <p className="rounded border border-red-200 bg-red-50 p-3 text-sm font-bold text-red-700">{error}</p>
          )}
          {message && (
            <p className="rounded border border-emerald-200 bg-emerald-50 p-3 text-sm font-bold text-emerald-700">
              {message}
              {isRedirecting && <span> Redirecting you to login in a few seconds…</span>}{" "}
              <Link href="/login" className="underline">Go to login</Link>
            </p>
          )}

          <button type="submit" disabled={isSubmitting} className="btn-primary w-full disabled:opacity-60">
            {isSubmitting ? "Updating..." : "Update password"}
          </button>
        </form>
      </div>
    </div>
  );
}
