"use client";

import { createApiClient, type Address } from "@mgmz/api-client";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function AddressesPage() {
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [error, setError] = useState<string | null>(null);

  async function loadAddresses() {
    const token = getToken();
    if (!token) {
      setError("Login to manage addresses.");
      return;
    }
    setAddresses(await createApiClient().addresses.list(token));
    setError(null);
  }

  useEffect(() => {
    void loadAddresses();
  }, []);

  async function createAddress(formData: FormData) {
    const token = getToken();
    if (!token) return;

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
    <div className="mx-auto grid max-w-6xl gap-8 px-5 py-12 lg:grid-cols-[1fr_420px]">
      <section>
        <h1 className="text-4xl font-black">Addresses</h1>
        {error && <p className="mt-6 rounded-2xl bg-amber-50 p-4 font-bold text-amber-800">{error} <Link href="/login" className="underline">Login</Link></p>}
        <div className="mt-6 space-y-4">
          {addresses.map((address) => (
            <div key={address.id} className="rounded-3xl bg-white p-5 shadow-sm">
              <div className="flex items-start justify-between gap-4">
                <div>
                  <h2 className="text-xl font-black">{address.label} {address.defaultAddress && <span className="text-sm text-amber-700">Default</span>}</h2>
                  <p className="mt-2 font-bold">{address.recipientName} - {address.phone}</p>
                  <p className="mt-1 text-stone-600">{address.street}, {address.city}, {address.province} {address.postalCode}</p>
                </div>
                <div className="flex shrink-0 gap-3 text-sm font-bold">
                  <button onClick={() => void setDefault(address.id)} className="text-amber-700">Set default</button>
                  <button onClick={() => void deleteAddress(address.id)} className="text-red-700">Delete</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      <form action={createAddress} className="h-fit rounded-3xl bg-white p-6 shadow-sm">
        <h2 className="text-2xl font-black">Add address</h2>
        <div className="mt-5 grid gap-3">
          <input name="label" placeholder="Label, e.g. Home" required className="rounded-2xl border border-stone-200 px-4 py-3" />
          <input name="recipientName" placeholder="Recipient name" required className="rounded-2xl border border-stone-200 px-4 py-3" />
          <input name="phone" placeholder="Phone" required className="rounded-2xl border border-stone-200 px-4 py-3" />
          <textarea name="street" placeholder="Street address" required className="min-h-24 rounded-2xl border border-stone-200 px-4 py-3" />
          <div className="grid gap-3 md:grid-cols-2">
            <input name="city" placeholder="City" required className="rounded-2xl border border-stone-200 px-4 py-3" />
            <input name="province" placeholder="Province" required className="rounded-2xl border border-stone-200 px-4 py-3" />
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <input name="postalCode" placeholder="Postal code" required className="rounded-2xl border border-stone-200 px-4 py-3" />
            <input name="areaId" placeholder="Area ID optional" className="rounded-2xl border border-stone-200 px-4 py-3" />
          </div>
          <label className="flex items-center gap-2 text-sm font-bold"><input name="defaultAddress" type="checkbox" /> Set as default</label>
          <button className="rounded-2xl bg-stone-900 px-5 py-3 font-black text-white">Save address</button>
        </div>
      </form>
    </div>
  );
}
