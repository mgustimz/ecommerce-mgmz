"use client";

import { createApiClient, type Address } from "@mgmz/api-client";
import { CustomerAuthGuard } from "@/components/customer-auth-guard";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function AddressesPage() {
  return (
    <CustomerAuthGuard>
      <AddressesContent />
    </CustomerAuthGuard>
  );
}

function AddressesContent() {
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  async function loadAddresses() {
    const token = getToken();
    if (!token) {
      setIsLoading(false);
      return;
    }
    try {
      setAddresses(await createApiClient().addresses.list(token));
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load addresses");
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadAddresses();
  }, []);

  async function createAddress(formData: FormData) {
    const token = getToken();
    if (!token) return;

    try {
      await createApiClient().addresses.create(token, {
        label: String(formData.get("label")),
        recipientName: String(formData.get("recipientName")),
        phone: String(formData.get("phone")),
        street: String(formData.get("street")),
        city: String(formData.get("city")),
        province: String(formData.get("province")),
        postalCode: String(formData.get("postalCode")),
        areaId: String(formData.get("areaId") || ""),
        latitude: null,
        longitude: null,
        defaultAddress: formData.get("defaultAddress") === "on"
      });
      await loadAddresses();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create address");
    }
  }

  async function setDefault(id: number) {
    const token = getToken();
    if (!token) return;
    await createApiClient().addresses.setDefault(token, id);
    await loadAddresses();
  }

  async function deleteAddress(id: number) {
    const token = getToken();
    if (!token) return;
    await createApiClient().addresses.delete(token, id);
    await loadAddresses();
  }

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <nav className="mb-4 text-xs text-neutral-500">
        <Link href="/" className="hover:text-red-600">Home</Link> / <span>My addresses</span>
      </nav>
      <h1 className="text-3xl font-black">My addresses</h1>

      {isLoading && <p className="mt-6 text-sm text-neutral-500">Loading addresses...</p>}
      {error && <p className="mt-6 rounded border border-red-200 bg-red-50 p-4 text-sm font-bold text-red-700">{error}</p>}

      <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_420px]">
        <section className="space-y-3">
          {addresses.map((address) => (
            <div key={address.id} className="rounded-lg border border-neutral-200 bg-white p-5">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <div className="flex items-center gap-2">
                    <h2 className="text-base font-black">{address.label}</h2>
                    {address.defaultAddress && (
                      <span className="rounded-full px-2 py-0.5 text-[10px] font-black uppercase text-white" style={{ background: "#cc1d00" }}>
                        Default
                      </span>
                    )}
                  </div>
                  <p className="mt-2 font-bold">{address.recipientName} - {address.phone}</p>
                  <p className="mt-1 text-sm text-neutral-600">{address.street}, {address.city}, {address.province} {address.postalCode}</p>
                </div>
                <div className="flex shrink-0 gap-3 text-sm font-bold">
                  {!address.defaultAddress && (
                    <button onClick={() => void setDefault(address.id)} className="hover:underline" style={{ color: "#cc1d00" }}>Set default</button>
                  )}
                  <button onClick={() => void deleteAddress(address.id)} className="text-neutral-500 hover:text-red-600">Delete</button>
                </div>
              </div>
            </div>
          ))}
          {!isLoading && !error && addresses.length === 0 && (
            <div className="rounded-lg border border-neutral-200 bg-white p-10 text-center">
              <p className="text-base font-bold">No saved addresses</p>
              <p className="mt-1 text-sm text-neutral-500">Add your first address using the form on the right.</p>
            </div>
          )}
        </section>

        <form action={createAddress} className="h-fit rounded-lg border border-neutral-200 bg-white p-6">
          <h2 className="text-lg font-black">Add new address</h2>
          <div className="mt-5 grid gap-3">
            <input name="label" placeholder="Label, e.g. Home" required className="input-field" />
            <input name="recipientName" placeholder="Recipient name" required className="input-field" />
            <input name="phone" placeholder="Phone" required className="input-field" />
            <textarea name="street" placeholder="Street address" required rows={2} className="input-field"></textarea>
            <div className="grid gap-3 md:grid-cols-2">
              <input name="city" placeholder="City" required className="input-field" />
              <input name="province" placeholder="Province" required className="input-field" />
            </div>
            <div className="grid gap-3 md:grid-cols-2">
              <input name="postalCode" placeholder="Postal code" required className="input-field" />
              <input name="areaId" placeholder="Area ID (optional)" className="input-field" />
            </div>
            <label className="flex items-center gap-2 text-sm font-bold text-neutral-700">
              <input name="defaultAddress" type="checkbox" /> Set as default
            </label>
            <button type="submit" className="btn-primary">Save address</button>
          </div>
        </form>
      </div>
    </div>
  );
}
