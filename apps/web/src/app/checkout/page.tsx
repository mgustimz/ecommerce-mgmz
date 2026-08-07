"use client";

import { createApiClient, type Address, type Cart } from "@mgmz/api-client";
import { formatCurrency } from "@mgmz/shared";
import { CustomerAuthGuard } from "@/components/customer-auth-guard";
import { getToken } from "@/lib/session";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function CheckoutPage() {
  return (
    <CustomerAuthGuard>
      <CheckoutContent />
    </CustomerAuthGuard>
  );
}

function CheckoutContent() {
  const router = useRouter();
  const [cart, setCart] = useState<Cart | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      return;
    }
    Promise.all([createApiClient().cart.get(token), createApiClient().addresses.list(token)])
      .then(([nextCart, nextAddresses]) => {
        setCart(nextCart);
        setAddresses(nextAddresses);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load checkout"))
      .finally(() => setIsLoading(false));
  }, []);

  async function checkout(formData: FormData) {
    const token = getToken();
    if (!token) return;
    setIsSubmitting(true);
    setError(null);
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
    } finally {
      setIsSubmitting(false);
    }
  }

  const defaultAddress = addresses.find((address) => address.defaultAddress) ?? addresses[0];

  if (isLoading) {
    return <div className="mx-auto max-w-6xl px-5 py-10 text-sm text-neutral-500">Loading checkout...</div>;
  }

  return (
    <div className="mx-auto max-w-6xl px-5 py-8">
      <nav className="mb-4 text-xs text-neutral-500">
        <Link href="/" className="hover:text-red-600">Home</Link> / <Link href="/cart" className="hover:text-red-600">Cart</Link> / <span>Checkout</span>
      </nav>
      <h1 className="text-3xl font-black">Checkout</h1>

      <div className="mt-6 grid gap-6 lg:grid-cols-[1fr_360px]">
        <form action={checkout} className="space-y-5 rounded-lg border border-neutral-200 bg-white p-6">
          {error && <p className="rounded border border-red-200 bg-red-50 p-3 text-sm font-bold text-red-700">{error}</p>}

          {addresses.length === 0 ? (
            <p className="rounded border border-amber-200 bg-amber-50 p-4 text-sm font-bold text-amber-800">
              Add a shipping address first.{" "}
              <Link href="/account/addresses" className="underline">Manage addresses</Link>
            </p>
          ) : (
            <div>
              <label className="text-sm font-bold text-neutral-700">Shipping address</label>
              <select name="addressId" defaultValue={defaultAddress?.id} required className="input-field mt-2">
                {addresses.map((address) => (
                  <option key={address.id} value={address.id}>
                    {address.label} - {address.street}, {address.city}
                  </option>
                ))}
              </select>
            </div>
          )}

          <div>
            <label className="text-sm font-bold text-neutral-700">Shipping service</label>
            <select name="shippingServiceCode" defaultValue="REG" className="input-field mt-2">
              <option value="REG">Regular (2-4 days)</option>
              <option value="EXP">Express (1-2 days)</option>
            </select>
          </div>

          <div>
            <label className="text-sm font-bold text-neutral-700">Payment method</label>
            <select name="paymentMethod" defaultValue="BANK_TRANSFER" className="input-field mt-2">
              <option value="BANK_TRANSFER">Bank Transfer</option>
              <option value="VIRTUAL_ACCOUNT">Virtual Account</option>
              <option value="EWALLET">E-Wallet</option>
              <option value="QRIS">QRIS</option>
              <option value="COD">Cash on Delivery</option>
            </select>
          </div>

          <div>
            <label className="text-sm font-bold text-neutral-700">Order notes (optional)</label>
            <textarea name="notes" rows={3} className="input-field mt-2" placeholder="Delivery instructions or notes for the seller"></textarea>
          </div>

          <button
            type="submit"
            disabled={!cart || cart.items.length === 0 || addresses.length === 0 || isSubmitting}
            className="btn-primary w-full disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isSubmitting ? "Placing order..." : "Place order"}
          </button>
        </form>

        {cart && (
          <aside className="h-fit rounded-lg border border-neutral-200 bg-white p-6">
            <h2 className="text-base font-black">Order summary</h2>
            <ul className="mt-4 space-y-2 text-sm">
              {cart.items.map((item) => (
                <li key={item.id} className="flex justify-between">
                  <span className="truncate pr-2">{item.productName} x {item.quantity}</span>
                  <b>{formatCurrency(item.lineTotal)}</b>
                </li>
              ))}
            </ul>
            <div className="mt-4 border-t border-neutral-200 pt-4">
              <div className="flex justify-between text-sm"><span>Subtotal</span><b>{formatCurrency(cart.subtotal)}</b></div>
              <p className="mt-1 text-xs text-neutral-500">Shipping fee is calculated by the backend after order placement.</p>
            </div>
          </aside>
        )}
      </div>
    </div>
  );
}
