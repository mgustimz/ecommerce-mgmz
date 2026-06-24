"use client";

import { createApiClient, type Address, type Cart } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function CheckoutPage() {
  const router = useRouter();
  const [cart, setCart] = useState<Cart | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      setError("Login before checkout.");
      return;
    }
    Promise.all([createApiClient().cart.get(token), createApiClient().addresses.list(token)])
      .then(([nextCart, nextAddresses]) => {
        setCart(nextCart);
        setAddresses(nextAddresses);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load checkout"));
  }, []);

  async function checkout(formData: FormData) {
    const token = getToken();
    if (!token) return;
    try {
      const order = await createApiClient().orders.checkout(token, {
        addressId: Number(formData.get("addressId")),
        shippingServiceCode: String(formData.get("shippingServiceCode")),
        paymentMethod: String(formData.get("paymentMethod")),
        notes: String(formData.get("notes") ?? "")
      });
      router.push(`/orders/${order.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Checkout failed");
    }
  }

  const defaultAddress = addresses.find((address) => address.defaultAddress) ?? addresses[0];

  return (
    <div className="mx-auto grid max-w-6xl gap-8 px-5 py-12 lg:grid-cols-[1fr_360px]">
      <form action={checkout} className="rounded-3xl bg-white p-6 shadow-sm">
        <h1 className="text-4xl font-black">Checkout</h1>
        {error && <p className="mt-6 rounded-2xl bg-red-50 p-4 font-bold text-red-700">{error}</p>}
        {addresses.length === 0 && <p className="mt-6 rounded-2xl bg-amber-50 p-4 font-bold text-amber-800">Add an address first. <Link href="/account/addresses" className="underline">Manage addresses</Link></p>}
        <label className="mt-6 block text-sm font-bold">Shipping address</label>
        <select name="addressId" defaultValue={defaultAddress?.id} required className="mt-2 w-full rounded-2xl border border-stone-200 px-4 py-3">
          {addresses.map((address) => <option key={address.id} value={address.id}>{address.label} - {address.street}</option>)}
        </select>
        <label className="mt-4 block text-sm font-bold">Shipping service</label>
        <select name="shippingServiceCode" defaultValue="REG" className="mt-2 w-full rounded-2xl border border-stone-200 px-4 py-3">
          <option value="REG">Regular</option>
          <option value="EXP">Express</option>
        </select>
        <label className="mt-4 block text-sm font-bold">Payment method</label>
        <select name="paymentMethod" defaultValue="BANK_TRANSFER" className="mt-2 w-full rounded-2xl border border-stone-200 px-4 py-3">
          <option value="BANK_TRANSFER">Bank transfer</option>
          <option value="VIRTUAL_ACCOUNT">Virtual account</option>
          <option value="EWALLET">E-wallet</option>
          <option value="QRIS">QRIS</option>
          <option value="COD">COD</option>
        </select>
        <label className="mt-4 block text-sm font-bold">Notes</label>
        <textarea name="notes" className="mt-2 min-h-24 w-full rounded-2xl border border-stone-200 px-4 py-3" />
        <button disabled={!cart || cart.items.length === 0 || addresses.length === 0} className="mt-6 rounded-2xl bg-stone-900 px-5 py-3 font-black text-white disabled:opacity-50">Place order</button>
      </form>

      <aside className="h-fit rounded-3xl bg-stone-900 p-6 text-white shadow-xl">
        <p className="text-sm uppercase tracking-[0.24em] text-amber-300">Cart total</p>
        <p className="mt-4 text-3xl font-black">{cart ? formatCurrency(cart.subtotal) : "-"}</p>
        <p className="mt-2 text-sm text-stone-300">Shipping fee is calculated by the backend after order placement.</p>
      </aside>
    </div>
  );
}
