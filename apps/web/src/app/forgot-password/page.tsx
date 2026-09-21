"use client";

import { createApiClient } from "@mgmz/api-client";
import Link from "next/link";
import { useState } from "react";

export default function ForgotPasswordPage() {
  const [message, setMessage] = useState<string | null>(null);
  const [devLink, setDevLink] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(formData: FormData) {
    setIsSubmitting(true);
    setMessage(null);
    setDevLink(null);
    try {
      const response = await createApiClient().auth.forgotPassword({
        email: String(formData.get("email"))
      });
      setMessage(response.message);
      // LoggingEmailSender prints the link to the server log; nothing to show here.
    } catch (err) {
      setMessage(err instanceof Error ? err.message : "Failed to request password reset");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-md px-5 py-12">
      <div className="rounded-lg border border-neutral-200 bg-white p-8">
        <h1 className="text-2xl font-black">Forgot your password?</h1>
        <p className="mt-1 text-sm text-neutral-500">Enter your email and we will send you a reset link.</p>

        <form action={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="text-sm font-bold text-neutral-700">Email</label>
            <input name="email" type="email" required className="input-field mt-2" placeholder="you@example.com" />
          </div>

          {message && (
            <p className="rounded border border-emerald-200 bg-emerald-50 p-3 text-sm font-bold text-emerald-700">
              {message}
            </p>
          )}

          <button type="submit" disabled={isSubmitting} className="btn-primary w-full disabled:opacity-60">
            {isSubmitting ? "Sending..." : "Send reset link"}
          </button>
        </form>

        {devLink && (
          <p className="mt-4 break-all rounded border border-amber-200 bg-amber-50 p-3 text-xs text-amber-800">
            Dev reset link: {devLink}
          </p>
        )}

        <p className="mt-6 text-center text-sm text-neutral-600">
          Back to{" "}
          <Link href="/login" className="font-bold" style={{ color: "#cc1d00" }}>
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
