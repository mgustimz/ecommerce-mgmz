"use client";

import { createApiClient, type UserProfile } from "@mgmz/api-client";
import { CustomerAuthGuard } from "@/components/customer-auth-guard";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function ProfilePage() {
  return (
    <CustomerAuthGuard>
      <ProfileContent />
    </CustomerAuthGuard>
  );
}

function ProfileContent() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  async function loadProfile() {
    const token = getToken();
    if (!token) {
      setIsLoading(false);
      return;
    }
    try {
      setProfile(await createApiClient().users.me.get(token));
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load profile");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadProfile();
  }, []);

  async function saveProfile(formData: FormData) {
    const token = getToken();
    if (!token || !profile) return;
    setIsSaving(true);
    setError(null);
    setSuccess(null);
    try {
      const updated = await createApiClient().users.me.update(token, {
        name: String(formData.get("name")),
        phone: String(formData.get("phone") || "")
      });
      setProfile(updated);
      setSuccess("Profile updated.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update profile");
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <nav className="mb-4 text-xs text-neutral-500">
        <Link href="/" className="hover:text-red-600">Home</Link> / <span>My profile</span>
      </nav>
      <h1 className="text-3xl font-black">My profile</h1>

      {isLoading && <p className="mt-6 text-sm text-neutral-500">Loading profile...</p>}
      {error && <p className="mt-6 rounded border border-red-200 bg-red-50 p-4 text-sm font-bold text-red-700">{error}</p>}
      {success && <p className="mt-6 rounded border border-emerald-200 bg-emerald-50 p-4 text-sm font-bold text-emerald-700">{success}</p>}

      {profile && (
        <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_340px]">
          <form action={saveProfile} className="rounded-lg border border-neutral-200 bg-white p-6">
            <h2 className="text-lg font-black">Account details</h2>
            <div className="mt-5 grid gap-4 md:grid-cols-2">
              <div>
                <label className="text-sm font-bold text-neutral-700">Name</label>
                <input name="name" required defaultValue={profile.name} className="input-field mt-2" />
              </div>
              <div>
                <label className="text-sm font-bold text-neutral-700">Phone</label>
                <input name="phone" defaultValue={profile.phone ?? ""} placeholder="Optional" className="input-field mt-2" />
              </div>
            </div>
            <button type="submit" disabled={isSaving} className="btn-primary mt-6 disabled:opacity-60">
              {isSaving ? "Saving..." : "Save changes"}
            </button>
          </form>

          <aside className="h-fit space-y-4 rounded-lg border border-neutral-200 bg-white p-6 text-sm">
            <h2 className="text-lg font-black">Account info</h2>
            <div className="space-y-3 text-neutral-600">
              <p>
                <span className="block text-xs font-black uppercase tracking-wider text-neutral-400">Email</span>
                <span className="font-bold text-neutral-800">{profile.email}</span>
              </p>
              <p>
                <span className="block text-xs font-black uppercase tracking-wider text-neutral-400">Role</span>
                <span className="font-bold text-neutral-800">{profile.role}</span>
              </p>
            </div>
            <div className="border-t border-neutral-200 pt-4">
              <p className="text-xs text-neutral-500">Other account pages</p>
              <ul className="mt-2 space-y-2">
                <li><Link href="/account/addresses" className="font-bold text-red-600 hover:underline">My addresses</Link></li>
                <li><Link href="/orders" className="font-bold text-red-600 hover:underline">My orders</Link></li>
              </ul>
            </div>
          </aside>
        </div>
      )}
    </div>
  );
}
